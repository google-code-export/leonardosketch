package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Transform;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.gfx.util.u;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.canvas.SketchCanvas;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 9/29/11
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class STransformNode extends SNode implements SelfDrawable {
    private SNode child;
    private double angle = 0;
    private double scy = 1;
    private double scx = 1;
    private double radius = 50;
    private boolean dead = false;


    public STransformNode(final SNode node, final VectorDocContext context) {
        this.child = node;
        this.setAnchorX(0);
        this.setAnchorY(0);
        Bounds bounds = this.child.getBounds();
        if(this.child instanceof HasTransformedBounds) {
            bounds = ((HasTransformedBounds)this.child).getTransformedBounds();
        }
        final double w = 0;
        final double h = 0;
        this.setTranslateX(this.child.getTranslateX() + w);
        this.setTranslateY(this.child.getTranslateY() + h);
        angle = -child.getRotate();
        scx = child.getScaleX();
        scy = child.getScaleY();
        this.child.setTranslateX(0 - w);
        this.child.setTranslateY(0 - h);

        EventBus.getSystem().addListener(Selection.SelectionChangeEvent.Changed, new Callback<Selection.SelectionChangeEvent>(){
            public void call(Selection.SelectionChangeEvent selectionChangeEvent) throws Exception {
                if(dead) return;
                if(selectionChangeEvent.getSelection().contains(STransformNode.this)) return;
                u.p("selection changed. i've been unselected");
                dead = true;
                context.getDocument().getCurrentPage().remove(STransformNode.this);
                context.getDocument().getCurrentPage().add(child);
                child.setTranslateX(getTranslateX() - w);
                child.setTranslateY(getTranslateY() - h);
                child.setRotate(-angle);
                child.setScaleX(scx);
                child.setScaleY(scy);
                context.redraw();
            }
        });

    }

    @Override
    public Bounds getBounds() {
        return new Bounds(getTranslateX()-radius,getTranslateY()-radius,radius*2,radius*2);
    }

    @Override
    public boolean contains(Point2D point) {
        return getBounds().contains(point);
    }

    private void setAngle(double a) {
        this.angle = a;
    }

    public void draw(GFX g) {

        double a = angle;


        g.translate(child.getAnchorX(),child.getAnchorY());
        g.scale(scx, scy);
        g.rotate(-a, Transform.Z_AXIS);
        g.translate(-child.getAnchorX(),-child.getAnchorY());

        ((SelfDrawable)child).draw(g);


        g.translate(child.getAnchorX(),child.getAnchorY());
        g.rotate(a, Transform.Z_AXIS);
        g.scale(1.0 / scx, 1.0 / scy);
        g.translate(-child.getAnchorX(),-child.getAnchorY());

        g.translate(child.getAnchorX(),child.getAnchorY());
        g.setPaint(FlatColor.BLUE);
        radius = 50.0*scy;
        g.drawOval(0-radius, 0-radius, radius*2, radius*2);
        Point2D pt2 = GeomUtil.calcPoint(new Point(0, 0), angle+90, radius+20);
        g.drawLine(0,0,pt2.getX(),pt2.getY());
        g.drawLine(0,0,0,+radius);
        g.translate(-child.getAnchorX(),-child.getAnchorY());
    }

    public static class TransformScaleHandle extends Handle {
        private STransformNode trans;

        public TransformScaleHandle(STransformNode trans) {
            this.trans = trans;
        }

        @Override
        public double getX() {
            return this.trans.getTranslateX() + trans.child.getAnchorX();
        }

        @Override
        public void setX(double x, boolean constrain) {
        }

        @Override
        public double getY() {
            return trans.getTranslateY()+50*trans.scy + trans.child.getAnchorY();
        }

        @Override
        public void setY(double y, boolean constrain) {
            double ty = y-this.trans.getTranslateY()-trans.child.getAnchorY();
            trans.scy = ((ty)/50.0);
            trans.scx = trans.scy;
        }

        @Override
        public void draw(GFX g, SketchCanvas sketchCanvas) {
            g.setPaint(FlatColor.GREEN);
            Point2D pt = sketchCanvas.transformToDrawing(getX(),getY());

            g.drawOval(pt.getX() - 5, pt.getY() - 5, 10, 10);
        }
    }


    public static class TransformRotateHandle extends Handle {
        private STransformNode trans;
        double x = 100;
        double y = 50;

        public TransformRotateHandle(STransformNode trans) {
            this.trans = trans;
        }

        @Override
        public double getX() {
            double radius = 50.0*trans.scy;
            Point2D pt2 = GeomUtil.calcPoint(new Point(0, 0), trans.angle+90, radius);
            return trans.getTranslateX() + pt2.getX() + trans.child.getAnchorX();
        }

        @Override
        public void setX(double x, boolean constrain) {
            this.x = x - trans.getTranslateX() - trans.child.getAnchorX();
            double a = GeomUtil.calcAngle(new Point2D.Double(0,0),new Point2D.Double(this.x,this.y));
            trans.setAngle(Math.toDegrees(a)-90);
        }

        @Override
        public double getY() {
            double radius = 50.0*trans.scy;
            Point2D pt2 = GeomUtil.calcPoint(new Point(0, 0), trans.angle+90, radius);
            return this.trans.getTranslateY() + pt2.getY() + trans.child.getAnchorY();
        }

        @Override
        public void setY(double y, boolean constrain) {
            this.y = y - this.trans.getTranslateY() - trans.child.getAnchorY();
        }

        @Override
        public void draw(GFX g, SketchCanvas sketchCanvas) {
            g.setPaint(FlatColor.GREEN);
            Point2D pt = sketchCanvas.transformToDrawing(getX(),getY());
            g.drawOval(pt.getX() - 5, pt.getY() - 5, 10, 10);
        }
    }

}
