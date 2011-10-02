package org.joshy.sketch.model;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.node.Bounds;
import org.joshy.sketch.util.Util;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class SPoly extends SShape implements SelfDrawable, HasTransformedBounds {
    private List<Point2D> points;
    private boolean closed;
    private Path2D.Double path;
    private Bounds bounds;

    public SPoly() {
        points = new ArrayList<Point2D>();
        this.setFillPaint(FlatColor.BLUE);
        closed = false;
        bounds = new Bounds(0,0,0,0);
    }

    public void addPoint(Point2D point) {
        this.points.add(point);
    }

    public int pointCount() {
        return this.points.size();
    }

    public Point2D getPoint(int i) {
        return points.get(i);
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
        recalcPath();
    }

    private void recalcPath() {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(getPoint(0).getX(),getPoint(0).getY());
        for(int i=1; i<this.pointCount(); i++) {
            p.lineTo(getPoint(i).getX(),getPoint(i).getY());
        }
        if(isClosed()) {
            p.closePath();
        }
        path = p;
        Rectangle2D bounds = path.getBounds2D();
        this.bounds = new Bounds(
                bounds.getX(),
                bounds.getY(),
                bounds.getWidth(),
                bounds.getHeight());
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(
                bounds.getX()+getTranslateX(),
                bounds.getY()+getTranslateY(),
                bounds.getWidth(),bounds.getHeight());

    }

    public boolean contains(Point2D point) {
        Point2D p = new Point2D.Double(point.getX()-getTranslateX(),point.getY()-getTranslateY());
        return path.contains(p);
    }

    public void removePoint(Point2D.Double point) {
        this.points.remove(point);
    }

    public List<Point2D> getPoints() {
        return points;
    }


    public void setPoints(List<Point2D> points) {
        this.points = new ArrayList<Point2D>();
        this.points.addAll(points);
    }

    public void addAllPoints(List<Point2D> points) {
        this.points.addAll(points);
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new SPoly();
        }
        ((SPoly)dupe).addAllPoints(this.getPoints());
        ((SPoly)dupe).setClosed(this.isClosed());
        ((SPoly)dupe).recalcPath();
        return super.duplicate(dupe);
    }

    @Override
    public Area toArea() {
        Area area = new Area(path);
        area.transform(AffineTransform.getTranslateInstance(getTranslateX(),getTranslateY()));
        return area;
    }

    @Override
    public SPath toPath() {
        SPath path = new SPath();
        for(int i=0; i<this.pointCount();i++) {
            Point2D pt = this.getPoint(i);
            if(i==0) {
                path.moveTo(pt.getX(),pt.getY());
            } else {
                path.lineTo(pt.getX(),pt.getY());
            }
        }
        if(this.isClosed()) {
            path.close(true);
        }
        path.setTranslateX(this.getTranslateX());
        path.setTranslateY(this.getTranslateY());
        path.setFillPaint(this.getFillPaint());
        path.setFillOpacity(this.getFillOpacity());
        path.setStrokeWidth(this.getStrokeWidth());
        path.setStrokePaint(this.getStrokePaint());
        return path;
    }

    @Override
    protected void fillShape(GFX g) {
        double[] points = new double[this.pointCount()*2];
        for(int i=0; i<this.pointCount(); i++) {
            points[i*2] = this.getPoint(i).getX();
            points[i*2+1] = this.getPoint(i).getY();
        }
        if(this.isClosed()) {
            g.fillPolygon(points);
        } else {
            g.drawPolygon(points,false);
        }
    }

    public void draw(GFX g) {
        drawShadow(g);

        Paint paint = this.getFillPaint();
        if(paint != null) {
            if(paint instanceof FlatColor) {
                g.setPaint(((FlatColor)paint).deriveWithAlpha(getFillOpacity()));
            }
            if(paint instanceof MultiGradientFill) {
                MultiGradientFill gf = (MultiGradientFill) paint;
                gf = gf.translate(bounds.getX(),bounds.getY());
                g.setPaint(gf);
            }
            if(paint instanceof PatternPaint) {
                g.setPaint(paint);
            }
        }

        fillShape(g);

        double[] points = new double[this.pointCount()*2];
        for(int i=0; i<this.pointCount(); i++) {
            points[i*2] = this.getPoint(i).getX();
            points[i*2+1] = this.getPoint(i).getY();
        }
        g.setPaint(this.getStrokePaint());
        g.setStrokeWidth(this.getStrokeWidth());
        if(this.isClosed()) {
            g.drawPolygon(points,true);
        } else {
            g.drawPolygon(points,false);
        }
        g.setStrokeWidth(1);
    }

    public Bounds getTransformedBounds() {
        AffineTransform af = new AffineTransform();
        af.translate(getTranslateX(),getTranslateY());
        af.translate(getAnchorX(),getAnchorY());
        af.rotate(Math.toRadians(getRotate()));
        af.scale(getScaleX(), getScaleY());
        af.translate(-getAnchorX(),-getAnchorY());
        Shape sh = af.createTransformedShape(path);
        Rectangle2D bds = sh.getBounds2D();
        return Util.toBounds(bds);
    }
}
