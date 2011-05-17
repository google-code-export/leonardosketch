package org.joshy.sketch.actions.io;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
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

        ExportProcessor.process(new HTMLCanvasExport(),
                new IndentWriter(new PrintWriter(new FileOutputStream(file))),
                (SketchDocument) context.getDocument());
        context.getDocument().setStringProperty(HTML_CANVAS_PATH_KEY,file.getAbsolutePath());
        OSUtil.openBrowser(file.toURI().toASCIIString());
    }

    private static class HTMLCanvasExport implements ShapeExporter<IndentWriter> {

        public void docStart(IndentWriter out, SketchDocument doc) {
            out.println("<html><head><title>Amino Canvas Export</title>"
                    +"<script src='http://goamino.org/download/daily/amino-0.01.js'></script>"
                    +"</head>");
            out.println("<body onload=\"setupDrawing();\">");
            out.println("<canvas width=\"800\" height=\"600\" id=\"foo\"></canvas>");
            out.println("<script>");
            out.println("function setupDrawing(){");
            out.println("var runner =  new Runner();\n"
                    +"runner.setCanvas(document.getElementById('foo'));\n"
                    +"runner.setFPS(30);\n"
                    +"runner.setBackground('white');\n"
                    +"runner.setRoot(new Group()\n"
            );
        }

        public void pageStart(IndentWriter out, SketchDocument.SketchPage page) {
        }

        public void exportPre(IndentWriter out, SNode node) {
            u.p("exporting pre: " + node);
            out.println(".add(");
            out.indent();
            if(node instanceof SShape) {
                out.println("new Transform(");
                if(node instanceof SOval) {
                    SOval n = (SOval) node;
                    out.println("  new Circle().set("+(n.getX()+n.getWidth()/2)+","+(n.getY()+n.getWidth()/2)+","+n.getWidth()/2+")");
                }
                if(node instanceof SRect) {
                    SRect n = (SRect) node;
                    out.println("  new Rect().set("+n.getX()+","+n.getY()+","+n.getWidth()+","+n.getHeight()+")");
                }

                SShape shape = (SShape) node;
                out.indent();
                out.println(".setStrokeWidth(" + shape.getStrokeWidth() + ")");
                out.println(".setFill('rgb("+serialize(shape.getFillPaint())+")')");
                out.println(")");
                out.outdent();
                out.println(".setTranslateX("+shape.getTranslateX()+").setTranslateY("+shape.getTranslateY()+")");
            }
            if(node instanceof SGroup) {
                SGroup n = (SGroup) node;
                out.println("new Group().setX("+n.getTranslateX()+").setY("+n.getTranslateY()+")");
                out.indent();
            }

            /*
            if(node instanceof SShape) {
                SShape shape = (SShape) node;

                out.println("\n//set the fill");
                //do the paints
                if(shape.getFillPaint() instanceof LinearGradientFill) {
                    LinearGradientFill grad = (LinearGradientFill) shape.getFillPaint();
                    out.println("var lingrad = ctx.createLinearGradient("
                            +grad.getStartX()+","
                            +grad.getStartY()+","
                            +grad.getEndX()+","
                            +grad.getEndY()+");");
                    for(MultiGradientFill.Stop stop : grad.getStops()) {
                        out.println("lingrad.addColorStop("+stop.getPosition()+",'"+toHexString(stop.getColor())+"');");
                    }
                    out.println("ctx.fillStyle = lingrad;");
                }
                if(shape.getFillPaint() instanceof RadialGradientFill) {
                    RadialGradientFill grad = (RadialGradientFill) shape.getFillPaint();
                    out.println("var radgrad = ctx.createRadialGradient("
                            +grad.getCenterX()+","
                            +grad.getCenterY()+","
                            +0+","
                            +grad.getCenterX()+","
                            +grad.getCenterY()+","
                            +grad.getRadius()+");");
                    for(MultiGradientFill.Stop stop : grad.getStops()) {
                        out.println("radgrad.addColorStop("+stop.getPosition()+",'"+toHexString(stop.getColor())+"');");
                    }
                    out.println("ctx.fillStyle = radgrad;");
                }
                if(shape.getFillPaint() instanceof FlatColor) {
                    out.println("ctx.fillStyle = \"rgb("+serialize(shape.getFillPaint())+");\"");
                }



                //do the shape fill
                if(shape instanceof SRect) {
                    out.println("\n//rectangle");
                    out.println("ctx.translate("+shape.getTranslateX()+","+shape.getTranslateY()+");");
                    SRect rect = (SRect) shape;
                    out.println("ctx.fillRect ("+rect.getX()+", "+rect.getY()+","+rect.getWidth()+","+rect.getHeight()+");");
                    if(rect.getStrokeWidth() > 0 && rect.getStrokePaint() != null) {
                        out.println("ctx.strokeStyle = \"rgb("+serialize(shape.getStrokePaint())+");\"");
                        out.println("ctx.lineWidth = " + shape.getStrokeWidth()+";");
                        out.println("ctx.strokeRect ("+rect.getX()+", "+rect.getY()+","+rect.getWidth()+","+rect.getHeight()+");");
                    }
                    out.println("ctx.translate(-"+shape.getTranslateX()+",-"+shape.getTranslateY()+");");
                }
                if(shape instanceof SOval) {
                    SOval oval = (SOval) shape;
                    out.println("\n//oval");
                    out.println("ctx.translate("+shape.getTranslateX()+","+shape.getTranslateY()+");");
                    out.println("ctx.beginPath();");
                    out.println("ellipse(ctx,"+oval.getX()+","+oval.getY()+","+oval.getWidth()+","+oval.getHeight()+");");
                    out.println("ctx.fill();");
                    if(oval.getStrokeWidth() > 0 && oval.getStrokePaint() != null) {
                        out.println("ctx.strokeStyle = \"rgb("+serialize(shape.getStrokePaint())+");\"");
                        out.println("ctx.lineWidth = " + shape.getStrokeWidth()+";");
                        out.println("ctx.stroke();");
                    }
                    out.println("ctx.translate(-"+shape.getTranslateX()+",-"+shape.getTranslateY()+");");
                }
                if(shape instanceof SPath || shape instanceof SArea || shape instanceof SPoly || shape instanceof NGon) {
                    if(shape instanceof SPath) out.println("\n//path");
                    if(shape instanceof SArea) out.println("\n//area");
                    if(shape instanceof SPoly) out.println("\n//polygon");
                    if(shape instanceof NGon) out.println("\n//n-gon");

                    Area area = shape.toArea();
                    Rectangle2D bounds = area.getBounds2D();
                    double dx = bounds.getX();
                    double dy = bounds.getY();
                    out.println("ctx.translate("+dx+","+dy+");");
                    out.println("ctx.beginPath()");

                    PathIterator it = area.getPathIterator(null);
                    while(!it.isDone()) {
                        double[] coords = new double[6];
                        int n = it.currentSegment(coords);
                        if(n == PathIterator.SEG_MOVETO) {
                            out.println("ctx.moveTo("+(coords[0]-dx)+","+(coords[1]-dy)+");");
                        }
                        if(n == PathIterator.SEG_LINETO) {
                            out.println("ctx.lineTo("+(coords[0]-dx)+","+(coords[1]-dy)+");");
                        }
                        if(n == PathIterator.SEG_CUBICTO) {
                            out.println("ctx.bezierCurveTo("+
                                    (coords[0]-dx)+","+(coords[1]-dy)+","+(coords[2]-dx)+","+(coords[3]-dy)+
                                    ","+(coords[4]-dx)+","+(coords[5]-dy)+");"
                            );
                        }
                        if(n == PathIterator.SEG_CLOSE) {
                            out.println("ctx.closePath();");
                            break;
                        }
                        it.next();
                    }

                    out.println("ctx.fill();");
                    if(shape.getStrokeWidth() > 0 && shape.getStrokePaint() != null) {
                        out.println("ctx.strokeStyle = \"rgb("+serialize(shape.getStrokePaint())+");\"");
                        out.println("ctx.lineWidth = " + shape.getStrokeWidth()+";");
                        out.println("ctx.stroke();");
                    }
                    out.println("ctx.translate("+(-dx)+","+(-dy)+");");
                }

                if(shape instanceof SText) {
                    out.println("\n//text");
                    out.println("ctx.translate("+shape.getTranslateX()+","+shape.getTranslateY()+");");
                    SText text = (SText) shape;
                    out.println("ctx.font = \"" + text.getFontSize()+"pt "+text.getFontName()+"\";");
                    out.println("ctx.fillText (\""+text.getText()+"\", "+text.getX()+","+(text.getY()+text.getHeight())+");");
                    out.println("ctx.translate("+(-shape.getTranslateX())+","+(-shape.getTranslateY())+");");
                }
            }*/
        }

        private String toHexString(FlatColor color) {
            return "#"+String.format("%06x",color.getRGBA()&0x00FFFFFF);
        }

        private String serialize(org.joshy.gfx.draw.Paint fillPaint) {
            if(fillPaint instanceof FlatColor) {
                FlatColor color = (FlatColor) fillPaint;
                return (int)(255*color.getRed())+","+(int)(255*color.getGreen())+","+(int)(255*color.getBlue());
            }
            return "";
        }

        public void exportPost(IndentWriter out, SNode node) {
            if(node instanceof SGroup) {
                out.outdent();
            }
            out.outdent();
            out.println(")");
        }

        public void pageEnd(IndentWriter out, SketchDocument.SketchPage page) {
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

