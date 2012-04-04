package org.joshy.sketch.modes.powerup;

import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.Togglebutton;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.sketch.model.CustomProperties;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SResizeableNode;
import org.joshy.sketch.util.Util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 4/4/12
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class FXAbstractComponent extends SNode implements CustomProperties {
    boolean rightAnchored = false;
    boolean leftAnchored = false;
    boolean topAnchored = false;
    boolean bottomAnchored = false;
    protected double x;
    protected double y;
    protected double w;
    protected double h;

    public FXAbstractComponent() {
        this.w = 50;
        this.h = 20;
        this.y = 0;
        this.x = 0;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(x,y,w,h);
    }

    @Override
    public Bounds getTransformedBounds() {
        java.awt.geom.Rectangle2D r = new Rectangle2D.Double(getX(),getY(),getWidth(),getHeight());
        AffineTransform af = new AffineTransform();
        af.translate(getTranslateX(),getTranslateY());

        af.translate(getAnchorX(),getAnchorY());
        af.rotate(Math.toRadians(getRotate()));
        af.scale(getScaleX(), getScaleY());
        af.translate(-getAnchorX(),-getAnchorY());

        Shape sh = af.createTransformedShape(r);
        Rectangle2D bds = sh.getBounds2D();
        return Util.toBounds(bds);
    }

    @Override
    public boolean contains(Point2D point) {
        double x = getX() + getTranslateX();
        if(point.getX() >= x && point.getX() <= x + getWidth()) {
            double y = getY() + getTranslateY();
            if(point.getY() >= y && point.getY() <= y + this.getHeight()) {
                return true;
            }
        }
        return false;
    }

    public double getX() {
        return this.x;
    }

    public double getWidth() {
        return this.w;
    }

    public void setWidth(double width) {
        this.w = width;
    }

    public double getHeight() {
        return this.h;
    }

    public void setHeight(double height) {
        this.h = height;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getPreferredAspectRatio() {
        return 1;
    }

    public boolean constrainByDefault() {
        return false;
    }

    @Override
    public SNode duplicate(SNode dupe) {
        FXAbstractComponent sdupe = (FXAbstractComponent) dupe;
        sdupe.leftAnchored = this.leftAnchored;
        sdupe.rightAnchored = this.rightAnchored;
        sdupe.topAnchored = this.topAnchored;
        sdupe.bottomAnchored = this.bottomAnchored;
        sdupe.setX(this.getX());
        sdupe.setY(this.getY());
        sdupe.setWidth(this.getWidth());
        sdupe.setHeight(this.getHeight());
        return super.duplicate(dupe);
    }

    public Iterable<Control> getControls() {
        java.util.List<Control> l = new ArrayList<Control>();
        HFlexBox row = new HFlexBox();

        final Togglebutton left = new Togglebutton("<");
        left.setSelected(leftAnchored);
        left.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) throws Exception {
                leftAnchored = left.isSelected();
            }
        });
        row.add(left);

        final Togglebutton right = new Togglebutton(">");
        right.setSelected(rightAnchored);
        right.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) throws Exception {
                rightAnchored = right.isSelected();
            }
        });
        row.add(right);

        final Togglebutton top = new Togglebutton("^");
        top.setSelected(topAnchored);
        top.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) throws Exception {
                topAnchored = top.isSelected();
            }
        });
        row.add(top);

        final Togglebutton bottom = new Togglebutton("v");
        bottom.setSelected(bottomAnchored);
        bottom.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) throws Exception {
                bottomAnchored = bottom.isSelected();
            }
        });
        row.add(bottom);


        l.add(row);

        return l;
    }
}
