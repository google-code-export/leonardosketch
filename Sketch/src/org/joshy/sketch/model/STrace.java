package org.joshy.sketch.model;

import java.awt.geom.Point2D;
import java.util.*;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Bounds;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 10/22/12
 * Time: 1:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class STrace extends SNode implements SelfDrawable {

    private Map<TracePoint,SlaveFunction> slaves;
    private List<TracePoint> points;

    public STrace() {
        super();
        slaves = new HashMap<TracePoint, SlaveFunction>();
        this.points = new ArrayList<TracePoint>();
    }

    public void updateSlavePositions() {
        for(TracePoint tp : slaves.keySet()) {
            SlaveFunction slave = slaves.get(tp);
            slave.apply(tp,this);
        }
    }

    public void addSlaveFunction(TracePoint point, SlaveFunction func) {
        slaves.put(point, func);
    }

    public void removeSlaveFunction(TracePoint point) {
        slaves.remove(point);
    }

    public TracePoint addPoint(Point2D.Double cursor) {
        TracePoint tp = new TracePoint(cursor); 
        this.points.add(tp);
        return tp;
    }

    public List<TracePoint> getPoints() {
        return points;
    }

    public void draw(GFX g) {
        g.setPaint(FlatColor.PURPLE);
        TracePoint start = null;
        Iterator<TracePoint> it = points.iterator();
        while (it.hasNext()) {
            TracePoint end =  it.next();
            if(start != null) {
                g.drawLine(start.getX(),start.getY(),end.getX(),end.getY());
            }
            start = end;
        }
    }

    public interface SlaveFunction {
        public void apply(TracePoint pt, SNode node);
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(0,0,100,100);
    }

    @Override
    public Bounds getTransformedBounds() {
        return new Bounds(0,0,100,100);
    }

    @Override
    public boolean contains(Point2D point) {
        return false;
    }

    @Override
    public void setTranslateX(double translateX) {
        super.setTranslateX(translateX);
        updateSlavePositions();
    }

    @Override
    public void setTranslateY(double translateY) {
        super.setTranslateY(translateY);
        updateSlavePositions();
    }

    public class TracePoint {

        private double x;
        private double y;

        public TracePoint(Point2D.Double cursor) {
            this.x = cursor.getX();
            this.y = cursor.getY();
        }

        public void setLocation(Point2D.Double cursor) {
            this.x = cursor.getX();
            this.y = cursor.getY();
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double distance(double x, double y) {
            return Point2D.distance(x,y,this.x,this.y);
        }

        public void setLocation(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
