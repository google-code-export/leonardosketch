package org.joshy.sketch.model;

import org.joshy.gfx.node.Bounds;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

/**
 * The base node for all graphics. Contains standard transforms for translate, rotate, scale
 * as well as a function to clone nodes.
 */
public abstract class SNode {
    private double translateX = 0.0;
    private double translateY = 0.0;
    private Map<String,String> props = new HashMap<String,String>();
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double rotate = 0.0;
    private String id = null;

    public abstract Bounds getBounds();

    public abstract boolean contains(Point2D point);

    public double getTranslateX() {
        return translateX;
    }

    public void setTranslateX(double translateX) {
        this.translateX = translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    public void setTranslateY(double translateY) {
        this.translateY = translateY;
    }

    public SNode duplicate(SNode dupe) {
        if(dupe == null) throw new IllegalArgumentException("SShape.duplicate: duplicate shape argument can't be null!");
        dupe.setTranslateX(this.getTranslateX());
        dupe.setTranslateY(this.getTranslateY());
        return dupe;
    }

    public String getStringProperty(String key) {
        return props.get(key);
    }

    public void setStringProperty(String key, String value) {
        props.put(key,value);
    }

    public Map getProperties() {
        return props;
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }

    public void setRotate(double rotate) {
        this.rotate = rotate;
    }

    public double getRotate() {
        return rotate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
