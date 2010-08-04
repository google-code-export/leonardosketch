package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.u;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: May 12, 2010
* Time: 7:41:09 PM
* To change this template use File | Settings | File Templates.
*/
public class SPath extends SShape implements SelfDrawable {
    public List<PathPoint> points;
    private boolean closed;
    private Path2D.Double path2d;

    public SPath() {
        this.points = new ArrayList<PathPoint>();
    }

    @Override
    public Bounds getBounds() {
        if(path2d != null) {
            Rectangle bds = path2d.getBounds();
            return new Bounds(
                    getTranslateX()+bds.getX(),
                    getTranslateY()+bds.getY(),
                    bds.getWidth(),
                    bds.getHeight());
        }
        return new Bounds(0,0,100,100);
    }

    @Override
    public boolean contains(Point2D point) {
        if(path2d != null) {
            return path2d.contains(point.getX()-getTranslateX(),point.getY()-getTranslateY());
        }
        return getBounds().contains(point.getX(),point.getY());
    }

    public void addPoint(PathPoint point) {
        this.points.add(point);
    }

    public void draw(GFX g) {
        drawPath(g,this);
    }



    public static void drawPath(GFX g, SPath node) {
        g.setPureStrokes(true);
        g.setPaint(FlatColor.BLACK);
        Path2D.Double pt = new Path2D.Double();
        int last = node.points.size()-1;
        for(int i=0; i<node.points.size(); i++) {
            SPath.PathPoint point = node.points.get(i);
            g.setPaint(FlatColor.BLACK);
            if(i == 0) {
                pt.moveTo(point.x,point.y);
                continue;
            }
            SPath.PathPoint prev = node.points.get(i - 1);
            pt.curveTo(prev.cx2,prev.cy2,
                    point.cx1,point.cy1,
                    point.x,point.y
                    );
            if(i == node.points.size()-1) {
                if(node.isClosed()) {
                    SPath.PathPoint first = node.points.get(0);
                    pt.curveTo(point.cx2,point.cy2,
                        first.cx1,first.cy1,
                        first.x,first.y
                        );
                    pt.closePath();
                }
            }
        }
        //if(node.isClosed()){
        Paint fp = node.getFillPaint();
        if(fp != null) {
            if(fp instanceof FlatColor) {
                fp = ((FlatColor)fp).deriveWithAlpha(node.getFillOpacity());
            }            
            g.setPaint(fp);
            g.fillPath(pt);
        }
        //}
        if(node.getStrokeWidth() > 0) {
            g.setPaint(node.getStrokePaint());
            g.setStrokeWidth(node.getStrokeWidth());
            g.drawPath(pt);
            g.setStrokeWidth(1);
        }
        g.setPureStrokes(false);
    }


    public void close(boolean closed) {
        setClosed(closed);
        recalcPath();
    }

    public void recalcPath() {
        path2d = new Path2D.Double();
        int last = points.size()-1;
        for(int i=0; i<points.size(); i++) {
            PathPoint point = points.get(i);
            if(i == 0) {
                path2d.moveTo(point.x,point.y);
                continue;
            }
            PathPoint prev = points.get(i - 1);
            path2d.curveTo(prev.cx2,prev.cy2,
                    point.cx1,point.cy1,
                    point.x,point.y
                    );
            if(i == points.size()-1) {
                path2d.closePath();
            }
        }

    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }

    public List<PathPoint> getPoints() {
        return points;
    }

    public void setPoints(List<PathPoint> points) {
        this.points = points;
        recalcPath();
    }

    public void addAllPoints(List<PathPoint> points) {
        this.points.addAll(points);
    }

    public Iterable<PathSegment> calculateSegments() {
        List<PathSegment> segs = new ArrayList<PathSegment>();
        for(int i=0; i<points.size()-1;i++) {
            PathPoint curr = points.get(i);
            PathPoint next = points.get(i+1);
            segs.add(new PathSegment(curr,next,i));
        }
        if(isClosed()) {
            int last = points.size()-1;
            segs.add(new PathSegment(points.get(last),points.get(0),last));
        }
        return segs;
    }


    public void splitPath(PathTuple location) {
        PathPoint a = location.a;
        PathPoint b = location.b;
        PathPoint c = new PathPoint(0,0);

        double co[] = new double[14];
        co[0] = a.x; co[1] = a.y;
        co[2] = a.cx2; co[3] = a.cy2;
        co[4] = b.cx1; co[5] = b.cy1;
        co[6] = b.x; co[7] = b.y;
        split(co,0,location.t);

        a.x = co[0];   a.y = co[1];
        a.cx2 = co[2]; a.cy2 = co[3];
        c.cx1 = co[4]; c.cy1 = co[5];
        c.x = co[6];   c.y = co[7];
        c.cx2 = co[8]; c.cy2 = co[9];
        b.cx1 = co[10]; b.cy1 = co[11];
        b.x = co[12];   b.y = co[13];
        u.p("added the new point: " + c.x + " " + c.y);
        points.add(location.index+1,c);
    }
    
