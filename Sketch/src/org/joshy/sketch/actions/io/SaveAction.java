package org.joshy.sketch.actions.io;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.PatternPaint;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.model.SImage;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.joshy.gfx.util.localization.Localization.getString;

public class SaveAction extends SAction {
    private boolean forceSaveAs;
    private DocContext context;
    private boolean useZip;


    public SaveAction(DocContext context, boolean forceSaveAs, boolean useZip) {
        this.context = context;
        this.forceSaveAs = forceSaveAs;
        this.useZip = useZip;
    }

    @Override
    public void execute() {
        u.p("saving a file");
        String extension = ".leo";
        if(useZip) {
            extension = ".leoz";
        }
        if(forceSaveAs || context.getDocument().getFile() == null ) {
            FileDialog fd = new FileDialog((Frame)context.getStage().getNativeWindow());
            fd.setMode(FileDialog.SAVE);
            fd.setTitle(getString("misc.saveFile").toString());
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
            context.main.addRecentFile(file);
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
        NativeExport export = new NativeExport();
        export.setDelayedImageWriting(true);
        ExportProcessor.process(export, outx, ((SketchDocument)context.getDocument()));
        outx.flush();
        out.closeEntry();

        List images = export.getDelayedImages();
        for(Object i : images) {

            if(i instanceof SImage) {
                SImage image = (SImage) i;
                ZipEntry ie = new ZipEntry(dir+"/"+image.getRelativeURL());
                out.putNextEntry(ie);
                ImageIO.write(image.getBufferedImage(),"png",out);
                out.flush();
                out.closeEntry();
            }
            if(i instanceof PatternPaint) {
                PatternPaint paint = (PatternPaint) i;
                u.p("saving the real pattern paint  with relative url " + paint.getRelativeURL());
                ZipEntry ie = new ZipEntry(dir+"/resources/"+ paint.getRelativeURL());
                out.putNextEntry(ie);
                ImageIO.write(paint.getImage(),"png",out);
                out.flush();
                out.closeEntry();
                /*
        BufferedImage img = pattern.getImage();
        URI baseURI = out.getBaseURI();
        u.p("document URI = " + baseURI);
        URI imageURI = baseURI.resolve("resources/"+pattern.getRelativeURL());
        u.p("image URI = " + imageURI);
        File imageFile = new File(imageURI);
        if(!imageFile.getParentFile().exists()) {
            imageFile.getParentFile().mkdir();
        }
        ImageIO.write(img,"png",imageFile);
        u.p("wrote out image");

                 */
            }
        }
        out.close();
    }

    public static void saveAsZip(File file, SketchDocument doc) throws IOException {
        saveAsZip(new FileOutputStream(file),file.getName(),file.toURI(),doc);
    }
    public static void saveAsZip(OutputStream fout, String fileName, URI fileURI, SketchDocument doc) throws IOException {
        ZipOutputStream out = new ZipOutputStream(fout);
        String dir = fileName.replace(".leoz", "");
        ZipEntry entry = new ZipEntry(dir+"/leo.xml");
        out.putNextEntry(entry);
        XMLWriter outx = new XMLWriter(new PrintWriter(new OutputStreamWriter(out,"UTF-8")),fileURI);
        NativeExport export = new NativeExport();
        export.setDelayedImageWriting(true);
        ExportProcessor.process(export, outx, doc);
        outx.flush();
        out.closeEntry();
        List images = export.getDelayedImages();
        for(Object image : images) {
            BufferedImage img = null;
            String relUrl = null;
            if(image instanceof SImage) {
                img = ((SImage)image).getBufferedImage();
                relUrl = ((SImage)image).getRelativeURL();
            }
            if(image instanceof PatternPaint) {
                img = ((PatternPaint)image).getImage();
                relUrl = ((PatternPaint)image).getRelativeURL();
            }
            u.p("saving delayed image: " + relUrl);
            ZipEntry ie = new ZipEntry(dir+"/resources/"+relUrl);
            u.p("entry = " + ie.getName());
            out.putNextEntry(ie);
            ImageIO.write(img,"png",out);
            out.flush();
            out.closeEntry();
        }
        out.close();
    }


}
