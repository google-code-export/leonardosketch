package org.joshy.sketch.actions.io;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;

import java.awt.*;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SaveAction extends SAction {
    private boolean forceSaveAs;
    private DocContext context;
    private boolean useZip;


    public SaveAction(DocContext context, boolean forceSaveAs) {
        this.context = context;
        this.forceSaveAs = forceSaveAs;
    }

    public SaveAction(DocContext context, boolean forceSaveAs, boolean useZip) {
        this.context = context;
        this.forceSaveAs = forceSaveAs;
        this.useZip = useZip;
    }

    @Override
    public void execute() {
        u.p("saving a file");
        String extension = ".sketchy";
        if(useZip) {
            extension = ".leoz";
        }
        if(forceSaveAs || context.getDocument().getFile() == null ) {
            FileDialog fd = new FileDialog((Frame)context.getStage().getNativeWindow());
            fd.setMode(FileDialog.SAVE);
            fd.setTitle("Save Sketchy File");
            fd.setVisible(true);
            if(fd.getFile() != null) {
                String fileName = fd.getFile();
                if(!fileName.toLowerCase().endsWith(extension)) {
                    fileName = fileName + extension;
                }
                File file = new File(fd.getDirectory(),fileName);
                save(file);
            }
        } else {
            save(context.getDocument().getFile());
        }
    }

    private void save(File file) {
        context.getUndoOverlay().showIndicator("Saving");
        try {
            if(useZip) {
                saveAsZip(file);
            } else {
                XMLWriter out = new XMLWriter(new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8")),file.toURI());
                ExportProcessor.process(new NativeExport(), out, ((SketchDocument)context.getDocument()));
                out.close();
            }
            context.getDocument().setFile(file);
            context.getDocument().setTitle(file.getName());
            context.getStage().setTitle(file.getName());
            context.getDocument().setDirty(false);
            context.main.rebuildWindowMenu();
        } catch (Exception ex) {
            u.p(ex);
        }
    }

    private void saveAsZip(File file) throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
        String dir = file.getName().replace(".leoz", "");
        ZipEntry entry = new ZipEntry(dir+"/leo.xml");
        out.putNextEntry(entry);
        XMLWriter outx = new XMLWriter(new PrintWriter(new OutputStreamWriter(out,"UTF-8")),file.toURI());
        ExportProcessor.process(new NativeExport(), outx, ((SketchDocument)context.getDocument()));
        outx.close();
        out.close();
    }


}
