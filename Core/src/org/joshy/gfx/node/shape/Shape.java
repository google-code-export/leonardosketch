package org.joshy.gfx.node.shape;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.draw.Transform;
import org.joshy.gfx.node.Node;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Feb 8, 2010
 * Time: 2:53:37 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Shape extends Node {
    protected Paint fill = FlatColor.BLACK;
    private Transform axis = Transform.Z_AXIS;
    private double rotation = 0;
    protected Paint stroke = FlatColor.BLACK;

    public Shape() {
        fill = new FlatColor(0,0,0,1);
    }

    public void setFill(Paint fillPaint) {
        this.fill = fillPaint;

    }

    public void setRotationAxis(Transform axis) {
        this.axis = axis;
    }

    public Transform getRotationAxis() {
        return axis;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public void setStroke(Paint strokePaint) {
        this.stroke = strokePaint;
    }

    protected Paint getFill() {
        return this.fill;
    }

    protected Paint getStroke() {
        return this.stroke;
    }
}
