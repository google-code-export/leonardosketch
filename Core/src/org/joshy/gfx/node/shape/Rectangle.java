package org.joshy.gfx.node.shape;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.draw.Transform;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.shape.Shape;
import org.joshy.gfx.util.u;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 18, 2010
 * Time: 2:04:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Rectangle extends Shape {

    private double x;
    private double y;
    private double width;
    private double height;

    public Rectangle() {
        super();
        width = 100.0;
        height = 100.0;
        x = 0.0;
        y = 0.0;
    }
    
    public double getWidth() {
        return width;
    }

    public Rectangle setWidth(double width) {
        this.width = width;
        setDrawingDirty();
        return this;
    }

    public double getHeight() {
        return height;
    }

    public Rectangle setHeight(double height) {
        this.height = height;
        setDrawingDirty();
        return this;
    }

    public double getY() {
        return y;
    }

    public Rectangle setY(double y) {
        this.y = y;
        setDrawingDirty();
        return this;
    }

    public double getX() {
        return x;
    }

    public Rectangle setX(double x) {
        this.x = x;
        setDrawingDirty();
        return this;
    }

    @Override
    public Bounds getVisualBounds() {
        return new Bounds(getTranslateX()+getX(),getTranslateY()+getY(),getWidth(),getHeight());
    }

    @Override
    public Bounds getInputBounds() {
        return getVisualBounds();
    }

    @Override
    public void draw(GFX g) {
        g.setPaint(fill);
        g.fillRect(x,y,width,height);
        g.setPaint(stroke);
        g.drawRect(x+0.5,y+0.5,width-1,height-1);
    }

}
