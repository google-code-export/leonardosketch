package org.joshy.sketch.actions;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.util.u;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

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
            export(file);
        }
    }

    private void export(File file) {
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
            ExportProcessor.process(new SVGExport(), out, (SketchDocument)context.getDocument());
            out.close();
        } catch (Exception ex) {
            u.p(ex);
        }
    }


    private void draw(PrintWriter out, SRect rect) {
        out.println("<rect x='"+ rect.getX() +"' y='"+ rect.getY() +"' width='"+ rect.getWidth() +"' height='"+ rect.getHeight() +"'"+
                " fill='"+toRGBString(rect.getFillPaint())+"'"+
                " stroke='"+toRGBString(rect.getStrokePaint())+"'" +
                " stroke-width='"+rect.getStrokeWidth()+"'" +
                "/>");
    }

    private void draw(PrintWriter out, SOval oval) {
        out.println("<ellipse");
        out.println("    cx='"+(oval.getX() + oval.getWidth() /2)+"'");
        out.println("    cy='"+(oval.getY() + oval.getHeight() /2)+"'");
        out.println("    rx='"+ oval.getWidth() /2+"'");
        out.println("    ry='"+ oval.getHeight() /2+"'");
        out.println("    fill='"+toRGBString(oval.getFillPaint())+"'");
        out.println("    stroke='"+toRGBString(oval.getStrokePaint())+"'");
        out.println("    stroke-width='"+oval.getStrokeWidth()+"'");
        out.println("/>");
    }

    private void draw(PrintWriter out, SText text) {
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

    private void draw(PrintWriter out, SPoly poly) {
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

    private String toRGBString(Paint paint) {
        if(paint instanceof FlatColor){
            FlatColor color = (FlatColor) paint;
            return "rgb("+color.getRed()*100+"%,"+color.getGreen()*100+"%,"+color.getBlue()*100+"%)";
        } else {
            return toRGBString(FlatColor.BLACK);
        }
    }

    private class SVGExport implements ShapeExporter<PrintWriter> {
        public void docStart(PrintWriter out, SketchDocument doc) {
            out.println("<?xml version=\"1.0\"?>");
            out.println("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.2\" baseProfile=\"tiny\" ");
            out.println(" viewBox=\"0 0 500 500\">");
        }

        public void pageStart(PrintWriter out, SketchDocument.SketchPage page) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void exportPre(PrintWriter out, SNode shape) {
            if(shape instanceof SRect) draw(out,(SRect)shape);
            if(shape instanceof SOval) draw(out,(SOval)shape);
            if(shape instanceof SText) draw(out,(SText)shape);
            if(shape instanceof SPoly) draw(out,(SPoly)shape);
        }

        public void exportPost(PrintWriter out, SNode shape) {
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
