package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Paint;

import java.awt.geom.Area;

public abstract class SShape extends SNode {
    public Paint fillPaint = FlatColor.GRAY;
    public FlatColor strokePaint = FlatColor.BLACK;
    private double strokeWidth = 1;
    private double fillOpacity = 1.0;

    public Paint getFillPaint() {
        return fillPaint;
    }

    public void setFillPaint(Paint fillPaint) {
        this.fillPaint = fillPaint;
    }

    public FlatColor getStrokePaint() {
        return strokePaint;
    }

    public void setStrokePaint(FlatColor strokePaint) {
        this.strokePaint = strokePaint;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public SNode duplicate(SNode dupe) {
        if(dupe == null) throw new IllegalArgumentException("SShape.duplicate: duplicate shape argument can't be null!");
        ((SShape)dupe).setFillPaint(this.getFillPaint());
        ((SShape)dupe).setStrokePaint(this.getStrokePaint());
        ((SShape)dupe).setStrokeWidth(this.getStrokeWidth());
        return super.duplicate(dupe);
    }

    public double getFillOpacity() {
        return fillOpacity;
    }

    public void setFillOpacity(double fillOpacity) {
        this.fillOpacity = fillOpacity;
    }

    public abstract Area toArea();
}
