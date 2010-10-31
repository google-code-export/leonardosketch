package org.joshy.sketch.actions.io;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;

import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

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

    @Override
    public void execute() throws Exception {
        File file = new File("foo.html");
        ExportProcessor.process(new HTMLCanvasExport(),
                new PrintWriter(new FileOutputStream(file)),
                (SketchDocument) context.getDocument());
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
                out.println("ctx.fillStyle = \"rgb("+serialize(shape.getFillPaint())+");\"");

                if(shape instanceof SRect) {
                    out.println("\n//rectangle");
                    out.println("ctx.translate("+shape.getTranslateX()+","+shape.getTranslateY()+");");
                    SRect rect = (SRect) shape;
                    out.println("ctx.fillRect ("+rect.getX()+", "+rect.getY()+","+rect.getWidth()+","+rect.getHeight()+");");
                    out.println("ctx.translate(-"+shape.getTranslateX()+",-"+shape.getTranslateY()+");");
                }
                if(shape instanceof SOval) {
                    SOval oval = (SOval) shape;
                    out.println("\n//oval");
                    out.println("ctx.translate("+shape.getTranslateX()+","+shape.getTranslateY()+");");
                    out.println("ctx.beginPath();");
                    out.println("ellipse(ctx,"+oval.getX()+","+oval.getY()+","+oval.getWidth()+","+oval.getHeight()+");");
                    out.println("ctx.fill();");
                    out.println("ctx.translate(-"+shape.getTranslateX()+",-"+shape.getTranslateY()+");");
                }
                if(shape instanceof SPath || shape instanceof SArea || shape instanceof SPoly || shape instanceof NGon) {
                    if(shape instanceof SPath) out.println("\n//path");
                    if(shape instanceof SArea) out.println("\n//area");
                    if(shape instanceof SPoly) out.println("\n//polygon");
                    if(shape instanceof NGon) out.println("\n//n-gon");
                    
                    out.println("ctx.beginPath()");

                    PathIterator it = shape.toArea().getPathIterator(null);
                    while(!it.isDone()) {
                        double[] coords = new double[6];
                        int n = it.currentSegment(coords);
                        if(n == PathIterator.SEG_MOVETO) {
                            out.println("ctx.moveTo("+coords[0]+","+coords[1]+");");
                        }
                        if(n == PathIterator.SEG_LINETO) {
                            out.println("ctx.lineTo("+coords[0]+","+coords[1]+");");
                        }
                        if(n == PathIterator.SEG_CUBICTO) {
                            out.println("ctx.bezierCurveTo("+
                                    coords[0]+","+coords[1]+","+coords[2]+","+coords[3]+
                                    ","+coords[4]+","+coords[5]+");"
                            );
                        }
                        if(n == PathIterator.SEG_CLOSE) {
                            out.println("ctx.closePath();");
                            break;
                        }
                        it.next();
                    }

                    out.println("ctx.fill();");
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
