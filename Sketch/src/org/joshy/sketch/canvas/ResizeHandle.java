package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.sketch.model.SResizeableNode;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Apr 6, 2010
 * Time: 2:31:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResizeHandle extends PositionHandle {
    private SResizeableNode rect;

    public ResizeHandle(SResizeableNode rect, Position position) {
        super(position);
        this.rect = rect;
    }

    public SResizeableNode getResizeableNode() {
        return rect;
    }


    @Override
    public double getY() {
        double y = rect.getTranslateY() + rect.getY();
        switch (position) {
            case TopLeft:  return y;
            case TopRight: return y;
            case BottomLeft:  return y + rect.getHeight();
            case BottomRight: return y + rect.getHeight();
            default: return 0;
        }
    }
    @Override
    public void setY(double y, boolean constrain) {
        double dy = y - rect.getTranslateY() - rect.getY();
        //flip the tense if constrain by default
        if(rect.constrainByDefault()) {
            constrain = !constrain;
        }
        switch (position) {
            case TopLeft:
            case TopRight:
                if(constrain) {
                    double ny = y - rect.getTranslateY();
                    double nh = rect.getHeight()-dy;
                    double ratio = rect.getPreferredAspectRatio();
                    double diff = nh-(rect.getWidth()*ratio);
                    rect.setY(ny+diff);
                    rect.setHeight(nh-diff);
                } else {
                    rect.setY(y - rect.getTranslateY());
                    rect.setHeight(rect.getHeight() - dy);
                }
                break;
            case BottomLeft:
            case BottomRight:
                if(constrain) {
                    double ratio = rect.getPreferredAspectRatio();
                    rect.setHeight(rect.getWidth()*ratio);
                } else {
                    rect.setHeight(y - rect.getY() - rect.getTranslateY());
                }
                break;
        }
    }

    @Override
    public double getX() {
        double x = rect.getTranslateX() + rect.getX();
        switch (position) {
            case TopLeft:  return x;
            case BottomLeft:  return x;
            case TopRight: return x + rect.getWidth();
            case BottomRight: return x + rect.getWidth();
            default: return 0;
        }
    }
    @Override
    public void setX(double x, boolean constrain) {
        double dx = x - rect.getTranslateX()-rect.getX();
        switch (position) {
            case TopLeft:
                rect.setX(x-rect.getTranslateX());
                rect.setWidth(rect.getWidth() - dx);
                break;
            case BottomLeft:
                rect.setX(x-rect.getTranslateX());
                rect.setWidth(rect.getWidth() - dx);
                break;
            case TopRight: rect.setWidth(x - rect.getX() - rect.getTranslateX()); break;
            case BottomRight: rect.setWidth(x - rect.getX() - rect.getTranslateX()); break;
        }
    }

    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);

        double x = pt.getX();
        double y = pt.getY();
        double s = 0;
        DrawUtils.drawStandardHandle(g,x,y, FlatColor.BLUE);
    }

}
