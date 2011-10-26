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
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class RadialGradientRadiusHandle extends Handle {
    private SShape shape;
    private VectorDocContext context;
    private RadialGradientCenterHandle master;

    public RadialGradientRadiusHandle(RadialGradientCenterHandle master, SShape shape, VectorDocContext context) {
        super();
        this.master = master;
        this.shape = shape;
        this.context = context;
    }

    private RadialGradientFill getFill() {
        return (RadialGradientFill) this.shape.getFillPaint();
    }

    @Override
    public double getX() {
        return shape.getBounds().getX() + getFill().getCenterX();
    }

    @Override
    public void setX(double x, boolean constrain) {
        //noop
    }

    @Override
    public double getY() {
        return shape.getBounds().getY()+getFill().getCenterY()+getFill().getRadius();
    }

    @Override
    public void setY(double y, boolean constrain) {
        y -= shape.getBounds().getY();
        getFill().setRadius(y-getFill().getCenterY());
        shape.setFillPaint(getFill());
        master.updateControlPositions();
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
}
