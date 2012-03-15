package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.sketch.model.Handle;
import org.joshy.sketch.model.SArrow;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Sep 14, 2010
 * Time: 6:51:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ArrowHandle extends Handle {
    private SArrow arrow;
    private Position pos;

    public enum Position {
        End, Start
    }

    public ArrowHandle(SArrow arrow, Position pos) {
        super();
        this.arrow = arrow;
        this.pos = pos;
    }

    @Override
    public double getX() {
        switch (pos) {
            case Start: return arrow.getStart().getX() + arrow.getTranslateX();
            case End: return   arrow.getEnd().getX() + arrow.getTranslateX();
        }
        return 0;
    }

    @Override
    public void setX(double x, boolean constrain) {
        //noop;
    }

    @Override
    public void setXY(double x, double y, boolean constrain) {
        x -= arrow.getTranslateX();
        y -= arrow.getTranslateY();

        double angle = 0;
        Point2D ptx = null;
        if(constrain) {
            Point2D pt = new Point2D.Double(x,y);
            switch(pos) {
                case Start:
                    angle = GeomUtil.calcAngle(arrow.getEnd(),pt);
                    angle = GeomUtil.snapTo45(angle);
                    ptx = GeomUtil.calcPoint(arrow.getEnd(),angle,arrow.getEnd().distance(pt));
                    arrow.setStart(ptx);
                    return;
                case End:
                    angle = GeomUtil.calcAngle(arrow.getStart(),pt);
                    angle = GeomUtil.snapTo45(angle);
                    ptx = GeomUtil.calcPoint(arrow.getStart(),angle,arrow.getStart().distance(pt));
                    arrow.setEnd(ptx);
                    return;
            }

        } else {
            switch(pos) {
                case Start:
                    arrow.setStart(new Point2D.Double(x,y));
                    return;
                case End:
                    arrow.setEnd(new Point2D.Double(x,y));
            }
        }
    }

    @Override
    public double getY() {
        switch (pos) {
            case Start: return arrow.getStart().getY() + arrow.getTranslateY();
            case End: return   arrow.getEnd().getY() + arrow.getTranslateY();
        }
        return 0;
    }

    @Override
    public void setY(double y, boolean constrain) {
        //noop;
    }

    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);

        double x = pt.getX();
        double y = pt.getY();
        DrawUtils.drawStandardHandle(g, x, y, FlatColor.BLUE);
    }

}
