package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.LinearGradientFill;
import org.joshy.gfx.node.Bounds;
import org.joshy.sketch.model.AbstractResizeableNode;
import org.joshy.sketch.model.Handle;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/15/11
 * Time: 4:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinearGradientEndHandle extends Handle {
    private LinearGradientStartHandle master;
    private SShape shape;
    private VectorDocContext context;

    public LinearGradientEndHandle(LinearGradientStartHandle h1, SShape shape, VectorDocContext context) {
        super();
        this.master = h1;
        this.shape = shape;
        this.context = context;
    }

    @Override
    public double getX() {
        return getFill().getEndX() + shape.getTransformedBounds().getX();
    }

    @Override
    public void setX(double x, boolean constrain) {
        x -= shape.getTransformedBounds().getX();
        if(shape instanceof AbstractResizeableNode) {
            snapX((AbstractResizeableNode) shape, x);
        } else {
            getFill().setEndX(x);
        }
        master.refresh();
        master.updateControlPositions();
    }

    private void snapX(AbstractResizeableNode node, double x) {
        LinearGradientFill f = getFill();
        Bounds b = node.getTransformedBounds();
        if(Math.abs(x-0)<5) {
            f.setEndX(0);
            f.setEndXSnapped(LinearGradientFill.Snap.Start);
            return;
        }
        if(Math.abs(x-b.getWidth()/2)<5) {
            f.setEndX(b.getWidth() / 2);
            f.setEndXSnapped(LinearGradientFill.Snap.Middle);
            return;
        }
        if(Math.abs(x-b.getWidth())<5) {
            f.setEndX(b.getWidth());
            f.setEndXSnapped(LinearGradientFill.Snap.End);
            return;
        }
        f.setEndX(x);
        f.setEndXSnapped(LinearGradientFill.Snap.None);
    }

    @Override
    public double getY() {
        return getFill().getEndY() + shape.getTransformedBounds().getY();
    }

    @Override
    public void setY(double y, boolean constrain) {
        y -= shape.getTransformedBounds().getY();
        if(shape instanceof AbstractResizeableNode) {
            snapY((AbstractResizeableNode)shape,y);
        } else {
            getFill().setEndY(y);
        }
        master.refresh();
        master.updateControlPositions();
    }

    private void snapY(AbstractResizeableNode node, double y) {
        LinearGradientFill f = getFill();
        Bounds b = node.getTransformedBounds();
        if(Math.abs(y-0)<5) {
            f.setEndY(0);
            f.setEndYSnapped(LinearGradientFill.Snap.Start);
            return;
        }
        if(Math.abs(y-b.getHeight()/2)<5) {
            f.setEndY(b.getHeight()/2);
            f.setEndYSnapped(LinearGradientFill.Snap.Middle);
            return;
        }
        if(Math.abs(y-b.getHeight())<5) {
            f.setEndY(b.getHeight());
            f.setEndYSnapped(LinearGradientFill.Snap.End);
            return;
        }
        f.setEndY(y);
        f.setEndYSnapped(LinearGradientFill.Snap.None);
    }

    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        if(!(shape.getFillPaint() instanceof LinearGradientFill)) return;

        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);

        double x = pt.getX();
        double y = pt.getY();

        DrawUtils.drawStandardHandle(g, x, y, FlatColor.PURPLE);
    }

    public LinearGradientFill getFill() {
        return (LinearGradientFill) shape.getFillPaint();
    }
}
