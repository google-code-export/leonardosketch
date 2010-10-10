package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;

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
        return new Area(new java.awt.geom.Ellipse2D.Double(
                getX()+getTranslateX(),
                getY()+getTranslateY(),
                getWidth(),
                getHeight()
        ));
    }

    public void draw(GFX g) {
        if(getFillPaint() != null) {
            g.setPaint(this.getFillPaint());
            if(getFillPaint() instanceof FlatColor) {
                g.setPaint(((FlatColor)getFillPaint()).deriveWithAlpha(getFillOpacity()));
            }
            g.fillOval(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
        if(getStrokePaint() != null) {
            g.setPaint(this.getStrokePaint());
            g.setStrokeWidth(this.getStrokeWidth());
            g.drawOval(this.getX(), this.getY(), this.getWidth(), this.getHeight());
            g.setStrokeWidth(1);
        }
    }


}
