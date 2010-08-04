package org.joshy.sketch.model;

import org.joshy.gfx.draw.GFX;

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

    public void draw(GFX g) {
        if(getFillPaint() != null) {
            g.setPaint(this.getFillPaint());
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
