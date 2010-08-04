package org.joshy.gfx.node.shape;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.shape.Shape;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Feb 1, 2010
 * Time: 11:35:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class Oval extends Shape {
    
    private double width = 100;
    private double height = 100;

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public void draw(GFX g) {
        g.setPaint(getFill());
        g.fillOval(0,0,getWidth(),getHeight());
        g.setPaint(getStroke());
        g.drawOval(0,0,getWidth(),getHeight());
    }

    @Override
    public Bounds getVisualBounds() {
        return new Bounds(getTranslateX(),getTranslateY(),getWidth(),getHeight());
    }

    @Override
    public Bounds getInputBounds() {
        return getVisualBounds();
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
