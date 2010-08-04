package org.joshy.gfx.node;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.util.u;

public abstract class Node {
    protected Parent parent;
    protected boolean drawingDirty = true;
    private double translateZ;
    protected boolean visible = true;
    protected double opacity = 1.0;

    public void setParent(Parent parent) {
        this.parent = parent;
        setDrawingDirty();
    }

    public abstract void draw(GFX g);

    protected void setDrawingDirty() {
        Core.getShared().assertGUIThread();
        this.drawingDirty = true;
        if(parent != null) {
            parent.setDrawingDirty(this);
        }
    }

    private double translateX;

    public double getTranslateX() {
        return this.translateX;
    }

    public void setTranslateX(double translateX) {
        this.translateX = translateX;
        setDrawingDirty();
    }
    

    private double translateY;

    public double getTranslateY() {
        return translateY;
    }

    public void setTranslateY(double translateY) {
        this.translateY = translateY;
        setDrawingDirty();
    }
    
    public abstract Bounds getVisualBounds();


    public abstract Bounds getInputBounds();

    public Parent getParent() {
        return parent;
    }

    public void setTranslateZ(double translateZ) {
        this.translateZ = translateZ;
        setDrawingDirty();
    }

    public double getTranslateZ() {
        return translateZ;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        setDrawingDirty();
    }


    public void setOpacity(double opacity) {
        this.opacity = opacity;
        setDrawingDirty();
    }
}
