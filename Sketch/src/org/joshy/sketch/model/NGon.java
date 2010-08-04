package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Transform;
import org.joshy.gfx.node.Bounds;

import java.awt.geom.Path2D;
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
    private double radius;
    private double angle;

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

    public void draw(GFX g) {
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

        g.setPaint(getFillPaint());
        g.fillPolygon(points);
        g.setPaint(getStrokePaint());
        g.setStrokeWidth(getStrokeWidth());
        g.drawPolygon(points,true);
        g.setStrokeWidth(1);
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new NGon(getSides());
        }
        ((NGon)dupe).setRadius(getRadius());
        ((NGon)dupe).setAngle(getAngle());
        return super.duplicate(dupe);
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngle() {
        return angle;
    }
}
