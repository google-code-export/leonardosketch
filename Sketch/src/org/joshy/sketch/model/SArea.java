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

    @Override
    public SPath toPath() {
        return SPath.fromPathIterator(this.area.getPathIterator(null));
    }

    @Override
    protected void fillShape(GFX g) {
        PathIterator it = area.getPathIterator(null);
        Path2D.Double pth = new Path2D.Double();
        pth.append(it,false);
        g.fillPath(pth);
    }

    public void draw(GFX g) {
        drawShadow(g);

        Paint paint = getFillPaint();
        if(paint != null) {
            double opacity = -1;
            if(paint instanceof FlatColor) {
                g.setPaint(((FlatColor)paint).deriveWithAlpha(getFillOpacity()));
            }
            if(paint instanceof MultiGradientFill) {
                Rectangle2D b = area.getBounds2D();
                MultiGradientFill gf = (MultiGradientFill) paint;
                gf = gf.translate(b.getX(),b.getY());
                g.setPaint(gf);
            }
            if(paint instanceof PatternPaint) {
                opacity = g.getOpacity();
                g.setOpacity(getFillOpacity());
                g.setPaint(paint);
            }
            fillShape(g);
            if(opacity >=0) g.setOpacity(opacity);
        }

        if(getStrokeWidth() > 0 && getStrokePaint() != null) {
            g.setPaint(getStrokePaint());
            g.setStrokeWidth(getStrokeWidth());
            PathIterator it = area.getPathIterator(null);
            Path2D.Double pth = new Path2D.Double();
            pth.append(it,false);
            g.drawPath(pth);
            g.setStrokeWidth(1);
        }

    }

    public void setArea(Area area) {
        this.area = area;
    }
}
