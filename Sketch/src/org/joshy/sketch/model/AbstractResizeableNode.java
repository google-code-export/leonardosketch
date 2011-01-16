package org.joshy.sketch.model;

import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.node.Bounds;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractResizeableNode extends SShape implements SResizeableNode {
    private List<SRectUpdateListener> listeners = new ArrayList<SRectUpdateListener>();
    public double width = 100;
    public double height = 100;
    private double x = 0;
    public double y = 0;

    public AbstractResizeableNode(double x, double y, double w, double h) {
        this.setX(x);
        this.setY(y);
        this.setWidth(w);
        this.setHeight(h);
    }

    public double getX() {
        return x;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
        rescaleGradient();
        fireUpdate();
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
        rescaleGradient();
        fireUpdate();
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
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
    public Bounds getBounds() {
        return new Bounds(
                getTranslateX()+getX()-getStrokeWidth()/2,
                getTranslateY()+getY()-getStrokeWidth()/2,
                getWidth()+getStrokeWidth(),
                getHeight()+getStrokeWidth());
    }

    @Override
    public boolean contains(Point2D point) {
        double x = getX() + getTranslateX();
        if(point.getX() >= x && point.getX() <= x + getWidth()) {
            double y = getY() + getTranslateY();
            if(point.getY() >= y && point.getY() <= y + this.getHeight()) {
                return true;
            }
        }
        return false;
    }

    public SNode duplicate(SNode dupe) {
        if(dupe == null) throw new IllegalArgumentException("SResizeableRect.duplicate: duplicate shape argument can't be null!");
        AbstractResizeableNode sdupe = (AbstractResizeableNode) dupe;
        sdupe.setX(this.getX());
        sdupe.setY(this.getY());
        sdupe.setWidth(this.getWidth());
        sdupe.setHeight(this.getHeight());
        return super.duplicate(sdupe);
    }

    public double getPreferredAspectRatio() {
        return 1.0;
    }

    public boolean constrainByDefault() {
        return false;
    }

    public void addListener(SRectUpdateListener gradientHandle) {
        listeners.add(gradientHandle);
    }

    protected void fireUpdate() {
        if(listeners != null) {
            for(SRectUpdateListener c : listeners) {
                c.updated();
            }
        }
    }

    public void removeListener(GradientHandle gradientHandle) {
        listeners.remove(gradientHandle);
    }

    public static interface SRectUpdateListener {
        public void updated();
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
}
