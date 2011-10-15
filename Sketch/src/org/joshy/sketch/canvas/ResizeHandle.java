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
        Point2D pt = modelToScreen(rect.getX(),rect.getY());
        switch (position) {
            case TopLeft:
            case TopRight: return pt.getY();
            case BottomLeft:
            case BottomRight: return pt.getY() + rect.getHeight();
            default: return 0;
        }
    }
    @Override
    public void setY(double y, boolean constrain) {
        //flip the tense if constrain by default
        if(rect.constrainByDefault()) {
            constrain = !constrain;
        }
        switch (position) {
            case TopLeft:
            case TopRight:
                Point2D pttop = screenToModel(getX(),y);
                if(constrain) {
                    double nh = rect.getY()+rect.getHeight()-pttop.getY();
                    double ratio = rect.getPreferredAspectRatio();
                    rect.setHeight(rect.getWidth()*ratio);
                    rect.setY(pttop.getY()+nh-(rect.getWidth()*ratio));
                } else {
                    rect.setHeight(rect.getY()+rect.getHeight() - pttop.getY());
                    rect.setY(pttop.getY());
                }
                break;
            case BottomLeft:
            case BottomRight:
                Point2D ptbottom = screenToModel(getX(),y);
                if(constrain) {
                    double ratio = rect.getPreferredAspectRatio();
                    rect.setHeight(rect.getWidth()*ratio);
                } else {
                    rect.setHeight(ptbottom.getY() - rect.getY());
                }
                break;
        }
        resetAnchor();
    }

    @Override
    public double getX() {
        Point2D pt = modelToScreen(rect.getX(),rect.getY());
        switch (position) {
            case TopLeft:
            case BottomLeft:
                return pt.getX();
            case TopRight:
            case BottomRight:
                return pt.getX()+rect.getWidth();
            default: return 0;
        }
    }
    @Override
    public void setX(double x, boolean constrain) {
        switch (position) {
            case TopLeft:
            case BottomLeft:
                Point2D ptleft = screenToModel(x,getY());
                rect.setWidth(rect.getX() + rect.getWidth() - ptleft.getX());
                rect.setX(ptleft.getX());
                break;
            case TopRight:
            case BottomRight:
                Point2D ptright = screenToModel(x, getY());
                rect.setWidth(ptright.getX()-rect.getX());
                break;
        }
        resetAnchor();
    }

    private Point2D screenToModel(double x, double y) {
        double tx = x - rect.getTranslateX();
        double ty = y - rect.getTranslateY();
        return new Point2D.Double(tx,ty);
    }
    private Point2D modelToScreen(double x, double y) {
        double tx = x + rect.getTranslateX();
        double ty = y + rect.getTranslateY();
        return new Point2D.Double(tx,ty);
    }

    private void resetAnchor() {
        rect.setAnchorX(rect.getX()+rect.getWidth()/2);
        rect.setAnchorY(rect.getY() + rect.getHeight() / 2);
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
