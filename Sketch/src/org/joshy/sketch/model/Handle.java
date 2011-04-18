package org.joshy.sketch.model;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.control.Control;
import org.joshy.sketch.canvas.SketchCanvas;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 13, 2010
 * Time: 5:41:29 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Handle {
    protected double size = 5;

    public final boolean contains(Point2D cursor, double scale) {
        return contains(cursor.getX(),cursor.getY(), scale);
    }

    public boolean contains(double x, double y, double scale) {
        double size = this.size/scale;
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

    public boolean hasControls() {
        return false;
    }

    public Iterable<? extends Control> getControls() {
        return new ArrayList<Control>();
    }

    public void detach() {

    }

    protected double snapX(double x, double value) {
        if(Math.abs(x-value)<5) {
            x = value;
        }
        return x;
    }

    protected double snapY(double y, double value) {
        if(Math.abs(y-value)<5) {
            y = value;
        }
        return y;
    }
}
