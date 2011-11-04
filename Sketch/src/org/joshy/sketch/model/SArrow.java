package org.joshy.sketch.model;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.GeomUtil;

import java.awt.geom.*;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: May 18, 2010
* Time: 2:33:42 PM
* To change this template use File | Settings | File Templates.
*/
public class SArrow extends SShape implements SelfDrawable {
    private Point2D start;
    private Point2D end;

    public SArrow(Point2D start, Point2D end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Bounds getBounds() {
        Rectangle2D r = calcBounds(getTranslateX(),getTranslateY()).getBounds2D();
        return new Bounds(r.getX(),r.getY(),r.getWidth(),r.getHeight());
    }

    @Override
    public Bounds getTransformedBounds() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean contains(Point2D point) {
        double x = getTranslateX();
        double y = getTranslateY();
        return calcBounds(x,y).contains(point);
    }

    private Path2D calcBounds(double x, double y) {
        Path2D path = new Path2D.Double();
        double angle = GeomUtil.calcAngle(start,end);

        Point2D ep1 = GeomUtil.calcPoint(end,angle+90,10);
        path.moveTo(x+ep1.getX(),y+ep1.getY());

        Point2D ep2 = GeomUtil.calcPoint(end,angle-90,10);
        path.lineTo(x+ep2.getX(),y+ep2.getY());


        Point2D sp2 = GeomUtil.calcPoint(start,angle-90,10);
        path.lineTo(x+sp2.getX(),y+sp2.getY());

        Point2D sp1 = GeomUtil.calcPoint(start,angle+90,10);
        path.lineTo(x+sp1.getX(),y+sp1.getY());

        path.lineTo(x+ep1.getX(),y+ep1.getY());
        path.closePath();
        return path;
    }

    public void setStart(Point2D start) {
        this.start = start;
    }


    public void draw(GFX g) {
        g.setPaint(getFillPaint());
        g.setStrokeWidth(getStrokeWidth());
        double angle = Math.toDegrees(GeomUtil.calcAngle(start,end));
        g.drawLine(start.getX(),start.getY(),end.getX(),end.getY());

        if(headEnd == HeadEnd.StartOnly || headEnd == HeadEnd.BothEnds) {
            Point2D ap1 = GeomUtil.calcPoint(start, angle + 45, 10);
            g.drawLine(start.getX(),start.getY(),ap1.getX(),ap1.getY());
            Point2D ap2 = GeomUtil.calcPoint(start, angle - 45, 10);
            g.drawLine(start.getX(),start.getY(),ap2.getX(),ap2.getY());
        }
        if(headEnd == HeadEnd.EndOnly || headEnd == HeadEnd.BothEnds) {
            Point2D ap1 = GeomUtil.calcPoint(end, angle - 45-90, 10);
            g.drawLine(end.getX(),end.getY(),ap1.getX(),ap1.getY());
            Point2D ap2 = GeomUtil.calcPoint(end, angle + 45+90, 10);
            g.drawLine(end.getX(),end.getY(),ap2.getX(),ap2.getY());
        }

        g.setStrokeWidth(1);

        //drawPath(g,calcBounds(getTranslateX(),getTranslateY()),true);
    }

    private void drawPath(GFX g, Path2D pt, boolean closed) {
        PathIterator it = pt.getPathIterator(null,0.01);
        double x = 0;
        double y = 0;
        g.setPaint(this.getStrokePaint());
        g.setStrokeWidth(this.getStrokeWidth());
        while(!it.isDone()) {
            double[] coords = new double[6];
            int n = it.currentSegment(coords);
            if(n == PathIterator.SEG_MOVETO) {
                x = coords[0];
                y = coords[1];
            }
            if(n == PathIterator.SEG_LINETO) {
                g.drawLine(x,y,coords[0],coords[1]);
                x = coords[0];
                y = coords[1];
            }
            if(n == PathIterator.SEG_CLOSE) {
                if(closed) {
                    //g.drawLine(x,y,node.points.get(0).x,node.points.get(0).y);
                }
                break;
            }
            it.next();
        }
        g.setStrokeWidth(1);
        g.setPureStrokes(false);

    }

    public Point2D getStart() {
        return start;
    }

    public Point2D getEnd() {
        return end;
    }

    public void setEnd(Point2D.Double end) {
        this.end = end;
    }

    public HeadEnd getHeadEnd() {
        return headEnd;
    }

    public void setHeadEnd(HeadEnd headEnd) {
        this.headEnd = headEnd;
    }

    @Override
    public Area toArea() {
        return new Area();
    }

    public enum HeadEnd {
        StartOnly,
        EndOnly,
        BothEnds
    }

    private HeadEnd headEnd = HeadEnd.StartOnly;

}
