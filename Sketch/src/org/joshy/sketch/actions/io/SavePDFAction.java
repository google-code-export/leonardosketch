package org.joshy.sketch.actions.io;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.PixelDocument;
import org.joshy.sketch.model.SNode;
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

            Rectangle pageSize = new Rectangle((int)doc.getWidth(),(int)doc.getHeight());
            Document pdf = new Document(pageSize);
            PdfWriter writer = PdfWriter.getInstance(pdf, new FileOutputStream(file));
            pdf.addCreator("Leonardo Sketch");
            pdf.open();
            ExportProcessor.process(new PDFExporter(pdf), writer, doc);
            pdf.close();
        } catch (DocumentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static class PDFExporter implements ShapeExporter<PdfWriter> {
        private PdfTemplate template;
        private PdfContentByte cb;
        private Graphics2D g;
        private Document pdf;

        public PDFExporter(Document pdf) {
            this.pdf = pdf;
        }

        public void docStart(PdfWriter out, SketchDocument doc) {
        }

        public void pageStart(PdfWriter out, SketchDocument.SketchPage page) {
            cb = out.getDirectContent();
            template = cb.createTemplate((int)page.getDocument().getWidth(), (int)page.getDocument().getHeight());
            g = template.createGraphics((int)page.getDocument().getWidth(), (int)page.getDocument().getHeight(),
                    new DefaultFontMapper());
            ExportProcessor.processFragment(new SavePNGAction.PNGExporter(), g, page.getNodes());
        }

        public void exportPre(PdfWriter out, SNode shape) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void exportPost(PdfWriter out, SNode shape) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void pageEnd(PdfWriter out, SketchDocument.SketchPage page) {
            g.dispose();
            cb.addTemplate(template, 0, 0);
            pdf.newPage();
        }

        public void docEnd(PdfWriter out, SketchDocument document) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean isContainer(SNode n) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Iterable<? extends SNode> getChildNodes(SNode n) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
