package org.joshy.sketch.model;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Transform;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.u;
import org.joshy.sketch.util.Util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SGroup extends SNode implements SelfDrawable {
    private List<SNode> nodes;
    private double boundsWidth;
    private double boundsHeight;

    public SGroup() {
        nodes = new ArrayList<SNode>();
    }

    public void addAll(SNode ... nodes) {
        addAll(Arrays.asList(nodes));
    }
    public void addAll(List<SNode> nodes) {
        double x = Double.MAX_VALUE;
        double y = Double.MAX_VALUE;
        double x2 = Double.MIN_VALUE;
        double y2 = Double.MIN_VALUE;

        for(SNode node : nodes) {
            Bounds bounds = node.getTransformedBounds();
            if(bounds == null) {
                u.p("waring. null bounds: " + node);
            }
            x = Math.min(x,bounds.getX());
            y = Math.min(y,bounds.getY());
            x2 = Math.max(x2,bounds.getX()+bounds.getWidth());
            y2 = Math.max(y2,bounds.getY()+bounds.getHeight());
        }

        this.nodes.addAll(nodes);

        this.boundsWidth = x2-x;
        this.boundsHeight = y2-y;

        setTranslateX(x);
        setTranslateY(y);
        for(SNode node : nodes) {
            node.setTranslateX(node.getTranslateX()-x);
            node.setTranslateY(node.getTranslateY()-y);
        }
        setAnchorX(boundsWidth/2);
        setAnchorY(boundsHeight/2);
    }

    public void normalize() {
        double x = Double.MAX_VALUE;
        double y = Double.MAX_VALUE;
        double x2 = Double.MIN_VALUE;
        double y2 = Double.MIN_VALUE;

        for(SNode node : nodes) {
            x = Math.min(x,node.getBounds().getX());
            y = Math.min(y,node.getBounds().getY());
            x2 = Math.max(x2,node.getBounds().getX()+node.getBounds().getWidth());
            y2 = Math.max(y2,node.getBounds().getY()+node.getBounds().getHeight());
        }

        this.boundsWidth = x2-x;
        this.boundsHeight = y2-y;

        setTranslateX(x);
        setTranslateY(y);
        for(SNode node : nodes) {
            node.setTranslateX(node.getTranslateX()-x);
            node.setTranslateY(node.getTranslateY()-y);
        }
    }
    
    public void addAll(boolean normalize, SNode node) {
        if(normalize) {
            addAll(node);
        } else {
            this.nodes.add(node);
        }
    }

    public List<? extends SNode> getNodes() {
        return nodes;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(getTranslateX(),getTranslateY(),boundsWidth,boundsHeight);
    }

    @Override
    public Bounds getTransformedBounds() {
        java.awt.geom.Rectangle2D r = new Rectangle2D.Double(0,0,boundsWidth,boundsHeight);
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
        return getBounds().contains(point.getX(),point.getY());
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new SGroup();
        }
        List<SNode> subdupes = new ArrayList<SNode>();
        for(SNode nd : getNodes()) {
            subdupes.add(nd.duplicate(null));
        }
        ((SGroup)dupe).addAll(subdupes);
        return super.duplicate(dupe);
    }

    public void draw(GFX g) {
        for(SNode node : this.getNodes()) {
            g.translate(node.getTranslateX(),node.getTranslateY());
            g.translate(node.getAnchorX(),node.getAnchorY());
            g.scale(node.getScaleX(),node.getScaleY());
            g.rotate(node.getRotate(), Transform.Z_AXIS);
            g.translate(-node.getAnchorX(),-node.getAnchorY());
            if(node instanceof SelfDrawable) {
                ((SelfDrawable)node).draw(g);
            }
            g.translate(node.getAnchorX(),node.getAnchorY());
            g.rotate(-node.getRotate(), Transform.Z_AXIS);
            g.scale(1/node.getScaleX(),1/node.getScaleY());
            g.translate(-node.getAnchorX(),-node.getAnchorY());
            g.translate(-node.getTranslateX(),-node.getTranslateY());
        }
    }

}
