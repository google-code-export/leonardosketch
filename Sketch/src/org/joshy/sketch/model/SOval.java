package org.joshy.sketch.model;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.Paint;

import java.awt.*;
import java.awt.geom.Area;

public class SOval extends AbstractResizeableNode implements SelfDrawable {

    public SOval() {
        super(0,0,100,100);
    }

    public SOval(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new SOval();
        }
        return super.duplicate(dupe);
    }

    @Override
    public Area toArea() {
        Shape sh = new java.awt.geom.Ellipse2D.Double(
                getX(),
                getY(),
                getWidth(),
                getHeight()
        );
        return new Area(transformShape(sh));
    }

    @Override
    public SPath toPath() {
        SPath path = new SPath();
        double x = this.getX();
        double w = this.getWidth();
        double y = this.getY();
        double h = this.getHeight();
        SPath.PathPoint pt = path.moveTo(x,y+h/2);

        double in_factor = 0.23;
        double out_factor = 1.0-in_factor;
        pt = path.curveTo(pt,
                x,y+h*in_factor,
                x+w*in_factor, y,
                x+w/2, y
                );
        pt = path.curveTo(pt,
                x+w*out_factor, y,
                x+w,y+h*in_factor,
                x+w,y+h/2
        );
        pt = path.curveTo(pt,
                x+w,y+h*out_factor,
                x+w*out_factor,y+h,
                x+w/2,y+h
                );
        pt = path.curveTo(pt,
                x+w*in_factor,y+h,
                x,y+h*out_factor,
                x,y+h/2
                );

        path.close(true);
        path.setTranslateX(this.getTranslateX());
        path.setTranslateY(this.getTranslateY());
        path.setFillPaint(this.getFillPaint());
        path.setFillOpacity(this.getFillOpacity());
        path.setStrokeWidth(this.getStrokeWidth());
        path.setStrokePaint(this.getStrokePaint());
        return path;
    }

    @Override
    protected void fillShape(GFX g) {
        g.fillOval(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    public void draw(GFX g) {
        drawShadow(g);
        Paint paint = this.getFillPaint();
        if(paint != null) {
            double opacity = -1;
            if(paint instanceof FlatColor) {
                g.setPaint(((FlatColor)paint).deriveWithAlpha(getFillOpacity()));
            }
            if(paint instanceof GradientFill) {
                g.setPaint(paint);
            }
            if(paint instanceof MultiGradientFill) {
                g.setPaint(paint);
            }
            if(paint instanceof PatternPaint) {
                opacity = g.getOpacity();
                g.setOpacity(getFillOpacity());
                g.setPaint(paint);
            }
            fillShape(g);
            if(opacity >=0) g.setOpacity(opacity);
        }
        if(getStrokePaint() != null && getStrokeWidth() > 0) {
            g.setPaint(this.getStrokePaint());
            g.setStrokeWidth(this.getStrokeWidth());
            g.drawOval(this.getX(), this.getY(), this.getWidth(), this.getHeight());
            g.setStrokeWidth(1);
        }
    }


}
