package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Transform;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.gfx.util.u;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.canvas.SketchCanvas;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;

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
    private VectorDocContext context;


    public STransformNode(final SNode node, final VectorDocContext context) {
        this.child = node;
        this.context = context;
        this.setAnchorX(0);
        this.setAnchorY(0);
        this.setTranslateX(this.child.getTranslateX());
        this.setTranslateY(this.child.getTranslateY());
        angle = -child.getRotate();
        scx = child.getScaleX();
        scy = child.getScaleY();
        this.child.setTranslateX(0);
        this.child.setTranslateY(0);

        EventBus.getSystem().addListener(Selection.SelectionChangeEvent.Changed, new Callback<Selection.SelectionChangeEvent>(){
            public void call(Selection.SelectionChangeEvent selectionChangeEvent) throws Exception {
                if(dead) return;
                if(selectionChangeEvent.getSelection().contains(STransformNode.this)) return;
                finishTransform();
            }
        });

    }

    private void finishTransform() {
        dead = true;
        context.getDocument().getCurrentPage().remove(STransformNode.this);
        context.getDocument().getCurrentPage().add(child);
        child.setTranslateX(getTranslateX());
        child.setTranslateY(getTranslateY());
        child.setRotate(-angle);
        child.setScaleX(scx);
        child.setScaleY(scy);
        context.getSelection().setSelectedNode(child);
        context.redraw();
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(getTranslateX()-radius,getTranslateY()-radius,radius*2,radius*2);
    }

    @Override
    public Bounds getTransformedBounds() {
        return getBounds();
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

        //draw the child
        g.translate(child.getAnchorX(),child.getAnchorY());
        g.scale(scx, scy);
        g.rotate(-a, Transform.Z_AXIS);
        g.translate(-child.getAnchorX(),-child.getAnchorY());
        ((SelfDrawable)child).draw(g);
        g.translate(child.getAnchorX(),child.getAnchorY());
        g.rotate(a, Transform.Z_AXIS);
        g.scale(1.0 / scx, 1.0 / scy);
        g.translate(-child.getAnchorX(), -child.getAnchorY());

        //draw the overlay
        g.translate(child.getAnchorX(),child.getAnchorY());
        g.setPaint(FlatColor.BLACK);
        radius = 50.0*scy;
        g.drawOval(0-radius, 0-radius, radius*2, radius*2);
        radius++;
        g.setPaint(FlatColor.WHITE);
        g.drawOval(0-radius, 0-radius, radius*2, radius*2);
        radius++;
        Point2D pt2 = GeomUtil.calcPoint(new Point(0, 0), angle+90, radius+20);
        g.setPaint(FlatColor.hsb(0,0,0.8));
        g.drawLine(0,0,pt2.getX(),pt2.getY());
        g.drawLine(0,0,0,+radius);
        g.setPaint(FlatColor.hsb(0,0,0.3));
        g.translate(1,1);
        g.drawLine(0,0,pt2.getX(),pt2.getY());
        g.drawLine(0,0,0,+radius);
        g.translate(-1,-1);
        g.translate(-child.getAnchorX(), -child.getAnchorY());
    }

    public double getAngle() {
        return angle;
    }

    public static class TransformScaleHandle extends Handle {
        private STransformNode trans;
        DecimalFormat fmt = new DecimalFormat();

        public TransformScaleHandle(STransformNode trans) {
            this.trans = trans;
            fmt.setMinimumFractionDigits(1);
            fmt.setMaximumFractionDigits(1);
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
            ty = ty/50.0;
            if(constrain) {
                if(ty > 0.9 && ty < 1.1) ty = 1.0;
                if(ty > 1.4 && ty < 1.6) ty = 1.5;
                if(ty > 1.9 && ty < 2.1) ty = 2.0;
                if(ty > 2.4 && ty < 2.6) ty = 2.5;
                if(ty > 2.9 && ty < 3.1) ty = 3.0;
            }
            trans.scy = ty;
            trans.scx = trans.scy;
        }

        @Override
        public void draw(GFX g, SketchCanvas sketchCanvas) {
            Point2D pt = sketchCanvas.transformToDrawing(getX(),getY());


            double size = 15;
            g.translate(pt.getX(),pt.getY());
            g.rotate(90,Transform.Z_AXIS);
            g.setPaint(FlatColor.GREEN);
            g.fillPolygon(new double[]{0,-size/3, size,0, 0,size/3, -size,0});
            g.setPaint(FlatColor.hsb(0,0,0.3));
            g.drawPolygon(new double[]{0,-size/3, size,0, 0,size/3, -size,0});
            g.rotate(-90,Transform.Z_AXIS);
            g.translate(-pt.getX(),-pt.getY());
        }

        @Override
        public boolean processKey(KeyEvent event, boolean hovered) {
            int amount = 1;
            if(event.isShiftPressed()) {
                amount = 10;
            }
            if(event.getKeyCode() == KeyEvent.KeyCode.KEY_UP_ARROW) {
                setY(getY()-amount,false);
                return true;
            }
            if(event.getKeyCode() == KeyEvent.KeyCode.KEY_DOWN_ARROW) {
                setY(getY()+amount,false);
                return true;
            }
            if(event.getKeyCode() == KeyEvent.KeyCode.KEY_ENTER) {
                trans.finishTransform();
                return true;
            }
            return false;
        }

        @Override
        public String[] customStatusLines() {
            return new String[]{""+fmt.format(trans.scy*100) + '%' };
        }
    }


    public static class TransformRotateHandle extends Handle {
        private STransformNode trans;
        double x = 100;
        double y = 50;
        DecimalFormat fmt = new DecimalFormat();

        public TransformRotateHandle(STransformNode trans) {
            this.trans = trans;
            fmt.setMaximumFractionDigits(2);
            fmt.setMinimumFractionDigits(2);
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
            u.p("angle = " + a);
            if(constrain) {
                a = GeomUtil.snapTo45(a);
            } else {
                //a = Math.toDegrees(a);
            }
            trans.setAngle(a-90);
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
            Point2D pt = sketchCanvas.transformToDrawing(getX(),getY());

            double size = 15;
            g.translate(pt.getX(),pt.getY());
            g.rotate(90-trans.getAngle(),Transform.Z_AXIS);
            g.setPaint(FlatColor.GREEN);
            g.fillPolygon(new double[]{0,-size/3, size,0, 0,size/3, -size,0});
            g.setPaint(FlatColor.hsb(0,0,0.3));
            g.drawPolygon(new double[]{0,-size/3, size,0, 0,size/3, -size,0});
            g.rotate(-(90-trans.getAngle()),Transform.Z_AXIS);
            g.translate(-pt.getX(),-pt.getY());
        }

        @Override
        public boolean processKey(KeyEvent event, boolean hovered) {
            int amount = 1;
            if(event.isShiftPressed()) {
                amount = 10;
            }
            if(event.getKeyCode() == KeyEvent.KeyCode.KEY_LEFT_ARROW) {
                trans.setAngle(trans.getAngle()+amount);
                return true;
            }
            if(event.getKeyCode() == KeyEvent.KeyCode.KEY_RIGHT_ARROW) {
                trans.setAngle(trans.getAngle()-amount);
                return true;
            }
            return false;
        }

        @Override
        public String[] customStatusLines() {
            return new String[]{""+fmt.format(trans.getAngle()) + '\u00b0' };
        }
    }

}
