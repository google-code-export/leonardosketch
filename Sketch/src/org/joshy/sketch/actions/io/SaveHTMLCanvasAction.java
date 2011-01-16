package org.joshy.sketch.actions.io;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
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
public class SaveHTMLCanvasAction extends SAction {
    private DocContext context;

    public SaveHTMLCanvasAction(DocContext context) {
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
            fd.setTitle("Export to Canvas");
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
                new PrintWriter(new FileOutputStream(file)),
                (SketchDocument) context.getDocument());
        context.getDocument().setStringProperty(HTML_CANVAS_PATH_KEY,file.getAbsolutePath());
        OSUtil.openBrowser(file.toURI().toASCIIString());
    }

    private static class HTMLCanvasExport implements ShapeExporter<PrintWriter> {

        public void docStart(PrintWriter out, SketchDocument doc) {
            out.println("<html><head><title>Canvas Export</title></head>");
            out.println("<body onload=\"draw();\">");
            out.println("<canvas width=\"800\" height=\"600\" id=\"foo\"></canvas>");
            out.println("<script>");
            out.println("function ellipse(ctx, aX, aY, aWidth, aHeight){\n" +
                    "        var hB = (aWidth / 2) * .5522848,\n" +
                    "            vB = (aHeight / 2) * .5522848,\n" +
                    "            eX = aX + aWidth,\n" +
                    "            eY = aY + aHeight,\n" +
                    "            mX = aX + aWidth / 2,\n" +
                    "            mY = aY + aHeight / 2;\n" +
                    "        ctx.moveTo(aX, mY);\n" +
                    "        ctx.bezierCurveTo(aX, mY - vB, mX - hB, aY, mX, aY);\n" +
                    "        ctx.bezierCurveTo(mX + hB, aY, eX, mY - vB, eX, mY);\n" +
                    "        ctx.bezierCurveTo(eX, mY + vB, mX + hB, eY, mX, eY);\n" +
                    "        ctx.bezierCurveTo(mX - hB, eY, aX, mY + vB, aX, mY);\n" +
                    "        ctx.closePath();\n" +
                    "    }");
            out.println("function draw(){");
            out.println("var canvas =  document.getElementById('foo');");
            out.println("var ctx =  canvas.getContext('2d');");
        }

        public void pageStart(PrintWriter out, SketchDocument.SketchPage page) {
        }

        public void exportPre(PrintWriter out, SNode node) {
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
            }
        }

        private String toHexString(FlatColor color) {
            return "#"+String.format("%06x",color.getRGBA()&0x00FFFFFF);
        }

        private String serialize(Paint fillPaint) {
            if(fillPaint instanceof FlatColor) {
                FlatColor color = (FlatColor) fillPaint;
                return (int)(255*color.getRed())+","+(int)(255*color.getGreen())+","+(int)(255*color.getBlue());
            }
            return "";
        }

        public void exportPost(PrintWriter out, SNode shape) {
        }

        public void pageEnd(PrintWriter out, SketchDocument.SketchPage page) {
        }

        public void docEnd(PrintWriter out, SketchDocument document) {
            out.println("}");
            out.println("</script>");
            out.println("</body>");
            out.println("</html>");
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
