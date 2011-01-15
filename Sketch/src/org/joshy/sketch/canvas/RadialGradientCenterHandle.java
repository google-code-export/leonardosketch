package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.RadialGradientFill;
import org.joshy.sketch.model.Handle;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/15/11
 * Time: 1:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class RadialGradientCenterHandle extends Handle {
    private SShape shape;
    private VectorDocContext context;

    public RadialGradientCenterHandle(SShape shape, VectorDocContext context) {
        super();
        this.shape = shape;
        this.context = context;
    }

    @Override
    public double getX() {
        return shape.getBounds().getX() + getFill().getCenterX();
    }

    @Override
    public void setX(double x, boolean constrain) {
        x -= shape.getBounds().getX();
        getFill().setCenterX(x);
        shape.setFillPaint(getFill());
    }

    @Override
    public double getY() {
        return shape.getBounds().getY()+getFill().getCenterY();
    }

    @Override
    public void setY(double y, boolean constrain) {
        y-= shape.getBounds().getY();
        getFill().setCenterY(y);
        shape.setFillPaint(getFill());
    }

    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        if(!(shape.getFillPaint() instanceof RadialGradientFill)) return;

        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);

        double x = pt.getX();
        double y = pt.getY();

        RadialGradientFill fill = getFill();
        DrawUtils.drawStandardHandle(g, x, y, FlatColor.PURPLE);
    }

    public RadialGradientFill getFill() {
        return (RadialGradientFill) this.shape.getFillPaint();
    }
}
