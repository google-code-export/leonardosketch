package org.joshy.gfx.node.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.css.CSSSkin;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * The base of all UI controls. It defines the layout contract, and
 * size values
 */
public abstract class Control extends Node {
    public static final double CALCULATED = -1;

    protected double width = 0;
    protected double height = 0;
    protected double prefWidth = CALCULATED;
    protected double prefHeight = CALCULATED;
    
    protected boolean skinsDirty;
    protected boolean layoutDirty;
    protected CSSSkin cssSkin;
    private String id;
    protected Set<String> cssClasses = new HashSet<String>();

    protected Control() {
        setSkinDirty();
        populateCSSClasses();
    }

    private void populateCSSClasses() {
        Class clz = this.getClass();
        while(true) {
            cssClasses.add("-class-"+clz.getName().replace(".","-"));
            cssClasses.add("-class-"+clz.getSimpleName().replace(".","-"));
            if(clz == Control.class) break;
            clz = clz.getSuperclass();
        }
    }

    public Control setWidth(double width) {
        this.width = width;
        setLayoutDirty();
        setDrawingDirty();
        return this;
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

    public Control setPrefWidth(double width) {
        this.prefWidth = width;
        return this;
    }

    public Control setPrefHeight(double height) {
        this.prefHeight = height;
        return this;
    }

    @Override
    public Bounds getInputBounds() {
        return getVisualBounds();
    }

    @Override
    public Bounds getVisualBounds() {
        return new Bounds(getTranslateX(),getTranslateY(),getWidth(),getHeight());
    }

    /** returns the bounds to be used for layout. All layouts and panels should use
     * these bounds, not the visual bounds. Visual bounds are only used for repainting.
     *
      * @return
     */
    public Bounds getLayoutBounds() {
        return getVisualBounds();
    }

    /** returns the baseline of this control, in the coordinate system returned by
     * getLayoutBounds().  A control with no particular baseline should just return
     * the height.
     *
     * @return
     */
    public double getBaseline() {
        return getLayoutBounds().getHeight();
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

    /** the control should calculate it's layout bounds,
     * caching as much info as possible so that drawing can be fast.
     */
    public abstract void doLayout();

    /** do pref layout
     *
     */
    public void doPrefLayout() { }

    /** the control should load up it's skins, caching as much
     * info as possible so that drawing and layout can be fast.
     */
    public abstract void doSkins();

    public String getId() {
        return id;
    }

    public Control setId(String id) {
        this.id = id;
        return this;
    }

    public Set<String> getCSSClasses() {
        return this.cssClasses;
    }

    public double getPrefWidth() {
        return prefWidth;
    }

    public double getPrefHeight() {
        return prefHeight;
    }
}


