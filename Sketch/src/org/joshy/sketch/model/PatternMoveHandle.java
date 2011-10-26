package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.PatternPaint;
import org.joshy.sketch.canvas.SketchCanvas;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/9/11
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatternMoveHandle extends Handle {

    private VectorDocContext context;
    private SShape shape;


    public PatternMoveHandle(SShape shape, VectorDocContext context) {
        super();
        this.shape = shape;
        this.context = context;
    }

    double offset = 0;
    @Override
    public double getX() {
        return getFill().getStart().getX() + shape.getTranslateX() + offset;
    }

    @Override
    public void setX(double x, boolean constrain) {
        x -= shape.getTranslateX();
        x -= offset;
        double y = getFill().getStart().getY();
        shape.setFillPaint(getFill().deriveNewStart(new Point2D.Double(x,y)));
    }

    @Override
    public double getY() {
        return getFill().getStart().getY() + shape.getTranslateY() + offset;
    }

    @Override
    public void setY(double y, boolean constrain) {
        double x = getFill().getStart().getX();
        y -= shape.getTranslateY();
        y -= offset;
        shape.setFillPaint(getFill().deriveNewStart(new Point2D.Double(x,y)));
    }

    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);
        PatternPaint pat = getFill();

        double x = pt.getX();
        double y = pt.getY();
        DrawUtils.drawStandardHandle(g, x, y, FlatColor.PURPLE);
    }

    private PatternPaint getFill() {
        return (PatternPaint) this.shape.getFillPaint();
    }

}
