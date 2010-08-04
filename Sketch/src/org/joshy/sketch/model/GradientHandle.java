package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.u;
import org.joshy.sketch.canvas.SketchCanvas;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 13, 2010
 * Time: 5:41:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class GradientHandle extends Handle {
    private SRect rect;
    private GradientPosition pos;
    private double x;
    private double y;

    public GradientHandle(SRect rect, GradientPosition pos) {
        super();
        this.rect = rect;
        GradientFill grad = (GradientFill) rect.getFillPaint();
        this.pos = pos;
        switch(pos) {
            case Start:
                this.x = grad.startX;
                this.y = grad.startY;
                break;
            case End:
                this.x = grad.endX;
                this.y = grad.endY;
                break;
        }
    }

    @Override
    public double getX() {
        return rect.getTranslateX()+rect.getX()+x;
    }

    @Override
    public void setX(double x, boolean constrain) {
        this.x = x - rect.getTranslateX() - rect.getX();
        snapX(0);
        snapX(rect.getWidth()/2);
        snapX(rect.getWidth());        
        updateGrad();
    }

    @Override
    public double getY() {
        return rect.getTranslateY()+rect.getY()+y;
    }

    @Override
    public void setY(double y, boolean constrain) {
        this.y = y-rect.getTranslateY()-rect.getY();
        snapY(0);
        snapY(rect.getHeight()/2);
        snapY(rect.getHeight());
        updateGrad();
    }

    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);

        double x = pt.getX();
        double y = pt.getY();
        double s = 6;
        g.setPaint(FlatColor.BLACK);
        g.fillRect(x-s,y-s,s*2,s*2);
        s = 5;
        g.setPaint(FlatColor.RED);
        g.fillRect(x-s,y-s,s*2,s*2);

        if(this.pos == GradientPosition.End) {
            Bounds bounds = rect.getBounds();
            GradientFill grad = (GradientFill) rect.getFillPaint();
            Point2D start = canvas.transformToDrawing(bounds.getX()+grad.startX,bounds.getY()+grad.startY);
            Point2D end = canvas.transformToDrawing(bounds.getX()+grad.endX,bounds.getY()+grad.endY);
            g.setPaint(FlatColor.BLACK);
            g.drawLine(start.getX(),start.getY(),end.getX(),end.getY());
            g.setPaint(FlatColor.WHITE);
            g.drawLine(start.getX()+1,start.getY()+1,end.getX()+1,end.getY()+1);
        }
    }

    private void snapX(double value) {
        if(Math.abs(this.x-value)<5) {
            this.x = value;
        }
    }
    private void snapY(double value) {
        if(Math.abs(this.y-value)<5) {
            this.y = value;
        }
    }

    private void updateGrad() {
        GradientFill grad = (GradientFill) rect.getFillPaint();
        switch (pos) {
            case Start:
                rect.setFillPaint(grad.derive(this.x,this.y,grad.endX,grad.endY));
                break;
            case End:
                rect.setFillPaint(grad.derive(grad.startX,grad.startY,this.x,this.y));
                break;
        }
    }

    public void setColor(FlatColor flatColor) {
        GradientFill grad = (GradientFill) rect.getFillPaint();
        switch (pos) {
            case Start: rect.setFillPaint(grad.derive(flatColor,grad.end)); break;
            case End: rect.setFillPaint(grad.derive(grad.start,flatColor)); break;
        }
    }

    public enum GradientPosition {
        End, Start
    }
}
