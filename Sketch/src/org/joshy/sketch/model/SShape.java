package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.ImageBuffer;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.draw.effects.BlurEffect;
import org.joshy.gfx.node.Bounds;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

public abstract class SShape extends SNode {
    public Paint fillPaint = FlatColor.GRAY;
    public FlatColor strokePaint = FlatColor.BLACK;
    private double strokeWidth = 1;
    private double fillOpacity = 1.0;
    private DropShadow shadow = null;
    private List<SShapeListener> listeners = new ArrayList<SShapeListener>();
    private ImageBuffer buf;
    private DropShadow oldShadow;
    private double oldWidth;
    private double oldHeight;

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

    public abstract Area toArea();

    protected void drawShadow(GFX g) {
        if(shadow != null) {
            int blurRadius = shadow.getBlurRadius();
            if(blurRadius <= 0) blurRadius = 0;
            double xoff = shadow.getXOffset();
            double yoff = shadow.getYOffset();
            Bounds b = getBounds();
            double dx = getTranslateX()-b.getX();
            double dy = getTranslateY()-b.getY();

            if(buf == null || shadow != oldShadow || b.getWidth() != oldWidth || b.getHeight() != oldHeight) {
                oldWidth = b.getWidth();
                oldHeight = b.getHeight();
                buf = g.createBuffer(
                        (int)b.getWidth()+blurRadius*2,
                        (int)b.getHeight()+blurRadius*2);
                GFX g2 = buf.getGFX();
                //g2.setPaint(FlatColor.RED);
                //g2.fillRect(0,0,buf.buf.getWidth(),buf.buf.getHeight());
                g2.setPaint(shadow.getColor().deriveWithAlpha(shadow.getOpacity()));
                g2.translate(blurRadius,blurRadius);
                g2.translate(dx, dy);
                fillShape(g2);
                g2.translate(-dx, -dy);
                buf.apply(new BlurEffect(blurRadius));
                g2.translate(-blurRadius,-blurRadius);
                oldShadow = shadow;
            }
            g.draw(buf,xoff-blurRadius-dx,yoff-blurRadius-dy);
        }
    }

    protected void fillShape(GFX g) { }

    public SShape setShadow(DropShadow shadow) {
        this.shadow = shadow;
        return this;
    }

    public DropShadow getShadow() {
        return this.shadow;
    }

    public void addListener(SShapeListener gradientHandle) {
        listeners.add(gradientHandle);
    }

    protected void fireUpdate() {
        if(listeners != null) {
            for(SShapeListener c : listeners) {
                c.changed();
            }
        }
    }

    public void removeListener(SShapeListener gradientHandle) {
        listeners.remove(gradientHandle);
    }

    public static interface SShapeListener {
        public void changed();
    }
}
