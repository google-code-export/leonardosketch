package org.joshy.sketch.actions.io;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 7, 2010
 * Time: 8:03:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class SaveHTMLAction extends SAction {
    private DocContext context;

    public SaveHTMLAction(DocContext context) {
        this.context = context;
    }

    @Override
    public void execute() {
        File dir = new File("foo");
        ExportProcessor.process(new HTMLExport(), new MultiFileOutput(dir), ((SketchDocument) context.getDocument()));
        OSUtil.openBrowser(new File(dir,"page0.html").toURI().toASCIIString());
    }

    public static void export(File file, SketchDocument doc) {
        ExportProcessor.process(new HTMLExport(), new MultiFileOutput(file), doc);
    }

    private static class HTMLExport implements ShapeExporter<MultiFileOutput> {
        public void docStart(MultiFileOutput out, SketchDocument doc) {
            out.dir.mkdir();
        }

        public void pageStart(MultiFileOutput out, SketchDocument.SketchPage page) {
            out.startPage();
            PrintWriter w = out.getWriter();
            w.println("<html>");
            w.println("<head>");
            w.println("<style type='text/css'>");
            w.println("body { background-color: "+toText(page.getDocument().getBackgroundFill())+";}");
            w.println("</style>");
            w.println("</head>");
            w.println("<head><title>slide</title></head>");
            w.println("<body>");
        }

        public void exportPre(MultiFileOutput out, SNode shape) {
            if(shape instanceof SText) {
                SText text = (SText) shape;
                out.getWriter().println("<p style='position: absolute;"
                        + " left:" + (shape.getTranslateX()+text.getX())+"px;"
                        + " top:"  + (shape.getTranslateY()+text.getY())+"px;"
                        + " font-size:"+text.getFontSize()+"pt;"
                        //+ " border: 1px solid red;"
                        + " color:" + toText(text.getFillPaint()) + ";"
                        + " margin: 0px;"
                        +"'>"+((SText)shape).getText()+"</p>");
            } else {
                renderShape(out,shape);
            }
        }

        private String toText(Paint fillPaint) {
            if(fillPaint instanceof FlatColor) {
                FlatColor color = (FlatColor) fillPaint;
                return "rgb("+ (int)(color.getRed()*255)
                        + ","+ (int)(color.getGreen()*255)
                        + ","+ (int)(color.getBlue()*255)+")";
            }
            return "black";
        }

        private void renderShape(MultiFileOutput out, SNode shape) {
            List<SNode> nodes = new ArrayList<SNode>();
            nodes.add(shape);
            File imf = out.createImageFile();
            SavePNGAction.export(imf,nodes);
            double x = shape.getTranslateX();
            double y = shape.getTranslateY();
            if(shape instanceof SResizeableNode) {
                x += ((SResizeableNode) shape).getX();
                y += ((SResizeableNode) shape).getY();
            }
            out.getWriter().println("<img src='"+imf.getName()+"'"
                    +" style='position:absolute;"
                    +" left:"+x+"px;"
                    +" top:" +y+"px;"
                    +"'/>");

        }

        public void exportPost(MultiFileOutput out, SNode shape) {

        }

        public void pageEnd(MultiFileOutput out, SketchDocument.SketchPage page) {
            PrintWriter w = out.getWriter();
            int index = out.getPageIndex();
            w.println("<p id='nav'>");
            if(index > 0) {
                w.println("<a href='"+out.getPageFilename(index-1)+"'>&lt; prev</a>");
            }
            w.println(" page " + (index+1));
            if(index < page.getDocument().getPages().size()-1) {
                w.println("<a href='"+out.getPageFilename(index+1)+"'>next &gt;</a>");
            }
            w.println("</p>");
            w.println("</body>");
            w.println("</html>");
            out.endPage();
        }

        public void docEnd(MultiFileOutput out, SketchDocument document) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean isContainer(SNode n) {
            if(n instanceof SGroup) return true;
            if(n instanceof ResizableGrid9Shape) return true;
            return false;
        }

        public Iterable<? extends SNode> getChildNodes(SNode n) {
            if(n instanceof SGroup) return ((SGroup)n).getNodes();
            if(n instanceof ResizableGrid9Shape) return ((ResizableGrid9Shape)n).getNodes();
            return null;
        }
    }

    private static class MultiFileOutput {
        private File dir;
        private int pageIndex;
        private File currentFile;
        private PrintWriter currentWriter;
        private int imageIndex = 0;

        public MultiFileOutput(File file) {
            this.dir = file;
            pageIndex = 0;
        }

        public void startPage() {
            currentFile = new File(dir,"page"+pageIndex+".html");
            try {
                currentWriter = new PrintWriter(new FileOutputStream(currentFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        public PrintWriter getWriter() {
            return currentWriter;
        }

        public void endPage() {
            currentWriter.close();
            pageIndex++;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public String getPageFilename(int index) {
            return "page"+index+".html";
        }

        public File createImageFile() {
            imageIndex++;
            return new File(dir,"image"+imageIndex+".png");
        }
    }
}
