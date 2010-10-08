package org.joshy.sketch.canvas;

import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.Focusable;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 8, 2010
 * Time: 5:10:47 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class DocumentCanvas extends Control implements Focusable {
    protected double scale = 1;

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
        setLayoutDirty();
        setDrawingDirty();
    }

    public void redraw() {
        setDrawingDirty();
    }


    public Point2D.Double transformToCanvas(double x, double y) {
        return new Point2D.Double(
                (x)/scale,
                (y)/scale);
    }

    public Point2D.Double transformToCanvas(Point2D point) {
        return new Point2D.Double(
                (point.getX())/scale,
                (point.getY())/scale);
    }

    public Point2D.Double transformToDrawing(double x, double y) {
        return new Point2D.Double(
                x*scale,
                y*scale);
    }

    public Bounds transformToDrawing(Bounds bounds) {
        return new Bounds(
                bounds.getX()*scale,
                bounds.getY()*scale,
                bounds.getWidth()*scale,
                bounds.getHeight()*scale);
    }

    public Point2D.Double transformToDrawing(Point2D point) {
        return new Point2D.Double(
                point.getX()*scale,
                point.getY()*scale
        );
    }
}
