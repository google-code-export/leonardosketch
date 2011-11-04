package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.ImageBuffer;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.draw.effects.BlurEffect;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.stage.swing.SwingGFX;

import java.awt.*;
import java.awt.geom.AffineTransform;
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
    private boolean oldInner;
    private boolean contentChanged = true;

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
        markContentChanged();
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
        markContentChanged();
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public SNode duplicate(SNode dupe) {
        if(dupe == null) throw new IllegalArgumentException("SShape.duplicate: duplicate shape argument can't be null!");
        ((SShape)dupe).setFillPaint(this.getFillPaint().duplicate());
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
        markContentChanged();
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

            if(buf == null
                    || shadow != oldShadow
                    || b.getWidth() != oldWidth
                    || b.getHeight() != oldHeight
                    || shadow.isInner() != oldInner
                    || contentChanged) {
                if(shadow.isInner()) {
                    regenInnerShadow(g);
                } else {
                    regenShadow(g);
                }
                oldInner = shadow.isInner();
                contentChanged = false;
            }
            double oldOpacity = g.getOpacity();
            g.setOpacity(shadow.getOpacity());
            g.draw(buf,b.getX()-blurRadius*2+xoff,b.getY()-blurRadius*2+yoff);
            g.setOpacity(oldOpacity);
        }
    }

    protected void initPaint(GFX g) {
        Paint paint = this.getFillPaint();
        if(paint != null) {
            if(paint instanceof FlatColor) {
                g.setPaint(((FlatColor)paint).deriveWithAlpha(getFillOpacity()));
            }
        }
    }

    protected void regenInnerShadow(GFX g) {
        //setup
        int blurRadius = 5;
        Bounds b = getBounds();
        ImageBuffer bufx = g.createBuffer(
                (int)b.getWidth()+blurRadius*2,
                (int)b.getHeight()+blurRadius*2);

        //render shadow to buffer
        bufx.clear();
        GFX g2 = bufx.getGFX();
        g2.setPaint(FlatColor.BLACK);
        g2.translate(10, 10);
        fillShape(g2);
        g2.translate(-10, -10);
        bufx.apply(new BlurEffect(blurRadius));
        g2.dispose();

        //invert alpha channel
        for(int i=0; i<bufx.buf.getWidth(); i++) {
            for(int j=0; j<bufx.buf.getHeight(); j++) {
                int rgb = bufx.buf.getRGB(i,j);
                int a = (0xFF000000 & rgb)>>24;
                a = 255-a;
                rgb = (rgb & 0x00FFFFFF) | (a<<24);
                bufx.buf.setRGB(i,j,rgb);
            }
        }


        //composite using a boolean op to mask out the bg
        buf = g.createBuffer(
                (int)b.getWidth()+blurRadius*2,
                (int)b.getHeight()+blurRadius*2);
        SwingGFX gx =(SwingGFX)buf.getGFX();
        Graphics2D graphics2D = (Graphics2D) gx.getNative();
        graphics2D.setComposite(AlphaComposite.SrcOver);

        initPaint(gx);
        fillShape(gx);
        graphics2D.setComposite(AlphaComposite.SrcAtop);
        gx.draw(bufx, -5, -5);
        gx.dispose();

    }

    public Bounds getEffectBounds() {
        if(getShadow() == null) {
            return getBounds();
        }
        Bounds bounds = getBounds();
        int rad = shadow.getBlurRadius();
        double xoff = shadow.getXOffset();
        double yoff = shadow.getYOffset();
        Bounds shadowBounds = new Bounds(bounds.getX()+xoff-rad,bounds.getY()+yoff-rad,bounds.getWidth()+xoff+rad,bounds.getHeight()+yoff+rad);
        Bounds union = bounds.union(shadowBounds);
        return union;
    }
    protected void regenShadow(GFX g) {
        //setup
        int blurRadius = shadow.getBlurRadius()*2;
        Bounds b = getBounds();
        oldWidth = b.getWidth();
        oldHeight = b.getHeight();
        buf = g.createBuffer(
                (int)b.getWidth()+blurRadius*2,
                (int)b.getHeight()+blurRadius*2);

        //draw shape with shadow color
        GFX g2 = buf.getGFX();

        //fill shape with black
        g2.setPaint(FlatColor.BLACK);
        g2.translate(blurRadius,blurRadius);
        g2.translate(-b.getX(),-b.getY());
        initPaint(g2);
        fillShape(g2);
        drawShape(g2);
        g2.translate(b.getX(),b.getY());
        //blur
        buf.apply(new BlurEffect(blurRadius));
        g2.translate(-blurRadius,-blurRadius);
        //use blur as alpha mask to draw in the real color
        buf.apply(new WipeColorEffect(shadow.getColor()));

        oldShadow = shadow;
    }

    protected void drawShape(GFX g) { }

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

    protected void markContentChanged() {
        contentChanged = true;
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

    public SPath toPath() {
        return null;
    }

    protected Shape transformShape(Shape sh) {
        AffineTransform af = new AffineTransform();
        af.translate(getTranslateX(),getTranslateY());
        af.translate(getAnchorX(),getAnchorY());
        af.rotate(Math.toRadians(getRotate()));
        af.scale(getScaleX(), getScaleY());
        af.translate(-getAnchorX(),-getAnchorY());
        return af.createTransformedShape(sh);
    }

    public static interface SShapeListener {
        public void changed();
    }
}
