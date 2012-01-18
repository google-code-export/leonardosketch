package org.joshy.sketch.actions.io;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 7, 2010
 * Time: 8:03:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class SaveHTMLAction extends BaseExportAction {

    public SaveHTMLAction(DocContext context) {
        super(context);
    }

    @Override
    protected String getStandardFileExtension() {
        return "HTML";
    }

    @Override
    protected boolean isDirectoryAction() {
        return true;
    }

    public void export(File basedir, SketchDocument doc) {
        ExportProcessor.process(new HTMLExport(), new MultiFileOutput(basedir), doc);
        OSUtil.openBrowser(new File(basedir, "index.html").toURI().toASCIIString());
    }

    private static class HTMLExport implements ShapeExporter<MultiFileOutput> {
        private SketchDocument doc;

        public void docStart(MultiFileOutput out, SketchDocument doc) {
            this.doc = doc;
            out.dir.mkdir();
        }

        public void pageStart(MultiFileOutput out, SketchDocument.SketchPage page) {
            out.startPage();
            XMLWriter xml = out.getXML();
            xml.start("html")
                .start("head");
            xml.start("style")
                .attr("type", "text/css")
                .text("body { background-color: " + toText(page.getDocument().getBackgroundFill()) + ";}")
                .end();
            xml.start("title").text("slide").end();
            xml.end();//head
            xml.start("body");
        }

        public String getPageFilenameById(MultiFileOutput out, String linkTarget) {
            return out.getPageFilename(doc.getPageIndexById(linkTarget));
        }

        public void exportPre(MultiFileOutput out, SNode shape) {
            if(shape instanceof SText) {
                SText text = (SText) shape;
                out.getXML().start("p")
                        .attr("style","position: absolute;"
                        + " left:" + (shape.getTranslateX()+text.getX())+"px;"
                        + " top:"  + (shape.getTranslateY()+text.getY())+"px;"
                        + " font-size:"+text.getFontSize()+"pt;"
                        //+ " border: 1px solid red;"
                        + " color:" + toText(text.getFillPaint()) + ";"
                        + " margin: 0px;"
                                )
                        .text(((SText)shape).getText())
                        .end();
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
            SavePNGAction.exportFragment(imf, nodes);
            double x = shape.getTranslateX();
            double y = shape.getTranslateY();
            if(shape instanceof SResizeableNode) {
                x += ((SResizeableNode) shape).getX();
                y += ((SResizeableNode) shape).getY();
            }
            XMLWriter xml = out.getXML();
            if(shape.isLink()) {
                xml.start("a", "href", getPageFilenameById(out,shape.getLinkTarget()));
            }
            xml.start("img")
                    .attr("src",imf.getName())
                    .attr("style","position:absolute;"
                        +" left:"+x+"px;"
                        +" top:" +y+"px;"
                    ).end();
            if(shape.isLink()) {
                xml.end();//a
            }
        }

        public void exportPost(MultiFileOutput out, SNode shape) {

        }

        public void pageEnd(MultiFileOutput out, SketchDocument.SketchPage page) {
            XMLWriter xml = out.getXML();
            int index = out.getPageIndex();
            xml.start("p")
                    .attr("id", "nav")
                    ;
            if(index > 0) {
                xml.start("a","href",out.getPageFilename(index-1));
                xml.text("&lt; prev");
                xml.end();
            } else {
                xml.text("&lt; prev");
            }
            xml.text(" | page " + (index+1) + " | ");
            if(index < page.getDocument().getPages().size()-1) {
                xml.start("a")
                    .attr("href",out.getPageFilename(index+1))
                    .text("next &gt;")
                    .end();
            } else {
                xml.text("next &gt;");
            }
            xml.end();
            xml.end();//body
            xml.end();//html
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
        private int imageIndex = 0;
        private XMLWriter xml;

        public MultiFileOutput(File file) {
            this.dir = file;
            pageIndex = 0;
        }

        public void startPage() {
            currentFile = new File(dir,"page"+pageIndex+".html");
            if(pageIndex == 0) {
                currentFile = new File(dir,"index.html");
            }
            try {
                xml = new XMLWriter(currentFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public XMLWriter getXML() {
            return xml;
        }

        public void endPage() {
            xml.close();
            pageIndex++;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public String getPageFilename(int index) {
            if(index == 0) {
                return "index.html";
            }
            return "page"+index+".html";
        }        

        public File createImageFile() {
            imageIndex++;
            return new File(dir,"image"+imageIndex+".png");
        }

    }
}
