package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.ImageBuffer;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.draw.effects.BlurEffect;
import org.joshy.gfx.node.Bounds;

import java.awt.geom.Area;

public abstract class SShape extends SNode {
    public Paint fillPaint = FlatColor.GRAY;
    public FlatColor strokePaint = FlatColor.BLACK;
    private double strokeWidth = 1;
    private double fillOpacity = 1.0;
    private DropShadow shadow = null;

    public Paint getFillPaint() {
        return fillPaint;
    }

    public SShape setFillPaint(Paint fillPaint) {
        this.fillPaint = fillPaint;
        return this;
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
        ((SShape)dupe).setFillOpacity(this.getFillOpacity());
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

    protected void drawShadow(GFX g) {
        if(shadow != null) {
            int blurRadius = shadow.getBlurRadius();
            if(blurRadius <= 0) blurRadius = 0;
            Bounds b = getBounds();
            ImageBuffer buf = g.createBuffer(
                    (int)b.getWidth()+blurRadius*2,
                    (int)b.getHeight()+blurRadius*2);
            double dx = getTranslateX()-b.getX();
            double dy = getTranslateY()-b.getY();
            double xoff = shadow.getXOffset();
            double yoff = shadow.getYOffset();
            if(buf != null) {
                GFX g2 = buf.getGFX();
                //g2.setPaint(FlatColor.RED);
                //g2.fillRect(0,0,buf.buf.getWidth(),buf.buf.getHeight());
                g2.setPaint(FlatColor.BLACK.deriveWithAlpha(shadow.getOpacity()));
                g2.translate(blurRadius,blurRadius);
                g2.translate(dx,dy);
                fillShape(g2);
                g2.translate(-dx,-dy);
                buf.apply(new BlurEffect(blurRadius));
                g2.translate(-blurRadius,-blurRadius);
            }
            g.draw(buf,xoff-blurRadius-dx,yoff-blurRadius-dy);
        }
    }

    protected void fillShape(GFX g) { }

    public void setShadow(DropShadow shadow) {
        this.shadow = shadow;
    }

    public DropShadow getShadow() {
        return this.shadow;
    }
}
