package org.joshy.sketch.model;

import org.joshy.gfx.draw.GFX;
import org.joshy.sketch.canvas.SketchCanvas;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 13, 2010
 * Time: 5:41:29 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Handle {
    protected double size = 5;

    public boolean contains(Point2D cursor) {
        return contains(cursor.getX(),cursor.getY());
    }

    public boolean contains(double x, double y) {
        if(x >= getX()-size && x <= getX()+size) {
            if(y >= getY()-size && y <= getY()+size) {
                return true;
            }
        }
        return false;
    }

    public abstract double getX();

    public abstract void setX(double x, boolean constrain);

    public abstract double getY();

    public abstract void setY(double y, boolean constrain);

    public abstract void draw(GFX g, SketchCanvas sketchCanvas);
}