package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.sketch.model.SResizeableNode;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
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
        switch (position) {
            case TopLeft: return modelToScreen(rect.getX(),rect.getY()).getY();
            case TopRight: return modelToScreen(rect.getX()+rect.getWidth(),rect.getY()).getY();
            case BottomLeft: return modelToScreen(rect.getX(),rect.getY()+ rect.getHeight()).getY();
            case BottomRight: return modelToScreen(rect.getX()+rect.getWidth(),rect.getY()+ rect.getHeight()).getY();
            default: return 0;
        }
    }
    @Override
    public void setY(double y, boolean constrain) {
        /*
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
        //resetAnchor();
        */
    }

    @Override
    public double getX() {
        switch (position) {
            case TopLeft:
                return modelToScreen(rect.getX(),rect.getY()).getX();
            case BottomLeft:
                return modelToScreen(rect.getX(),rect.getY()+rect.getHeight()).getX();
            case TopRight:
                return modelToScreen(rect.getX()+rect.getWidth(),rect.getY()).getX();
            case BottomRight:
                return modelToScreen(rect.getX()+rect.getWidth(),rect.getY()+rect.getHeight()).getX();
            default: return 0;
        }
    }
    @Override
    public void setX(double x, boolean constrain) {
        /*
        switch (position) {
            case TopLeft:
                Point2D left = screenToModel(x,getY());
                rect.setWidth(rect.getX() + rect.getWidth() - left.getX());
                rect.setX(left.getX());
                break;
            case BottomLeft:
                Point2D left2 = screenToModel(x,getY());
                rect.setWidth(rect.getX() + rect.getWidth() - left2.getX());
                rect.setX(left2.getX());
                break;
            case TopRight:
                Point2D ptright = screenToModel(x, getY());
                rect.setWidth(ptright.getX()-rect.getX());
                break;
            case BottomRight:
                Point2D ptright2 = screenToModel(x, getY());
                rect.setWidth(ptright2.getX()-rect.getX());
                break;
        }
        //resetAnchor();
        */
    }


    @Override
    public void setXY(double x, double y, boolean constrain) {
        switch (position) {
            case TopLeft:
                Point2D tl = screenToModel(x,y);
                rect.setWidth(rect.getX() + rect.getWidth() - tl.getX());
                rect.setX(tl.getX());
                rect.setHeight(rect.getY() + rect.getHeight() - tl.getY());
                rect.setY(tl.getY());
                break;
            case BottomLeft:
                Point2D bl = screenToModel(x,y);
                rect.setWidth(rect.getX() + rect.getWidth() - bl.getX());
                rect.setX(bl.getX());
                rect.setHeight(bl.getY() - rect.getY());
                break;
            case TopRight:
                Point2D tr = screenToModel(x, y);
                rect.setWidth(tr.getX()-rect.getX());
                rect.setHeight(rect.getY()+rect.getHeight() - tr.getY());
                rect.setY(tr.getY());
                break;
            case BottomRight:
                Point2D br = screenToModel(x, y);
                rect.setWidth(br.getX()-rect.getX());
                rect.setHeight(br.getY()-rect.getY());
                break;
        }
        resetAnchor();
    }

    private Point2D screenToModel(double x, double y) {
        AffineTransform af = new AffineTransform();
        af.translate(rect.getTranslateX(),rect.getTranslateY());
        af.translate(rect.getAnchorX(), rect.getAnchorY());
        af.rotate(Math.toRadians(rect.getRotate()));
        af.scale(rect.getScaleX(), rect.getScaleY());
        af.translate(-rect.getAnchorX(), -rect.getAnchorY());
        try {
            return af.inverseTransform(new Point2D.Double(x, y), null);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
            return new Point2D.Double(x,y);
        }
    }

    private Point2D modelToScreen(double x, double y) {
        AffineTransform af = new AffineTransform();
        af.translate(rect.getTranslateX(),rect.getTranslateY());
        af.translate(rect.getAnchorX(), rect.getAnchorY());
        af.rotate(Math.toRadians(rect.getRotate()));
        af.scale(rect.getScaleX(), rect.getScaleY());
        af.translate(-rect.getAnchorX(), -rect.getAnchorY());
        return af.transform(new Point2D.Double(x,y),null);
    }

    private void resetAnchor() {
        //rect.setAnchorX(rect.getX() + rect.getWidth() / 2);
        //rect.setAnchorY(rect.getY() + rect.getHeight() / 2);
    }

    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);

        double x = pt.getX();
        double y = pt.getY();
        //double s = 0;
        DrawUtils.drawStandardHandle(g,x,y, FlatColor.BLUE);
    }

}
