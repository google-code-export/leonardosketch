package org.joshy.sketch.actions.io;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ShapeExporter;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

public class SaveSVGAction extends SAction {
    private DocContext context;

    public SaveSVGAction(DocContext context) {
        this.context = context;
    }
    
    public void execute() {
        FileDialog fd = new FileDialog((Frame)context.getStage().getNativeWindow());
        fd.setMode(FileDialog.SAVE);
        fd.setTitle("Export SVG Image");
        File currentFile = context.getDocument().getFile();
        if(currentFile != null) {
            fd.setFile(currentFile.getName().substring(0,currentFile.getName().lastIndexOf('.'))+".svg");
        }
        fd.setVisible(true);
        if(fd.getFile() != null) {
            String fileName = fd.getFile();
            if(!fileName.toLowerCase().endsWith(".svg")) {
                fileName = fileName + ".svg";
            }
            File file = new File(fd.getDirectory(),fileName);
            export(file,(SketchDocument)context.getDocument());
        }
    }

    public static void export(File file, SketchDocument doc) {
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
            ExportProcessor.process(new SVGExport(), out, doc);
            out.close();
        } catch (Exception ex) {
            u.p(ex);
        }
    }


    private static void draw(PrintWriter out, SRect rect) {
        //String id = Math.random();
        String id = "A"+Long.toHexString(Double.doubleToLongBits(Math.random()));
        if(rect.getFillPaint() instanceof GradientFill) {
            GradientFill grad = (GradientFill) rect.getFillPaint();
            out.println("  <linearGradient id='"+id+"'>");
            out.println("    <stop offset='0.0' style='stop-color:"+toRGBString(grad.start)+"'/>");
            out.println("    <stop offset='1.0' style='stop-color:"+toRGBString(grad.end)+"'/>");
            out.println("  </linearGradient>");
        }

        if(rect.getFillPaint() instanceof LinearGradientFill) {
            LinearGradientFill grad = (LinearGradientFill) rect.getFillPaint();
            out.println("<g>");
            out.println("<defs>");
            out.println("  <linearGradient "
                    + "id='"+id+"'"
                    +" x1='"+grad.getStartX()
                    +"' y1='"+grad.getStartY()
                    +"' x2='"+grad.getEndX()
                    +"' y2='"+grad.getEndY()+"'>");
            for(MultiGradientFill.Stop stop : grad.getStops()) {
                out.println("    <stop offset='"+stop.getPosition()+"' stop-color='"+toHexString(stop.getColor())+"'/>");
            }
            out.println("  </linearGradient>");
            out.println("</defs>");
        }
        out.println("<rect x='"+ rect.getX() +"' y='"+ rect.getY() +"' width='"+ rect.getWidth() +"' height='"+ rect.getHeight() +"'"+
                " transform='translate("+rect.getTranslateX()+","+rect.getTranslateY()+")'");

        if(rect.getFillPaint() instanceof FlatColor) {
            out.println(" fill='"+toRGBString(rect.getFillPaint())+"'");
        }
        if(rect.getFillPaint() instanceof GradientFill) {
            out.println(" fill='url(#"+id+")'");
        }
        if(rect.getFillPaint() instanceof LinearGradientFill) {
            out.println(" fill='url(#"+id+")'");
        }

        out.println(" stroke='"+toRGBString(rect.getStrokePaint())+"'" +
                " stroke-width='"+rect.getStrokeWidth()+"'" +
                "/>");
        if(rect.getFillPaint() instanceof LinearGradientFill) {
            out.println("</g>");
        }
    }

    private static String toHexString(FlatColor color) {
        return "#"+Integer.toHexString(color.getRGBA()&0x00FFFFFF);
    }

    private static void draw(PrintWriter out, SOval oval) {
        out.println("<ellipse");
        out.println("    cx='"+(oval.getX() + oval.getWidth() /2)+"'");
        out.println("    cy='"+(oval.getY() + oval.getHeight() /2)+"'");
        out.println("    rx='"+ oval.getWidth() /2+"'");
        out.println("    ry='"+ oval.getHeight() /2+"'");
        out.println("    transform='translate("+oval.getTranslateX()+","+oval.getTranslateY()+")'");
        out.println("    fill='"+toRGBString(oval.getFillPaint())+"'");
        out.println("    stroke='"+toRGBString(oval.getStrokePaint())+"'");
        out.println("    stroke-width='"+oval.getStrokeWidth()+"'");
        out.println("/>");
    }

    private static void draw(PrintWriter out, SText text) {
        org.joshy.gfx.draw.Font font = org.joshy.gfx.draw.Font.DEFAULT;
        font = org.joshy.gfx.draw.Font.name(font.getName())
                .size((float)text.getFontSize())
                .weight(text.getWeight())
                .style(text.getStyle())
                .resolve();

        out.println("<text");
        out.println("    x='"+(text.getX() + text.getTranslateX())+"'");
        out.println("    y='"+(text.getY() + text.getTranslateY() + font.getAscender())+"'");
        out.println("    fill='"+toRGBString(text.getFillPaint())+"'");
        out.println("    font-family='"+font.getName()+"'");
        out.println("    font-size='"+text.getFontSize()+"'");
        out.print(">");
        out.print(text.getText());
        out.print("</text>");
    }

    private static void draw(PrintWriter out, SPoly poly) {
        if(poly.isClosed()) {
            out.println("<polygon ");
        } else {
            out.println("<polyline ");
        }
        out.print("    points='");
        for(int i=0; i<poly.pointCount(); i++) {
            out.print(""  + (poly.getPoint(i).getX() + poly.getTranslateX())
                    + "," + (poly.getPoint(i).getY() + poly.getTranslateY()) + " ");
        }
        out.println("'");

        if(poly.isClosed()) {
            out.println("    fill='"+toRGBString(poly.getFillPaint())+"'");
        } else {
            out.println("    fill='none'");
        }
        out.println("    stroke='"+toRGBString(poly.getStrokePaint())+"'");
        out.println("    stroke-width='"+poly.getStrokeWidth()+"'");
        
        out.println("/>");
    }

    private static void draw(PrintWriter out, NGon nGon) {
        out.println("<polygon ");

        //the vector data
        out.print("    points='");
        double[] points = nGon.toPoints();
        for(int i=0; i<points.length; i+=2) {
            out.print(""  + (points[i]+nGon.getTranslateX())
                    + "," + (points[i+1]+nGon.getTranslateY()) + " ");
        }
        out.println("'");


        out.println("    fill='"+toRGBString(nGon.getFillPaint())+"'");
        out.println("    stroke='"+toRGBString(nGon.getStrokePaint())+"'");
        out.println("    stroke-width='"+nGon.getStrokeWidth()+"'");
        out.println("/>");

    }

    private static void draw(PrintWriter out, SPath path) {
        out.println("<path ");

        //the translate
        out.println("    transform='translate("+path.getTranslateX()+","+path.getTranslateY()+")' ");
        //the vector data
        out.print("    d='");
        int count = 0;
        List<SPath.PathPoint> points = path.getPoints();
        for(int i=0; i<points.size(); i++) {
            if(i == 0) {
                out.print(" M "+points.get(i).x + " " + points.get(i).y);
            } else {
                out.print(" C "
                        +points.get(i-1).cx2 + " " + points.get(i-1).cy2 + " "
                        +points.get(i).cx1 + " " + points.get(i).cy1 + " "
                        +points.get(i).x + " " + points.get(i).y + " "
                        );
            }
            out.print(" ");
        }
        out.println(" z'");

        out.println("    fill='"+toRGBString(path.getFillPaint())+"'");
        out.println("    stroke='"+toRGBString(path.getStrokePaint())+"'");
        out.println("    stroke-width='"+path.getStrokeWidth()+"'");
        out.println("/>");
    }

    private static void draw(PrintWriter out, SArea sArea) {
        out.println("<path ");
        out.println("    transform='translate("+ sArea.getTranslateX()+","+ sArea.getTranslateY()+")' ");
        //the vector data
        out.print("    d='");
        int count = 0;
        Area area = sArea.toArea();
        PathIterator it = area.getPathIterator(new AffineTransform());
        while(!it.isDone()) {
            double[] coords = new double[6];
            int n = it.currentSegment(coords);
            if(n == PathIterator.SEG_MOVETO) {
                out.println(" M "+coords[0]+" "+coords[1]);
            }
            if(n == PathIterator.SEG_LINETO) {
                out.println(" L " + coords[0]+" " +coords[1]);
            }
            if(n == PathIterator.SEG_CUBICTO) {
                out.println(" C "
                        +coords[0]+" "+coords[1] + " "
                        +coords[2]+" "+coords[3] + " "
                        +coords[4]+" "+coords[5] + " "
                        );
            }
            if(n == PathIterator.SEG_CLOSE) {
                out.println(" z");
                break;
            }
            it.next();
        }
        out.println("'");
/*
        for(int i=0; i<points.size(); i++) {
            if(i == 0) {
                out.print(" M "+points.get(i).x + " " + points.get(i).y);
            } else {
                out.print(" C "
                        +points.get(i-1).cx2 + " " + points.get(i-1).cy2 + " "
                        +points.get(i).cx1 + " " + points.get(i).cy1 + " "
                        +points.get(i).x + " " + points.get(i).y + " "
                        );
            }
            out.print(" ");
        }*/
        //out.println(" z'");

        out.println("    fill='"+toRGBString(sArea.getFillPaint())+"'");
        out.println("    stroke='"+toRGBString(sArea.getStrokePaint())+"'");
        out.println("    stroke-width='"+ sArea.getStrokeWidth()+"'");
        out.println("/>");
    }

    private static String toRGBString(Paint paint) {
        if(paint instanceof FlatColor){
            FlatColor color = (FlatColor) paint;
            return "rgb("+color.getRed()*100+"%,"+color.getGreen()*100+"%,"+color.getBlue()*100+"%)";
        } else {
            return toRGBString(FlatColor.BLACK);
        }
    }

    private  static class SVGExport implements ShapeExporter<PrintWriter> {
        public void docStart(PrintWriter out, SketchDocument doc) {
            out.println("<?xml version=\"1.0\"?>");
            out.println("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.2\" baseProfile=\"tiny\" ");
            out.println(" viewBox=\"0 0 500 500\">");
        }

        public void pageStart(PrintWriter out, SketchDocument.SketchPage page) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void exportPre(PrintWriter out, SNode shape) {
            if(shape instanceof SGroup) {
                out.println("<g transform='translate("+shape.getTranslateX()+","+shape.getTranslateY()+")'>");
            }
            if(shape instanceof SRect) draw(out,(SRect)shape);
            if(shape instanceof SArea) draw(out,(SArea)shape);
            if(shape instanceof SOval) draw(out,(SOval)shape);
            if(shape instanceof SText) draw(out,(SText)shape);
            if(shape instanceof SPoly) draw(out,(SPoly)shape);
            if(shape instanceof NGon)  draw(out, (NGon)shape);
            if(shape instanceof SPath)  draw(out, (SPath)shape);
        }

        public void exportPost(PrintWriter out, SNode shape) {
            if(shape instanceof SGroup) out.println("</g>");
        }

        public void pageEnd(PrintWriter out, SketchDocument.SketchPage page) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void docEnd(PrintWriter out, SketchDocument document) {
            out.println("</svg>");
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

}
