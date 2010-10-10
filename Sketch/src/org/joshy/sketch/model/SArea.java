package org.joshy.sketch.model;

import org.joshy.gfx.draw.GFX;
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
        return new Area(this.area);
    }

    public void draw(GFX g) {
        PathIterator it = area.getPathIterator(null);
        Path2D.Double pth = new Path2D.Double();
        pth.append(it,false);

        g.setPaint(getFillPaint());        
        g.fillPath(pth);
        g.setPaint(getStrokePaint());
        g.setStrokeWidth(getStrokeWidth());
        g.drawPath(pth);
        g.setStrokeWidth(1);
    }
}
