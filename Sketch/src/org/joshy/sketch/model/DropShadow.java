package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/9/11
 * Time: 6:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class DropShadow {
    private double opacity = 1.0;
    private double xoff = 5;
    private double yoff = 5;
    private int blurRadius = 3;
    private FlatColor color = FlatColor.BLACK;
    private boolean inner;

    public DropShadow() {

    }

    public DropShadow(DropShadow dropShadow) {
        this.opacity = dropShadow.opacity;
        this.xoff = dropShadow.xoff;
        this.yoff = dropShadow.yoff;
        this.blurRadius = dropShadow.blurRadius;
        this.color = dropShadow.color;
        this.inner = dropShadow.inner;
    }

    public DropShadow setOpacity(double opacity) {
        DropShadow shadow = new DropShadow(this);
        shadow.opacity = opacity;
        return shadow;
    }

    public double getOpacity() {
        return opacity;
    }

    public DropShadow setXOffset(double xoff) {
        DropShadow shadow = new DropShadow(this);
        shadow.xoff = xoff;
        return shadow;
    }

    public double getXOffset() {
        return xoff;
    }

    public DropShadow setYOffset(double yoff) {
        DropShadow shadow = new DropShadow(this);
        shadow.yoff = yoff;
        return shadow;
    }

    public double getYOffset() {
        return yoff;
    }

    public DropShadow setBlurRadius(int blurRadius) {
        DropShadow shadow = new DropShadow(this);
        shadow.blurRadius = blurRadius;
        return shadow;
    }

    public int getBlurRadius() {
        return this.blurRadius;
    }

    public DropShadow setColor(FlatColor color) {
        DropShadow shadow = new DropShadow(this);
        shadow.color = color;
        return shadow;
    }

    public FlatColor getColor() {
        return color;
    }

    public void setInner(boolean inner) {
        this.inner = inner;
    }

    public boolean isInner() {
        return inner;
    }
}
