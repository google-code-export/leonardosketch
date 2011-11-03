package org.joshy.sketch.modes.preso;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.actions.io.SavePNGAction;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SResizeableNode;
import org.joshy.sketch.model.SText;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 9/30/11
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExportHTMLPresentationAction extends SAction {
    private VectorDocContext context;

    public ExportHTMLPresentationAction(VectorDocContext ctx) {
        this.context = ctx;
    }

    @Override
    public void execute() throws Exception {
        File basedir = Util.requestDirectory("Choose Output Directory:",context);
        if(basedir == null) return;

        File file = new File(basedir,"presentation.html");
        XMLWriter out = new XMLWriter(new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8")),file.toURI());
        ExportProcessor.process(new HTMLPresoExport(basedir), out, context.getDocument());
        OSUtil.openBrowser(file.toURI().toASCIIString());
    }

    private class HTMLPresoExport implements ShapeExporter<XMLWriter> {
        private File basedir;
        private DecimalFormat df;
        private DecimalFormat intFormat;
        private int imageIndex;
        private File resources;

        public HTMLPresoExport(File basedir) {
            this.basedir = basedir;
            df = new DecimalFormat();
            df.setMinimumIntegerDigits(2);
            df.setMaximumFractionDigits(2);
            intFormat = new DecimalFormat();
            intFormat.setMaximumFractionDigits(0);
        }

        public void docStart(XMLWriter out, SketchDocument doc) {
            out.start("html");
            out.start("head");
            out.start("title").text("a presentation crafted with Leonardo Sketch").end();
            resources = new File(basedir,"resources");
            resources.mkdir();
            SwitchTheme.PresoThemeAction theme = (SwitchTheme.PresoThemeAction) doc.getProperties().get("theme");
            if(theme != null) {
                theme.exportResources(out,resources);
            }
            out.end();
            out.start("body");
        }

        private File createImageFile() {
            imageIndex++;
            return new File(resources,"image"+imageIndex+".png");
        }

        public void pageStart(XMLWriter out, SketchDocument.SketchPage page) {
            out.start("div","class","page","style","position:relative;");
            for(SNode node : page.getNodes()) {
                if(node instanceof SText) {
                    SText text = (SText) node;
                    if(text.isBulleted()) {
                        String[] lines = text.getText().split("\n");
                        out.start("ul");
                        for(String line : lines) {
                            out.start("li").text(line).end();
                        }
                        out.end();
                        continue;
                    }
                    if("title".equals(text.getStringProperty("text-class"))) {
                        out.start("h1").text(text.getText()).end();
                        continue;
                    }
                    if("subtitle".equals(text.getStringProperty("text-class"))) {
                        out.start("h3").text(text.getText()).end();
                        continue;
                    }
                    if("header".equals(text.getStringProperty("text-class"))) {
                        out.start("h2").text(text.getText()).end();
                        continue;
                    }
                    //do other kinds of text
                    String style = "position: absolute;"
                            +" left:"+(node.getTranslateX()+text.getX())+"px;"
                            +" top:" +(node.getTranslateY()+text.getY())+"px;"
                            +" color:"+toHTMLColor(text.getFillPaint())+";"
                            +" font:"+toHTMLFont(text)+";"
                            ;
                    out.start("p")
                            .attr("style",style)
                            .text(text.getText())
                            .end();
                } else {
                    renderShape(out,node);
                }
            }
        }

        private void renderShape(XMLWriter out, SNode shape) {
            List<SNode> nodes = new ArrayList<SNode>();
            nodes.add(shape);
            File imf = createImageFile();
            SavePNGAction.exportFragment(imf, nodes);
            double x = shape.getTranslateX();
            double y = shape.getTranslateY();
            if(shape instanceof SResizeableNode) {
                x += ((SResizeableNode) shape).getX();
                y += ((SResizeableNode) shape).getY();
            }
            String style = "position:absolute;"
                    +"left:"+x+"px;"
                    +"top:"+y+"px;";
            out.start("img")
                    .attr("src",resources.getName()+"/"+imf.getName())
                    .attr("style",style);
            if(shape.getId() != null) {
                out.attr("id",shape.getId());
            }
            out.end();
            /*
            out.getWriter().println("<img src='"+imf.getName()+"'"
                    +" style='position:absolute;"
                    +" left:"+x+"px;"
                    +" top:" +y+"px;"
                    +"'/>");*/

        }


        private String toHTMLFont(SText text) {
            return
                    (text.getWeight()== Font.Weight.Bold?" bold ":"")
                    + (text.getStyle()== Font.Style.Italic?" italic ":"")
                    + " " + df.format(text.getFontSize())+"pt "
                    +" \""+text.getFontName()+"\"";
        }

        private String toHTMLColor(Paint fillPaint) {
            if(fillPaint instanceof FlatColor) {
                FlatColor c = (FlatColor) fillPaint;
                return "rgb("
                        +intFormat.format(c.getRed()*255)
                        +","+intFormat.format(c.getGreen()*255)
                        +","+intFormat.format(c.getBlue()*255)
                        +")";
            }
            return "black";
        }

        public void exportPre(XMLWriter out, SNode shape) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void exportPost(XMLWriter out, SNode shape) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void pageEnd(XMLWriter out, SketchDocument.SketchPage page) {
            out.end();
        }

        public void docEnd(XMLWriter out, SketchDocument document) {
            out.end();
            out.end();
            out.close();
        }

        public boolean isContainer(SNode n) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Iterable<? extends SNode> getChildNodes(SNode n) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
