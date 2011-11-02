package org.joshy.sketch.actions.io;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.stage.swing.SwingGFX;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.util.Util;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Sep 6, 2010
 * Time: 12:52:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class SavePDFAction extends BaseExportAction {

    public SavePDFAction(DocContext context) {
        super(context);
    }

    @Override
    protected String getStandardFileExtension() {
        return "pdf";
    }


    public void export(File file, SketchDocument doc) {
        try {
            Rectangle pageSize = new Rectangle((int)doc.getWidth(),(int)doc.getHeight());
            Document pdf = new Document(pageSize);
            PdfWriter writer = PdfWriter.getInstance(pdf, new FileOutputStream(file));
            pdf.addCreator("Leonardo Sketch");
            pdf.open();
            ExportProcessor.process(new PDFExporter(pdf), writer, doc);
            pdf.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private static class PDFExporter implements ShapeExporter<PdfWriter> {
        private PdfTemplate template;
        private PdfContentByte cb;
        private Graphics2D g;
        private Document pdf;
        private DefaultFontMapper mapper;

        public PDFExporter(Document pdf) {
            this.pdf = pdf;
        }

        public void docStart(PdfWriter out, SketchDocument doc) {
            mapper = new DefaultFontMapper();
            try {
                File tempdir = File.createTempFile("leonardosketch_pdfexport", "_tempdir");
                tempdir = new File(tempdir.getAbsolutePath()+"2");
                boolean ret = tempdir.mkdirs();
                for(String string : Main.getFontMap().keySet()) {
                    Font font = Main.getFontMap().get(string);
                    if(font.isCustom()) {
                        File fontfile = new File(tempdir, string +".ttf");
                        //u.p("exporting to font file = " + fontfile.getAbsolutePath());
                        Util.copyToFile(font.getInputStream(), fontfile);
                    }
                }
                int count = mapper.insertDirectory(tempdir.getAbsolutePath());
                //u.p("imported fonts = " + count);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void pageStart(PdfWriter out, SketchDocument.SketchPage page) {
            cb = out.getDirectContent();
            template = cb.createTemplate((int)page.getDocument().getWidth(), (int)page.getDocument().getHeight());
            g = template.createGraphics((int)page.getDocument().getWidth(), (int)page.getDocument().getHeight(),mapper);

            //fill in the background
            SwingGFX gfx = new SwingGFX(g);
            org.joshy.gfx.draw.Paint fill = page.getDocument().getBackgroundFill();
            if(fill != null) {
                gfx.setPaint(fill);
                gfx.fillRect(0,0,(int)page.getDocument().getWidth(), (int) page.getDocument().getHeight());
            }

            //draw everything on the page
            ExportProcessor.processFragment(new SavePNGAction.PNGExporter(), g, page.getNodes());
        }

        public void exportPre(PdfWriter out, SNode shape) {
        }

        public void exportPost(PdfWriter out, SNode shape) {
        }

        public void pageEnd(PdfWriter out, SketchDocument.SketchPage page) {
            g.dispose();
            cb.addTemplate(template, 0, 0);
            pdf.newPage();
        }

        public void docEnd(PdfWriter out, SketchDocument document) {
        }

        public boolean isContainer(SNode n) {
            return false;
        }

        public Iterable<? extends SNode> getChildNodes(SNode n) {
            return null;
        }
    }
}
