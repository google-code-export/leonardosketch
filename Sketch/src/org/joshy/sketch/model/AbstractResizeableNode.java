package org.joshy.sketch.model;

import org.joshy.gfx.node.Bounds;

import java.awt.geom.Point2D;

public abstract class AbstractResizeableNode extends SShape implements SResizeableNode {
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
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
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
    public Bounds getBounds() {
        return new Bounds(
                getTranslateX()+getX(),
                getTranslateY()+getY(),
                getWidth(),
                getHeight());
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
}
