package org.joshy.sketch.model;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.node.Bounds;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: May 13, 2010
* Time: 10:45:00 PM
* To change this template use File | Settings | File Templates.
*/
public class NGon extends SShape implements SelfDrawable {
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

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public int getSides() {
        return sides;
    }

    public void setSides(int sides) {
        this.sides = sides;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    protected void fillShape(GFX g) {
        double[] points = toPoints();
        g.fillPolygon(points);
    }

    public void draw(GFX g) {
        drawShadow(g);

        Paint paint = this.getFillPaint();
        if(paint != null) {
            if(paint instanceof FlatColor) {
                g.setPaint(((FlatColor)paint).deriveWithAlpha(getFillOpacity()));
            }
            if(paint instanceof GradientFill) {
                GradientFill gf = (GradientFill) paint;
                gf = gf.translate(-getRadius(),-getRadius());
                g.setPaint(gf);
            }
            if(paint instanceof PatternPaint) {
                g.setPaint(paint);
            }
        }

        fillShape(g);
        g.setPaint(getStrokePaint());


        g.setStrokeWidth(getStrokeWidth());
        g.drawPolygon(toPoints(),true);
        g.setStrokeWidth(1);
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngle() {
        return angle;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public boolean isStar() {
        return star;
    }

    public double getInnerRadius() {
        return innerRadius;
    }

    public void setInnerRadius(double innerRadius) {
        this.innerRadius = innerRadius;
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
                points[i*4] =   (int)Math.round(or*Math.cos(angle))+x;
                points[i*4+1] = (int)Math.round(or*Math.sin(angle))+y;
                points[i*4+2] = (int)Math.round(ir*Math.cos(angle+addAngle/2))+x;
                points[i*4+3] = (int)Math.round(ir*Math.sin(angle+addAngle/2))+y;
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
            double resx = (int)Math.round(r*Math.cos(angle))+x;
            double resy = (int)Math.round(r*Math.sin(angle))+y;
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
        Area area = new Area(poly);
        area.transform(AffineTransform.getTranslateInstance(getTranslateX(),getTranslateY()));
        return area;
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new NGon(getSides());
        }
        ((NGon)dupe).setRadius(getRadius());
        ((NGon)dupe).setAngle(getAngle());
        ((NGon)dupe).setStar(isStar());
        return super.duplicate(dupe);
    }

}
