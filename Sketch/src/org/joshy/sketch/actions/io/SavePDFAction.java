package org.joshy.sketch.actions.io;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import org.joshy.sketch.actions.SAction;
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

    @Override
    public void execute() {

    }
    private static class PDFContext {
        public PDFContext(File file) {

        }
    }
}
