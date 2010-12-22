package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.draw.Paint;
import org.joshy.sketch.canvas.SketchCanvas;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Apr 6, 2010
 * Time: 12:36:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class SRect extends AbstractResizeableNode implements SelfDrawable {
    private double corner = 0;
    private List<SRectUpdateListener> listeners = new ArrayList<SRectUpdateListener>();

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

    @Override
    public void setTranslateX(double translateX) {
        super.setTranslateX(translateX);
        fireUpdate();
    }

    @Override
    public void setTranslateY(double translateY) {
        super.setTranslateY(translateY);
        fireUpdate();
    }

    @Override
    public void setWidth(double width) {
        super.setWidth(width);
        rescaleGradient();
        fireUpdate();
    }

    @Override
    public void setHeight(double height) {
        super.setHeight(height);
        rescaleGradient();
        fireUpdate();
    }

    private void rescaleGradient() {
        if(getFillPaint() instanceof GradientFill) {
            GradientFill grad = (GradientFill) getFillPaint();
            if(grad.isStartSnapped() && grad.isEndSnapped()) {
                double sx = grad.getStartX();
                double sy = grad.getStartY();
                double ex = grad.getEndX();
                double ey = grad.getEndY();
                if(grad.getStartY() == 0)  sy = 0;
                if(grad.getStartY() > getHeight()/3 && grad.getStartY() < getHeight()/3*2) sy = getHeight()/2;
                if(grad.getStartY() > getHeight()/3*2) sy = getHeight();

                if(grad.getEndY() == 0) ey = 0;
                if(grad.getEndY() > getHeight()/3 && grad.getEndY() < getHeight()/3*2) ey = getHeight()/2;
                if(grad.getEndY() > getHeight()/3*2) ey = getHeight();

                if(grad.getStartX() == 0) sx = 0;
                if(grad.getStartX() > getWidth()/3 && grad.getStartY()<getWidth()/3*2) sx = getWidth()/2;
                if(grad.getStartX() > getWidth()/3*2) sx = getWidth();

                if(grad.getEndX() == 0) ex = 0;
                if(grad.getEndX() > getWidth()/3 && grad.getEndX()<getWidth()/3*2) ex = getWidth()/2;
                if(grad.getEndX() > getWidth()/3*2) ex = getWidth();

                grad = grad.derive(sx,sy,ex,ey);
                setFillPaint(grad);
            }
        }
    }

    private void fireUpdate() {
        if(listeners != null) {
            for(SRectUpdateListener c : listeners) {
                c.updated();
            }
        }
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
        return new Area(new java.awt.Rectangle.Double(
                getX()+getTranslateX(),
                getY()+getTranslateY(),
                getWidth(),getHeight()
        ));
    }

    public void draw(GFX g) {
        g.translate(this.getX(),this.getY());

        Paint paint = this.getFillPaint();
        if(paint != null) {
            if(paint instanceof FlatColor) {
                g.setPaint(((FlatColor)paint).deriveWithAlpha(getFillOpacity()));
            }
            if(paint instanceof GradientFill) {
                g.setPaint(paint);
            }
            if(this.getCorner() > 0) {
                g.fillRoundRect(0,0,this.getWidth(),this.getHeight(),this.getCorner(),this.getCorner());
            } else {
                g.fillRect(0,0, this.getWidth(), this.getHeight());
            }
        }
        if(this.getStrokePaint() != null && getStrokeWidth() > 0) {
            g.setPaint(this.getStrokePaint());
            g.setStrokeWidth(this.getStrokeWidth());

            if(this.getCorner() > 0) {
                g.drawRoundRect(0,0, this.getWidth(), this.getHeight(),this.getCorner(),this.getCorner());
            } else {
                g.drawRect(0,0, this.getWidth(), this.getHeight());
            }
        }

        g.translate(-this.getX(),-this.getY());
        g.setStrokeWidth(1);
    }

    public void addListener(GradientHandle gradientHandle) {
        listeners.add(gradientHandle);
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

    public static interface SRectUpdateListener {
        public void updated();
    }
}
