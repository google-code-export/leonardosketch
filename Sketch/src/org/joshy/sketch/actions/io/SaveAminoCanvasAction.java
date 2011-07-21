package org.joshy.sketch.actions.io;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.LinearGradientFill;
import org.joshy.gfx.draw.MultiGradientFill;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Oct 30, 2010
 * Time: 5:32:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class SaveAminoCanvasAction extends SAction {
    private DocContext context;

    public SaveAminoCanvasAction(DocContext context) {
        this.context = context;
    }

    private static String HTML_CANVAS_PATH_KEY = "export.htmlcanvas.path";

    @Override
    public void execute() throws Exception {
        File file = null;
        Map props = context.getDocument().getProperties();
        if(props.containsKey(HTML_CANVAS_PATH_KEY)) {
            File ffile = new File((String)props.get(HTML_CANVAS_PATH_KEY));
            if(ffile.exists()) {
                file = ffile;
            }
        }

        if(file == null) {
            String extension = ".html";
            FileDialog fd = new FileDialog((Frame)context.getStage().getNativeWindow());
            fd.setMode(FileDialog.SAVE);
            fd.setTitle("Export to Amino Canvas");
            fd.setVisible(true);
            //cancel
            if(fd.getFile() == null) return;

            String fileName = fd.getFile();
            if(!fileName.toLowerCase().endsWith(extension)) {
                fileName = fileName + extension;
            }
            file = new File(fd.getDirectory(),fileName);
        }

        ExportProcessor.process(new AminoExport(),
                new IndentWriter(new PrintWriter(new FileOutputStream(file))),
                (SketchDocument) context.getDocument());
        context.getDocument().setStringProperty(HTML_CANVAS_PATH_KEY,file.getAbsolutePath());
        OSUtil.openBrowser(file.toURI().toASCIIString());
    }

    private static class AminoExport implements ShapeExporter<IndentWriter> {
        private DecimalFormat df;

        private AminoExport() {
            this.df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
        }

        public void docStart(IndentWriter out, SketchDocument doc) {
            out.println("<html><head><title>Amino Canvas Export</title>"
                    +"<script src='http://goamino.org/download/daily/amino-1.0b2.js'></script>"
                    +"<style type='text/css'>\n"
                    +"  body { background-color: " + serializeFlatColor(doc.getBackgroundFill()) + "; }\n"
                    +"</style>\n"
                    +"</head>");
            out.println("<body onload=\"setupDrawing();\">");
            out.println("<canvas width=\"800\" height=\"600\" id=\"foo\"></canvas>");
            out.println("<script>");

            //export named elements
            exportNamedElements(out,doc);

            //function definition
            out.println("function setupDrawing(){");
            out.println("var runner =  new Runner();\n"
                    +"runner.setCanvas(document.getElementById('foo'));\n"
                    +"runner.setFPS(30);\n"
                    +"runner.setBackground("+serializePaint(doc.getBackgroundFill())+");\n"
                    +"runner.setRoot(new Group()\n"
            );
            //export unnamed elements
        }

        private void exportNamedElements(IndentWriter out, SketchDocument document) {
            for(SketchDocument.SketchPage page : document.getPages()) {
                for(SNode node : page.getNodes()) {
                    exportNamedElements(out,node);
                }
            }
        }
        private void exportNamedElements(IndentWriter out, SNode node) {
            String id = node.getId();
            if(id != null && !id.equals("")) {
                out.println("var " + id + " = ");
                //do pre
                out.mode = "named";
                exportPre(out,node);
                //do post
                exportPost(out,node);
                //finish the statement
                out.mode = "";
                out.println(";");
            }
            if(node instanceof SGroup) {
                SGroup group = (SGroup) node;
                for(SNode nd : group.getNodes()) {
                    exportNamedElements(out,nd);
                }
            }
        }

        public void docEnd(IndentWriter out, SketchDocument document) {
            out.println(");");
            out.println("runner.start();");
            out.println("}");
            out.println("</script>");
            out.println("</body>");
            out.println("</html>");
            out.close();
        }

        public void pageStart(IndentWriter out, SketchDocument.SketchPage page) {
        }

        public void exportPre(IndentWriter out, SNode node) {
            u.p("exporting pre: " + node);
            if("".equals(out.mode)) {
                String id = node.getId();
                if(id != null && !id.equals("")) {
                    out.println(".add("+id);
                    return;
                } else {
                    out.println(".add(");
                }
            }


            out.indent();
            if(node instanceof SShape) {
                out.println("new Transform(");
                out.indent();
                if(node instanceof SOval) {
                    SOval n = (SOval) node;
                    out.println("new Ellipse().set("
                            +n.getX()
                            +","+n.getY()
                            +","+n.getWidth()
                            +","+n.getHeight()
                            +")");
                }
                if(node instanceof SRect) {
                    SRect n = (SRect) node;
                    out.println("new Rect().set("
                            +n.getX()
                            +","+n.getY()
                            +","+n.getWidth()
                            +","+n.getHeight()
                            +")");
                }
                if(node instanceof NGon) {
                    NGon n = (NGon) node;
                    toPathNode(out, n.toArea());
                }
                if(node instanceof SArea) {
                    SArea n = (SArea) node;
                    toPathNode(out, n.toArea());
                }
                if(node instanceof SPoly) {
                    SPoly n = (SPoly) node;
                    toPathNode(out, n.toArea());
                }
                if(node instanceof SPath) {
                    SPath n = (SPath) node;
                    //toPathNode(out, n.toArea());
                    serializePath(out,n);
                }
                SShape shape = (SShape) node;
                out.indent();
                out.println(".setStrokeWidth(" + shape.getStrokeWidth() + ")");
                out.println(".setStroke("+ serializePaint(shape.getStrokePaint()) + ")");
                out.println(".setFill("+ serializePaint(shape.getFillPaint())+")");
                if(shape.getFillOpacity() != 1.0) {
                    out.println(".setOpacity("+df.format(shape.getFillOpacity())+")");
                }
                if(node.getBooleanProperty("com.joshondesign.amino.nodecache")) {
                    out.println(".setCached(true)");
                }
                out.println(")");
                out.outdent();
                out.outdent();
                if(node instanceof NGon) return;
                if(node instanceof SArea) return;
                out.println(".setTranslateX(" + shape.getTranslateX() + ").setTranslateY(" + shape.getTranslateY() + ")");
            }
            if(node instanceof SGroup) {
                SGroup n = (SGroup) node;
                out.println("new Group().setX("+n.getTranslateX()+").setY("+n.getTranslateY()+")");
                if(node.getBooleanProperty("com.joshondesign.amino.nodecache")) {
                    out.println(".setCached(true)");
                }
                out.indent();
            }
            if(node.getBooleanProperty("com.joshondesign.amino.nodecache")) {
                out.println(".setCached(true)");
            }
        }

        private void serializePath(IndentWriter out, SPath path) {
            out.println("new PathNode()");
            out.indent();
            out.println(".setPath(");
            out.indent();
            out.println("new Path()");
            out.indent();
            Path2D.Double j2dpath = SPath.toPath(path);
            PathIterator it = j2dpath.getPathIterator(null);
            double dx = 0;
            double dy = 0;
            while(!it.isDone()) {
                double[] coords = new double[6];
                int n = it.currentSegment(coords);
                if(n == PathIterator.SEG_MOVETO) {
                    out.println(".moveTo("+(coords[0]-dx)+","+(coords[1]-dy)+")");
                }
                if(n == PathIterator.SEG_LINETO) {
                    out.println(".lineTo("+(coords[0]-dx)+","+(coords[1]-dy)+")");
                }
                if(n == PathIterator.SEG_CUBICTO) {
                    out.println(".curveTo("+
                            (coords[0]-dx)+","+(coords[1]-dy)+","+(coords[2]-dx)+","+(coords[3]-dy)+
                            ","+(coords[4]-dx)+","+(coords[5]-dy)+")"
                    );
                }
                if(n == PathIterator.SEG_CLOSE) {
                    out.println(".closeTo()");
                    break;
                }
                it.next();
            }
            out.println(".build()");
            out.outdent();
            out.outdent();
            out.println(")");
        }

        private void toPathNode(IndentWriter out, Area area) {
            out.println("new PathNode()");
            out.indent();
            out.println(".setPath(");
            out.indent();
            out.println("new Path()");
            out.indent();
            Rectangle2D bounds = area.getBounds2D();
            double dx = bounds.getX();
            double dy = bounds.getY();
            //don't subtract off the translation just yet
            dx = 0;
            dy = 0;
            PathIterator it = area.getPathIterator(null);
            while(!it.isDone()) {
                double[] coords = new double[6];
                int n = it.currentSegment(coords);
                if(n == PathIterator.SEG_MOVETO) {
                    out.println(".moveTo("+(coords[0]-dx)+","+(coords[1]-dy)+")");
                }
                if(n == PathIterator.SEG_LINETO) {
                    out.println(".lineTo("+(coords[0]-dx)+","+(coords[1]-dy)+")");
                }
                if(n == PathIterator.SEG_CUBICTO) {
                    out.println(".curveTo("+
                            (coords[0]-dx)+","+(coords[1]-dy)+","+(coords[2]-dx)+","+(coords[3]-dy)+
                            ","+(coords[4]-dx)+","+(coords[5]-dy)+")"
                    );
                }
                if(n == PathIterator.SEG_CLOSE) {
                    out.println(".closeTo()");
                    break;
                }
                it.next();
            }
            out.println(".build()");
            out.outdent();
            out.outdent();
            out.println(")");

        }

        private String toHexString(FlatColor color) {
            return "#"+String.format("%06x",color.getRGBA()&0x00FFFFFF);
        }

        private String serializeFlatColor(FlatColor color) {
            return "rgba("
                    +(int)(255*color.getRed())
                    +","+(int)(255*color.getGreen())
                    +","+(int)(255*color.getBlue())
                    +","+(int)(255*color.getAlpha())
                    +")"
                    ;
        }
        private String serializePaint(org.joshy.gfx.draw.Paint fillPaint) {
            if(fillPaint instanceof FlatColor) {
                FlatColor color = (FlatColor) fillPaint;
                return "'"+serializeFlatColor(color) + "'";
            }
            if(fillPaint instanceof LinearGradientFill) {
                LinearGradientFill grad = (LinearGradientFill) fillPaint;
                StringBuffer sb = new StringBuffer();
                sb.append("new LinearGradientFill("
                        +grad.getStartX()
                        +","+grad.getStartY()
                        +","+grad.getEndX()
                        +","+grad.getEndY()
                        +")"
                );
                for(MultiGradientFill.Stop stop : grad.getStops()) {
                    sb.append("\n.addStop("+df.format(stop.getPosition())
                            +",'"+serializeFlatColor(stop.getColor())+"'"
                            +")");
                }
                return sb.toString();
            }
            return "";
        }

        public void exportPost(IndentWriter out, SNode node) {
            if(node instanceof SGroup) {
                out.outdent();
            }
            if("".equals(out.mode)) {
                out.println(")");
            }
            out.outdent();
        }

        public void pageEnd(IndentWriter out, SketchDocument.SketchPage page) {
        }


        public boolean isContainer(SNode n) {
            if(n instanceof SGroup) return true;
            return false;
        }

        public Iterable<? extends SNode> getChildNodes(SNode n) {
            if(n instanceof SGroup) {
                SGroup sg = (SGroup) n;
                return sg.getNodes();
            }
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private class IndentWriter {
        private PrintWriter writer;
        private int tab = 0;
        private String tabString = "";
        public String mode = "";

        public IndentWriter(PrintWriter printWriter) {
            this.writer = printWriter;
        }

        public void println(String s) {
            this.writer.print(tabString);
            this.writer.println(s);
        }

        public void close() {
            this.writer.close();
        }

        public void indent() {
            tab++;
            calcTabString();
        }

        public void outdent() {
            tab--;
            calcTabString();
        }

        private void calcTabString() {
            tabString = "";
            for(int i=0; i<tab; i++) {
                tabString += "  ";
            }
        }
    }
}

