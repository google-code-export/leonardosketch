package org.joshy.sketch.actions.io;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.model.PixelDocument;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Sep 6, 2010
 * Time: 12:52:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class SavePDFAction extends SAction {
    private DocContext context;

    public SavePDFAction(DocContext context) {
        this.context = context;
    }

    public void execute() {
        FileDialog fd = new FileDialog((Frame)context.getStage().getNativeWindow());
        fd.setMode(FileDialog.SAVE);
        fd.setTitle("Export PDF Image");
        File currentFile = context.getDocument().getFile();
        if(currentFile != null) {
            fd.setFile(currentFile.getName().substring(0,currentFile.getName().lastIndexOf('.'))+".pdf");
        }
        fd.setVisible(true);
        if(fd.getFile() != null) {
            String fileName = fd.getFile();
            if(!fileName.toLowerCase().endsWith(".pdf")) {
                fileName = fileName + ".pdf";
            }
            File file = new File(fd.getDirectory(),fileName);
            if(context.getDocument() instanceof SketchDocument) {
                export(file, (SketchDocument) context.getDocument());
            }
            if(context.getDocument() instanceof PixelDocument) {
                //export(file, (PixelDocument) context.getDocument());
            }
        }
    }

    public static void export(File file, SketchDocument doc) {
        try {
            Rectangle pageSize = new Rectangle(500,500);
            Document pdf = new Document(pageSize);
            PdfWriter writer = PdfWriter.getInstance(pdf, new FileOutputStream(file));
            pdf.addCreator("Java FX Designer");
            pdf.open();
            PdfContentByte cb = writer.getDirectContent();

            PdfTemplate tmp = cb.createTemplate(500,500);
            Graphics2D g = tmp.createGraphics(500,500, new DefaultFontMapper());
            SavePNGAction.export(g,doc);
            //canvas.paintToExport(g);
            g.dispose();
            cb.addTemplate(tmp, 0, 0);
            pdf.close();
            PDFContext ctx = new PDFContext(file);
        } catch (DocumentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static class PDFContext {
        public PDFContext(File file) {

        }
    }
}
