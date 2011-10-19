package org.joshy.sketch.model;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.node.Bounds;
import org.joshy.sketch.util.Util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: May 13, 2010
* Time: 10:45:00 PM
* To change this template use File | Settings | File Templates.
*/
public class NGon extends SShape implements SelfDrawable, HasTransformedBounds {
    private int sides;
    private double radius = 50;
    private double innerRadius = 40;
    private double angle;
    private boolean star;

    public NGon(int sides) {
        this.sides = sides;
        setFillPaint(FlatColor.GRAY);
        setStrokePaint(FlatColor.BLACK);
    }
    
    public NGon() {
        this(5);
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(
                getTranslateX()-getRadius(),
                getTranslateY()-getRadius(),
                getRadius()*2,getRadius()*2);
    }

    @Override
    public boolean contains(Point2D point) {
        return getBounds().contains(point.getX(),point.getY());
    }

    public NGon setRadius(double radius) {
        this.radius = radius;
        return this;
    }

    public int getSides() {
        return sides;
    }

    public NGon setSides(int sides) {
        this.sides = sides;
        return this;
    }

    public double getRadius() {
        return radius;
    }

    public NGon setAngle(double angle) {
        this.angle = angle;
        return this;
    }

    public double getAngle() {
        return angle;
    }

    public NGon setStar(boolean star) {
        this.star = star;
        return this;
    }

    public boolean isStar() {
        return star;
    }

    public double getInnerRadius() {
        return innerRadius;
    }

    public NGon setInnerRadius(double innerRadius) {
        this.innerRadius = innerRadius;
        return this;
    }

    @Override
    protected void fillShape(GFX g) {
        double[] points = toPoints();
        g.fillPolygon(points);
    }

    public void draw(GFX g) {
        drawShadow(g);

        Paint paint = this.getFillPaint();
        double opacity = -1;
        if(paint != null) {
            if(paint instanceof FlatColor) {
                g.setPaint(((FlatColor)paint).deriveWithAlpha(getFillOpacity()));
            }
            if(paint instanceof MultiGradientFill) {
                MultiGradientFill gf = (MultiGradientFill) paint;
                gf = gf.translate(-getRadius(),-getRadius());
                g.setPaint(gf);
            }
            if(paint instanceof PatternPaint) {
                opacity = g.getOpacity();
                g.setOpacity(getFillOpacity());
                g.setPaint(paint);
            }
        }

        fillShape(g);
        if(opacity >=0) g.setOpacity(opacity);


        if(getStrokePaint() != null && getStrokeWidth() > 0) {
            g.setPaint(getStrokePaint());
            g.setStrokeWidth(getStrokeWidth());
            g.drawPolygon(toPoints(),true);
            g.setStrokeWidth(1);
        }
    }

    public double[] toPoints() {
        if(isStar()) {
            double[] points = new double[getSides()*4];
            double addAngle=2*Math.PI/getSides();
            double angle= -this.angle;
            double x = 0;
            double y = 0;
            double or = getRadius();
            double ir = getInnerRadius();
            for (int i=0; i<getSides(); i++) {
                points[i*4] =   (or*Math.cos(angle))+x;
                points[i*4+1] = (or*Math.sin(angle))+y;
                points[i*4+2] = (ir*Math.cos(angle+addAngle/2))+x;
                points[i*4+3] = (ir*Math.sin(angle+addAngle/2))+y;
                angle+=addAngle;
            }
            return points;
        }
        double[] points = new double[getSides()*2];
        double addAngle=2*Math.PI/getSides();
        double angle= -this.angle;
        double x = 0;
        double y = 0;
        double r = getRadius();
        for (int i=0; i<getSides(); i++) {
            double resx = r*Math.cos(angle)+x;
            double resy = r*Math.sin(angle)+y;
            angle+=addAngle;
            points[i*2] = resx;
            points[i*2+1] = resy;
        }
        return points;
    }

    @Override
    public Area toArea() {
        Polygon poly = new Polygon();
        double[] points = toPoints();
        for(int i=0; i<points.length; i+=2) {
            poly.addPoint((int)points[i],(int)points[i+1]);
        }
        return new Area(transformShape(poly));
    }

    @Override
    public SPath toPath() {
        SPath path = new SPath();
        double[] points = toPoints();
        for(int i=0; i<points.length; i+=2) {
            if(i == 0) {
                path.moveTo(points[i],points[i+1]);
            } else {
                path.lineTo(points[i],points[i+1]);
            }
        }
        path.close(true);

        path.setTranslateX(this.getTranslateX());
        path.setTranslateY(this.getTranslateY());
        path.setFillPaint(this.getFillPaint());
        path.setFillOpacity(this.getFillOpacity());
        path.setStrokeWidth(this.getStrokeWidth());
        path.setStrokePaint(this.getStrokePaint());
        return path;
    }

    public Bounds getTransformedBounds() {
        Polygon poly = new Polygon();
        double[] points = toPoints();
        for(int i=0; i<points.length; i+=2) {
            poly.addPoint((int)points[i],(int)points[i+1]);
        }

        AffineTransform af = new AffineTransform();
        af.translate(getTranslateX(),getTranslateY());
        af.translate(getAnchorX(),getAnchorY());
        af.rotate(Math.toRadians(getRotate()));
        af.scale(getScaleX(), getScaleY());
        af.translate(-getAnchorX(),-getAnchorY());
        Shape sh = af.createTransformedShape(poly);
        Rectangle2D bds = sh.getBounds2D();
        return Util.toBounds(bds);
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new NGon(getSides());
        }
        ((NGon)dupe).setRadius(getRadius());
        ((NGon)dupe).setInnerRadius(getInnerRadius());
        ((NGon)dupe).setAngle(getAngle());
        ((NGon)dupe).setStar(isStar());
        return super.duplicate(dupe);
    }

}
