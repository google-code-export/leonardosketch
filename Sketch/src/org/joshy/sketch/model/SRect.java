package org.joshy.sketch.model;

import org.joshy.gfx.draw.*;
import org.joshy.sketch.canvas.SketchCanvas;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.Area;
import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Apr 6, 2010
 * Time: 12:36:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class SRect extends AbstractResizeableNode implements SelfDrawable {
    private double corner = 0;
    private double opacity = -1;

    public SRect(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    public SRect() {
        super(0,0,100,100);
    }

    public void setCorner(double corner) {
        this.corner = corner;
        fireUpdate();
    }


    public double getCorner() {
        return corner;
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new SRect();
        }
        ((SRect)dupe).setCorner(this.getCorner());
        return super.duplicate(dupe);
    }

    @Override
    public Area toArea() {
        return new Area(
                transformShape(new java.awt.Rectangle.Double(
                    getX(),
                    getY(),
                    getWidth(),getHeight()
            )));
    }

    @Override
    protected void fillShape(GFX g) {
        if(this.getCorner() > 0) {
            g.fillRoundRect(0,0,this.getWidth(),this.getHeight(),this.getCorner(),this.getCorner());
        } else {
            g.fillRect(0,0, this.getWidth(), this.getHeight());
        }
    }

    @Override
    public SPath toPath() {
        SPath path = new SPath();

        double x = this.getX();
        double y = this.getY();
        double w = this.getWidth();
        double h = this.getHeight();
        double c = this.getCorner()/2;
        double in = corner*0.265;
        if(getCorner() > 0) {
            //rounded rect
            SPath.PathPoint pt = path.moveTo(x, y + c);
            pt = path.curveTo(pt,x,y+c-in,   x+c-in,y,   x+c,y);
            pt = path.lineTo(x+w-c,y);
            pt = path.curveTo(pt, x+w-c+in, y,  x+w,y+c-in, x+w,y+c);
            pt = path.lineTo(x+w,y+h-c);
            pt = path.curveTo(pt, x+w,y+h-c+in, x+w-c+in,y+h, x+w-c,y+h);
            pt = path.lineTo(x+c,y+h);
            path.curveTo(pt, x+c-in, y+h, x,y+h-c+in, x,y+h-c);
            path.close();
        } else {
            //regular rect
            path.moveTo(this.getX(),this.getY());
            path.lineTo(this.getX()+this.getWidth(),this.getY());
            path.lineTo(this.getX()+this.getWidth(),this.getY()+this.getHeight());
            path.lineTo(this.getX(),this.getY()+this.getHeight());
            path.close();
        }
        path.setTranslateX(this.getTranslateX());
        path.setTranslateY(this.getTranslateY());
        path.setFillPaint(this.getFillPaint());
        path.setFillOpacity(this.getFillOpacity());
        path.setStrokeWidth(this.getStrokeWidth());
        path.setStrokePaint(this.getStrokePaint());
        return path;
    }

    public void draw(GFX g) {
        g.translate(this.getX(),this.getY());

        drawShadow(g);
        initPaint(g);
        fillShape(g);

        if(this.getStrokePaint() != null && getStrokeWidth() > 0) {
            g.setPaint(this.getStrokePaint());
            g.setStrokeWidth(this.getStrokeWidth());
            if(this.getCorner() > 0) {
                g.drawRoundRect(0,0, this.getWidth(), this.getHeight(),this.getCorner(),this.getCorner());
            } else {
                g.drawRect(0,0, this.getWidth(), this.getHeight());
            }
        }
        g.setStrokeWidth(1);
        uninitPaint(g);
        g.translate(-this.getX(),-this.getY());
    }

    private void uninitPaint(GFX g) {
        if(opacity >= 0) {
            g.setOpacity(opacity);
        }
    }

    protected void initPaint(GFX g) {
        Paint paint = this.getFillPaint();
        if(paint != null) {
            if(paint instanceof FlatColor) {
                g.setPaint(((FlatColor)paint).deriveWithAlpha(getFillOpacity()));
            }
            if(paint instanceof MultiGradientFill) {
                g.setPaint(paint);
            }
            if(paint instanceof PatternPaint) {
                opacity = g.getOpacity();
                g.setOpacity(getFillOpacity());
                g.setPaint(paint);
            }
        }
    }


    public static class RoundRectMasterHandle extends Handle {
        private SRect rect;

        public RoundRectMasterHandle(SRect rect) {
            this.rect = rect;
        }

        @Override
        public double getX() {
            return rect.getX()+10 + rect.getTranslateX() + this.rect.getCorner()/2;
        }

        @Override
        public void setX(double x, boolean constrain) {
            double d = x - rect.getX()-rect.getTranslateX()-10;
            d = d *2;
            if(d < 0) d = 0;
            rect.setCorner(d);
        }

        @Override
        public double getY() {
            return rect.getY()+10 + rect.getTranslateY() + this.rect.getCorner()/2;
        }

        @Override
        public void setY(double y, boolean constrain) {

        }

        @Override
        public void draw(GFX g, SketchCanvas sketchCanvas) {
            Point2D pt = new Point2D.Double(getX(),getY());
            pt = sketchCanvas.transformToDrawing(pt);

            Point2D top = new Point2D.Double(getX(),rect.getY()+rect.getTranslateY());
            top = sketchCanvas.transformToDrawing(top);
            Point2D left = new Point2D.Double(rect.getX()+rect.getTranslateX(),getY());
            left = sketchCanvas.transformToDrawing(left);

            g.setPaint(FlatColor.GREEN);
            double x = pt.getX();
            double y = pt.getY();
            //g.fillOval(getX()-5,getY()-5,10,10);
            g.drawLine(x-10,y-10,left.getX(),y-10);
            g.drawLine(x-10,y-10,x-10,top.getY());
            g.drawLine(x-10,y-10,x,y);
            DrawUtils.drawStandardHandle(g,x,y,FlatColor.GREEN);
        }
    }

}
