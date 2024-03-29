package org.joshy.sketch.actions.io;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.model.PixelDocument;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.util.Util;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 11/2/11
 * Time: 5:01 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseExportAction extends SAction {
    protected DocContext context;
    private File lastfile;

    public BaseExportAction(DocContext context) {
        this.context = context;
    }

    public void execute() throws Exception {
        String ext = getStandardFileExtension();
        if(isDirectoryAction()) {
            File basedir = Util.requestDirectory("Choose Output Directory:", context);
            if(basedir == null) return;
            export(basedir, (SketchDocument) context.getDocument());
            lastfile = basedir;
            context.setLastExportAction(this);
            context.addNotification("Exported " + basedir.getName());
        } else {
            FileDialog fd = new FileDialog((Frame)context.getStage().getNativeWindow());
            fd.setMode(FileDialog.SAVE);
            fd.setTitle("Export " + ext.toUpperCase() + " File");
            File currentFile = context.getDocument().getFile();
            if(currentFile != null) {
                fd.setFile(currentFile.getName().substring(0,currentFile.getName().lastIndexOf('.'))+"."+ext);
            }
            fd.setVisible(true);
            if(fd.getFile() == null) return;
            try {
                String fileName = fd.getFile();
                if(!fileName.toLowerCase().endsWith("."+ext)) {
                    fileName = fileName + "."+ext;
                }
                File file = new File(fd.getDirectory(),fileName);
                if(context.getDocument() instanceof SketchDocument) {
                    export(file, (SketchDocument) context.getDocument());
                }
                if(context.getDocument() instanceof PixelDocument) {
                    //exportFragment(file, (PixelDocument) context.getDocument());
                }
                lastfile = file;
                context.setLastExportAction(this);
                context.addNotification("Exported " + file.getName());
            } catch (Exception e) {
                context.addNotification("Export FAILED!");
                e.printStackTrace();
            }
        }
    }

    protected boolean isDirectoryAction() {
        return false;
    }

    public void exportHeadless() throws Exception {
        if(lastfile != null) {
            if(context.getDocument() instanceof SketchDocument) {
                export(lastfile, (SketchDocument) context.getDocument());
            }
            if(context.getDocument() instanceof PixelDocument) {
                export(lastfile, (PixelDocument) context.getDocument());
            }
        }
    }


    protected abstract String getStandardFileExtension();

    protected abstract void export(File file, SketchDocument document) throws Exception;
    protected void export(File lastfile, PixelDocument document) throws Exception { }
}
