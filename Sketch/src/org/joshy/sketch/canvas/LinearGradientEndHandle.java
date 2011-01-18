package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.LinearGradientFill;
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
        return getFill().getEndX() + shape.getBounds().getX();
    }

    @Override
    public void setX(double x, boolean constrain) {
        x -= shape.getBounds().getX();
        getFill().setEndX(x);
        master.refresh();
        master.updateControlPositions();
    }

    @Override
    public double getY() {
        return getFill().getEndY() + shape.getBounds().getY();
    }

    @Override
    public void setY(double y, boolean constrain) {
        y -= shape.getBounds().getY();
        getFill().setEndY(y);
        master.refresh();
        master.updateControlPositions();
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
