package org.joshy.gfx.node.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.css.CSSSkin;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Node;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 23, 2010
 * Time: 7:59:09 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Control extends Node {
    
    protected double width;
    protected double height;
    protected boolean skinsDirty;
    protected boolean layoutDirty;
    protected CSSSkin cssSkin;
    protected String id;
    protected List<String> cssClasses;

    protected Control() {
        setSkinDirty();
    }

    public Control setWidth(double width) {
        this.width = width;
        setLayoutDirty();
        setDrawingDirty();
        return this;
    }

    protected void setLayoutDirty() {
        Core.getShared().assertGUIThread();
        this.layoutDirty = true;
        if(parent != null) {
            parent.setLayoutDirty(this);
        }
    }

    protected void setSkinDirty() {
        Core.getShared().assertGUIThread();
        this.skinsDirty = true;
        if(parent != null) {
            parent.setSkinDirty(this);
        }
    }

    public Control setHeight(double height) {
        this.height = height;
        setLayoutDirty();
        setDrawingDirty();
        return this;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    /** the control should calculate it's layout bounds,
     * caching as much info as possible so that drawing can be fast.
     */
    public abstract void doLayout();

    /** the control should load up it's skins, caching as much
     * info as possible so that drawing and layout can be fast.
     */
    public abstract void doSkins();

    @Override
    public Bounds getInputBounds() {
        return getVisualBounds();
    }

    @Override
    public Bounds getVisualBounds() {
        return new Bounds(getTranslateX(),getTranslateY(),getWidth(),getHeight());
    }

    public Bounds getLayoutBounds() {
        return getVisualBounds();
    }
}


