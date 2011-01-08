package org.joshy.sketch.model;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.node.Bounds;

import java.awt.geom.*;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Oct 8, 2010
 * Time: 8:12:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class SArea extends SShape implements SelfDrawable {
    private Area area;

    public SArea(Area area) {
        super();
        this.area = area;
    }

    @Override
    public Bounds getBounds() {
        Rectangle2D b = area.getBounds2D();
        return new Bounds(getTranslateX()+b.getX(),getTranslateY()+b.getY(),b.getWidth(),b.getHeight());
    }

    @Override
    public boolean contains(Point2D point) {
        return getBounds().contains(point);
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new SArea(this.area);
        }
        return super.duplicate(dupe);
    }

    @Override
    public Area toArea() {
        Area a = new Area(this.area);
        a.transform(AffineTransform.getTranslateInstance(getTranslateX(),getTranslateY()));
        return a;
    }

    public void draw(GFX g) {
        PathIterator it = area.getPathIterator(null);
        Path2D.Double pth = new Path2D.Double();
        pth.append(it,false);

        Paint paint = getFillPaint();
        if(paint != null) {
            if(paint instanceof FlatColor) {
                g.setPaint(((FlatColor)paint).deriveWithAlpha(getFillOpacity()));
            }
            if(paint instanceof GradientFill) {
                g.setPaint(paint);
            }
            if(paint instanceof PatternPaint) {
                g.setPaint(paint);
            }
            g.fillPath(pth);
        }
        g.setPaint(getStrokePaint());
        g.setStrokeWidth(getStrokeWidth());
        g.drawPath(pth);
        g.setStrokeWidth(1);
    }

    public void setArea(Area area) {
        this.area = area;
    }
}
