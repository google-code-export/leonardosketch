package org.joshy.sketch.model;

import org.joshy.gfx.node.Bounds;
import org.joshy.sketch.util.Util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
                getX()-getStrokeWidth()/2,
                getY()-getStrokeWidth()/2,
                getWidth()+getStrokeWidth(),
                getHeight()+getStrokeWidth());
    }

    public Bounds getTransformedBounds() {
        java.awt.geom.Rectangle2D r = new Rectangle2D.Double(getX(),getY(),getWidth(),getHeight());
        AffineTransform af = new AffineTransform();
        af.translate(getTranslateX(),getTranslateY());

        af.translate(getAnchorX(),getAnchorY());
        af.rotate(Math.toRadians(getRotate()));
        af.scale(getScaleX(), getScaleY());
        af.translate(-getAnchorX(),-getAnchorY());

        Shape sh = af.createTransformedShape(r);
        Rectangle2D bds = sh.getBounds2D();
        return Util.toBounds(bds);
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

    private void rescaleGradient() {
    }



}
