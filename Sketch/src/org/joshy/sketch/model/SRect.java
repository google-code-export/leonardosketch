package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.draw.Paint;

import java.awt.geom.Area;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Apr 6, 2010
 * Time: 12:36:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class SRect extends AbstractResizeableNode implements SelfDrawable {
    private double corner = 0;

    public SRect(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    public SRect() {
        super(0,0,100,100);
    }

    public void setCorner(double corner) {
        this.corner = corner;
    }

    public double getCorner() {
        return corner;
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new SRect();
        }
        ((SRect)dupe).setCorner(this.getCorner());
        return super.duplicate(dupe);
    }

    @Override
    public Area toArea() {
        return new Area(new java.awt.Rectangle.Double(
                getX()+getTranslateX(),
                getY()+getTranslateY(),
                getWidth(),getHeight()
        ));
    }

    public void draw(GFX g) {
        g.translate(this.getX(),this.getY());

        Paint paint = this.getFillPaint();
        if(paint != null) {
            if(paint instanceof FlatColor) {
                g.setPaint(((FlatColor)paint).deriveWithAlpha(getFillOpacity()));
            }
            if(paint instanceof GradientFill) {
                g.setPaint(rescaleGradient((GradientFill) paint,this));
            }
            if(this.getCorner() > 0) {
                g.fillRoundRect(0,0,this.getWidth(),this.getHeight(),this.getCorner(),this.getCorner());
            } else {
                g.fillRect(0,0, this.getWidth(), this.getHeight());
            }
        }
        if(this.getStrokePaint() != null && getStrokeWidth() > 0) {
            g.setPaint(this.getStrokePaint());
            g.setStrokeWidth(this.getStrokeWidth());

            if(this.getCorner() > 0) {
                g.drawRoundRect(0,0, this.getWidth(), this.getHeight(),this.getCorner(),this.getCorner());
            } else {
                g.drawRect(0,0, this.getWidth(), this.getHeight());
            }
        }

        g.translate(-this.getX(),-this.getY());
        g.setStrokeWidth(1);
    }

    private Paint rescaleGradient(GradientFill gf, SRect rect) {
        double angle = gf.angle;
        int mode = 0;
        double a = Math.toRadians(angle);
        double a2 = Math.toRadians(angle+180.0);
        if(mode==1) {
            double h = rect.getHeight()/2;
            double cx = rect.getWidth()/2;
            double cy = rect.getHeight()/2;
            gf.startX = Math.cos(a)*h + cx;
            gf.endX = Math.cos(a2)*h + cx;
            gf.startY = Math.sin(a)*h + cy;
            gf.endY =  Math.sin(a2)*h + cy;
            }
        if(mode == 0) {
            //quadrant 1
            //right
            if(angle >=0 && angle <45)          { gf.startX = 0; gf.endX = rect.getWidth();  gf.startY = 0; gf.endY = 0;  }
            //up
            if(angle >=45 && angle <90+45)      { gf.startX = 0; gf.endX = 0;  gf.startY = 0; gf.endY = rect.getHeight(); }
            //left
            if(angle >=90+45 && angle <180+45)  { gf.startX = rect.getWidth(); gf.endX = 0;  gf.startY = 0; gf.endY = 0;  }
            //down
            if(angle >=180+45 && angle <270+45) { gf.startX = 0; gf.endX = 0; gf.startY = rect.getHeight(); gf.endY = 0;  }
            //right
            if(angle >=270+45 && angle <360+45) { gf.startX = 0; gf.endX = rect.getWidth();  gf.startY = 0; gf.endY = 0; }
        }
        return gf;
    }

}
