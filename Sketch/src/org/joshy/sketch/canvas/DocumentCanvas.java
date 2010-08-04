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
    protected double panX = 0;
    protected double panY = 0;

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
        setDrawingDirty();
    }

    public void setPanX(double panX) {
        this.panX = panX;
        setDrawingDirty();
    }

    public void setPanY(double panY) {
        this.panY = panY;
        setDrawingDirty();
    }

    public void redraw() {
        setDrawingDirty();
    }

    public double getPanX() {
        return panX;
    }

    public double getPanY() {
        return panY;
    }

    public Point2D.Double transformToCanvas(double x, double y) {
        return new Point2D.Double(
                (x-panX)/scale,
                (y-panY)/scale);
    }

    public Point2D.Double transformToCanvas(Point2D point) {
        return new Point2D.Double(
                (point.getX()-panX)/scale,
                (point.getY()-panY)/scale);
    }

    public Point2D.Double transformToDrawing(double x, double y) {
        return new Point2D.Double(
                x*scale+panX,
                y*scale+panY);
    }

    public Bounds transformToDrawing(Bounds bounds) {
        return new Bounds(
                bounds.getX()*scale+panX,
                bounds.getY()*scale+panY,
                bounds.getWidth()*scale,
                bounds.getHeight()*scale);
    }

    public Point2D.Double transformToDrawing(Point2D point) {
        return new Point2D.Double(
                point.getX()*scale+panX,
                point.getY()*scale+panY
        );
    }
}