    /*
     * Split the cubic Bezier stored at coords[pos...pos+7] representing
     * the parametric range [0..1] into two subcurves representing the
     * parametric subranges [0..t] and [t..1].  Store the results back
     * into the array at coords[pos...pos+7] and coords[pos+6...pos+13].
     */
    public static void split(double coords[], int pos, double t) {
	double x0, y0, cx0, cy0, cx1, cy1, x1, y1;
	coords[pos+12] = x1 = coords[pos+6];
	coords[pos+13] = y1 = coords[pos+7];
	cx1 = coords[pos+4];
	cy1 = coords[pos+5];
	x1 = cx1 + (x1 - cx1) * t;
	y1 = cy1 + (y1 - cy1) * t;
	x0 = coords[pos+0];
	y0 = coords[pos+1];
	cx0 = coords[pos+2];
	cy0 = coords[pos+3];
	x0 = x0 + (cx0 - x0) * t;
	y0 = y0 + (cy0 - y0) * t;
	cx0 = cx0 + (cx1 - cx0) * t;
	cy0 = cy0 + (cy1 - cy0) * t;
	cx1 = cx0 + (x1 - cx0) * t;
	cy1 = cy0 + (y1 - cy0) * t;
	cx0 = x0 + (cx0 - x0) * t;
	cy0 = y0 + (cy0 - y0) * t;
	coords[pos+2] = x0;
	coords[pos+3] = y0;
	coords[pos+4] = cx0;
	coords[pos+5] = cy0;
	coords[pos+6] = cx0 + (cx1 - cx0) * t;
	coords[pos+7] = cy0 + (cy1 - cy0) * t;
	coords[pos+8] = cx1;
	coords[pos+9] = cy1;
	coords[pos+10] = x1;
	coords[pos+11] = y1;
    }

    public void normalize() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        for(PathPoint pt : points) {
            minX = Math.min(pt.x,minX);
            minY = Math.min(pt.y,minY);
        }
        u.p("smallest points = " + minX + " " + minY);
        for(PathPoint pt : points) {
            pt.x -= minX;
            pt.y -= minY;
            
            pt.cx1 -= minX;
            pt.cx2 -= minX;

            pt.cy1 -= minY;
            pt.cy2 -= minY;
        }
        setTranslateX(getTranslateX()+minX);
        setTranslateY(getTranslateY()+minY);
        recalcPath();
    }

    public static class PathTuple {
        public double distance;
        public double t;
        public Point2D.Double point;
        public PathPoint a;
        public PathPoint b;
        public int index;
    }

    public static class PathSegment {
        Point2D.Double p1;
        Point2D.Double p2;
        Point2D.Double p3;
        Point2D.Double p4;
        private PathPoint a;
        private PathPoint b;
        private int index;

        public PathSegment(PathPoint curr, PathPoint next, int index) {
            a = curr;
            b = next;
            this.index = index;
            this.p1 = new Point2D.Double(curr.x,curr.y);
            this.p2 = new Point2D.Double(curr.cx2,curr.cy2);
            this.p3 = new Point2D.Double(next.cx1,next.cy1);
            this.p4 = new Point2D.Double(next.x,next.y);
        }

        public PathTuple closestDistance(Point2D.Double point) {
            Point2D.Double closest = calculatePoint(p1,p2,p3,p4,0);
            double closestDistance = calculateDistance(point.getX(),point.getY(),closest);
            double closestT = 0;
            for(double t=0; t<=1.0; t+=0.01) {
                Point2D.Double b = calculatePoint(p1,p2,p3,p4,t);
                double distance = calculateDistance(point.getX(),point.getY(),b);
                if(distance < closestDistance) {
                    closestDistance = distance;
                    closest = b;
                    closestT = t;
                }
            }
            PathTuple tup = new PathTuple();
            tup.t = closestT;
            tup.point = closest;
            tup.distance = closestDistance;
            tup.a = a;
            tup.b = b;
            tup.index = index;
            return tup;
        }

        private double calculateDistance(double x, double y, Point2D.Double b) {
            double dx = x-b.x;
            double dy = y-b.y;
            double distance = Math.sqrt(dx*dx+dy*dy);
            return distance;
        }
        private Point2D.Double calculatePoint(Point2D.Double p1, Point2D.Double p2, Point2D.Double p3, Point2D.Double p4, double mu) {
            double mum1 = 1 - mu;
            double mum13 = mum1 * mum1 * mum1;
            double mu3 = mu*mu*mu;

            Point2D.Double p = new Point2D.Double();
            p.x = mum13*p1.x + 3*mu*mum1*mum1*p2.x + 3*mu*mu*mum1*p3.x + mu3*p4.x;
            p.y = mum13*p1.y + 3*mu*mum1*mum1*p2.y + 3*mu*mu*mum1*p3.y + mu3*p4.y;
            return p;
        }
    }

    public static class PathPoint {
        public double x;
        public double y;
        public boolean bound;
        public double cx1;
        public double cy1;
        public double cx2;
        public double cy2;

        public PathPoint(double x, double y, double cx1, double cy1, double cx2, double cy2) {
            this.x = x;
            this.y = y;
            this.bound = false;
            this.cx1 = cx1;
            this.cy1 = cy1;
            this.cx2 = cx2;
            this.cy2 = cy2;
        }

        public PathPoint(double x, double y) {
            this.x = x;
            this.y = y;
            this.cx1 = x;
            this.cy1 = y;
            this.cx2 = x;
            this.cy2 = y;
        }

        public double distance(double x, double y) {
            double x2 = this.x - x;
            double y2 = this.y - y;
            return Math.sqrt(x2*x2+y2*y2);
        }

        public PathPoint copy() {
            return new PathPoint(x,y,cx1,cy1,cx2,cy2);
        }
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new SPath();
        }
        for(PathPoint point : this.getPoints()) {
            ((SPath)dupe).addPoint(point.copy());
        }
        ((SPath)dupe).setClosed(this.isClosed());
        ((SPath)dupe).recalcPath();
        return super.duplicate(dupe);
    }
}