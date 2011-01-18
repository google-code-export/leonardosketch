package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.PatternPaint;
import org.joshy.sketch.model.Handle;
import org.joshy.sketch.model.PatternMoveHandle;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/15/11
 * Time: 7:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatternResizeHandle extends Handle {
    private PatternMoveHandle master;
    private SShape shape;
    private VectorDocContext context;

    public PatternResizeHandle(PatternMoveHandle h1, SShape shape, VectorDocContext context) {
        super();
        this.master = h1;
        this.shape = shape;
        this.context = context;
    }

    private PatternPaint getFill() {
        return (PatternPaint) this.shape.getFillPaint();
    }

    @Override
    public double getX() {
        double v = getFill().getStart().getX()
                + getFill().getEnd().getX()/10
                + shape.getBounds().getX();
        return v;
    }

    @Override
    public void setX(double x, boolean constrain) {
        x -= shape.getBounds().getX();
        x -= getFill().getStart().getX();
        PatternPaint pat = getFill().deriveNewEnd(new Point2D.Double(x*10, getFill().getEnd().getY()));
        shape.setFillPaint(pat);
    }

    @Override
    public double getY() {
        double v = getFill().getStart().getY()
                +  getFill().getEnd().getY()/10
                + shape.getBounds().getY();
        return v;
    }

    @Override
    public void setY(double y, boolean constrain) {
        y -= shape.getBounds().getY();
        y -= getFill().getStart().getY();
        PatternPaint pat = getFill().deriveNewEnd(new Point2D.Double(getFill().getEnd().getX(), y*10));
        shape.setFillPaint(pat);
    }

    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);
        double x = pt.getX();
        double y = pt.getY();
        DrawUtils.drawStandardHandle(g, x, y, FlatColor.PURPLE);
    }
}
