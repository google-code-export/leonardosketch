package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.LinearGradientFill;
import org.joshy.gfx.draw.MultiGradientFill;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.Container;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.sketch.model.AbstractResizeableNode;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.util.Util;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/15/11
 * Time: 4:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinearGradientStartHandle extends BaseGradientHandle<LinearGradientFill> {

    public LinearGradientStartHandle(SShape shape, VectorDocContext context) {
        super(shape,context);
    }


    @Override
    public double getX() {
        return shape.getTransformedBounds().getX() + getFill().getStartX();
    }


    @Override
    public void setX(double x, boolean constrain) {
        x -= shape.getTransformedBounds().getX();
        if(shape instanceof AbstractResizeableNode) {
            snapX((AbstractResizeableNode) shape, x);
        } else {
            getFill().setStartX(x);
        }
        refresh();
        updateControlPositions();
    }

    private void snapX(AbstractResizeableNode node, double x) {
        LinearGradientFill f = getFill();
        Bounds b = node.getTransformedBounds();
        if(Math.abs(x-0)<5) {
            f.setStartX(0);
            f.setStartXSnapped(LinearGradientFill.Snap.Start);
            return;
        }
        if(Math.abs(x-b.getWidth()/2)<5) {
            f.setStartX(b.getWidth() / 2);
            f.setStartXSnapped(LinearGradientFill.Snap.Middle);
            return;
        }
        if(Math.abs(x-b.getWidth())<5) {
            f.setStartX(b.getWidth());
            f.setStartXSnapped(LinearGradientFill.Snap.End);
            return;
        }
        f.setStartX(x);
        f.setStartXSnapped(LinearGradientFill.Snap.None);
    }


    @Override
    public double getY() {
        return shape.getTransformedBounds().getY()+getFill().getStartY();
    }

    @Override
    public void setY(double y, boolean constrain) {
        y-= shape.getTransformedBounds().getY();
        if(shape instanceof AbstractResizeableNode) {
            snapY((AbstractResizeableNode)shape,y);
        } else {
            getFill().setStartY(y);
        }
        refresh();
        updateControlPositions();
    }
    private void snapY(AbstractResizeableNode node, double y) {
        LinearGradientFill f = getFill();
        Bounds b = node.getTransformedBounds();
        if(Math.abs(y-0)<5) {
            f.setStartY(0);
            f.setStartYSnapped(LinearGradientFill.Snap.Start);
            return;
        }
        if(Math.abs(y-b.getHeight()/2)<5) {
            f.setStartY(b.getHeight() / 2);
            f.setStartYSnapped(LinearGradientFill.Snap.Middle);
            return;
        }
        if(Math.abs(y-b.getHeight())<5) {
            f.setStartY(b.getHeight());
            f.setStartYSnapped(LinearGradientFill.Snap.End);
            return;
        }
        f.setStartY(y);
        f.setStartYSnapped(LinearGradientFill.Snap.None);
    }

    @Override
    Point2D getConnectingLineStart() {
        Point2D pt = new Point2D.Double(getX(),getY());
        pt = context.getSketchCanvas().transformToDrawing(pt);
        return pt;
    }

    @Override
    Point2D getConnectingLineEnd() {
        Point2D pt = new Point2D.Double(
                getFill().getEndX() + shape.getTransformedBounds().getX(),
                getFill().getEndY()+shape.getTransformedBounds().getY());
        pt = context.getSketchCanvas().transformToDrawing(pt);
        return pt;
    }

    @Override
    protected Point2D getDragHandlePosition(MultiGradientFill.Stop stop, SketchCanvas canvas) {
        Point2D start = new Point2D.Double(
                getFill().getStartX()+shape.getTransformedBounds().getX()
                ,getFill().getStartY()+shape.getTransformedBounds().getY());
        Point2D end = new Point2D.Double(
                getFill().getEndX() + shape.getTransformedBounds().getX(),
                getFill().getEndY() + shape.getTransformedBounds().getY()
        );
        start = context.getSketchCanvas().transformToDrawing(start);
        end = context.getSketchCanvas().transformToDrawing(end);
        Point2D pt = Util.interpolatePoint(start, end, stop.getPosition());
        return pt;
    }

    @Override
    protected Point2D getStart() {
        return new Point2D.Double(
            getFill().getStartX() + shape.getTransformedBounds().getX(),
            getFill().getStartY() + shape.getTransformedBounds().getY()
        );
    }

    @Override
    protected Point2D getEnd() {
        return new Point2D.Double(
            getFill().getEndX() + shape.getTransformedBounds().getX(),
            getFill().getEndY() + shape.getTransformedBounds().getY()
        );
    }



    public void mousePressed(MouseEvent event, Point2D.Double cursor) {
        showAddIndicator = false;
        startPoint = cursor;
        if(getStart().distance(cursor) < 5) {
            onPoint = true;
            return;
        }


        Point2D start = getStart();
        Point2D end = getEnd();

        //check if starting to drag a stop
        for(MultiGradientFill.Stop stop: getFill().getStops()) {
            Point2D pt = Util.interpolatePoint(start,end,stop.getPosition());
            if(pt.distance(cursor) < 5) {
                onStop = true;
                activeStop = stop;
                return;
            }
        }

        //if no stop, then user is creating a new stop
        if(!onStop) {
            //calculation fraction position
            double dx = fractionOf(start,end,cursor);
            addStop(dx);
        }

    }

    private double fractionOf(Point2D start, Point2D end, Point2D.Double cursor) {
        double d1 = Math.abs(start.distance(end));
        double d2 = Math.abs(start.distance(cursor));
        return d2/d1;
    }

    public void mouseDragged(double nx, double ny, boolean shiftPressed, Point2D.Double cursor) {
        if(onPoint) {
            setX(nx, shiftPressed);
            setY(ny, shiftPressed);
        }


        if(onStop && !draggingStop) {
            if(cursor.distance(startPoint) > 5) {
                draggingStop = true;
            }
        }
        if(onStop) {
            Point2D stopPoint = Util.interpolatePoint(getStart(),getEnd(),activeStop.getPosition());
            double d = stopPoint.distance(cursor);
            if(d > 20) {
                couldDelete = true;
            } else {
                couldDelete = false;
            }
            double pos = fractionOf(getStart(),getEnd(),cursor);
            pos = Util.clamp(0.001, pos, 0.999);
            activeStop.setPosition(pos);
            updateControlPositions();
            refresh();
        }
    }

    @Override
    public void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        if(couldDelete) {
            getFill().removeStop(activeStop);
            removeStopControl(activeStop);
            context.getSelection().regenHandleControls(shape);
            updateControlPositions();
        }
        couldDelete = false;
        super.mouseReleased(event, cursor);
    }


    @Override
    void updateControlPositions() {
        Container popupLayer = context.getSketchCanvas().getParent().getStage().getPopupLayer();
        Point2D start = new Point2D.Double(
                getFill().getStartX()+shape.getTransformedBounds().getX()
                ,getFill().getStartY()+shape.getTransformedBounds().getY());
        Point2D end = new Point2D.Double(
                getFill().getEndX() + shape.getTransformedBounds().getX(),
                getFill().getEndY() + shape.getTransformedBounds().getY()
        );
        start = context.getSketchCanvas().transformToDrawing(start);
        end = context.getSketchCanvas().transformToDrawing(end);
        double angle = GeomUtil.calcAngle(start,end);

        for(MultiGradientFill.Stop stop : getFill().getStops()){
            Point2D pt = Util.interpolatePoint(start,end,stop.getPosition());
            pt = NodeUtils.convertToScene(context.getSketchCanvas(), pt);
            pt = NodeUtils.convertFromScene(popupLayer, pt);
            pt = GeomUtil.calcPoint(pt,Math.toDegrees(angle)+90,15);
            Control colorControl = colorControlMap.get(stop);
            colorControl.setTranslateX((int) (pt.getX() - 5));
            colorControl.setTranslateY((int) (pt.getY() - 5));

            pt = GeomUtil.calcPoint(pt,Math.toDegrees(angle)+90,10);
            Control alphaControl = alphaControlMap.get(stop);
            alphaControl.setTranslateX((int) (pt.getX() - 5));
            alphaControl.setTranslateY((int) (pt.getY() - 5));

        }
    }

    @Override
    public void changed() {
        if(shape instanceof AbstractResizeableNode) {
            AbstractResizeableNode res = (AbstractResizeableNode) shape;
            Paint paint = shape.getFillPaint();
            if(paint instanceof LinearGradientFill) {
                LinearGradientFill lg = (LinearGradientFill) paint;
                switch(lg.getStartXSnapped()) {
                    case Start: lg.setStartX(0); break;
                    case Middle: lg.setStartX(res.getBounds().getWidth() / 2); break;
                    case End: lg.setStartX(res.getBounds().getWidth()); break;
                }
                switch(lg.getStartYSnapped()) {
                    case Start: lg.setStartY(0); break;
                    case Middle: lg.setStartY(res.getBounds().getHeight() / 2); break;
                    case End: lg.setStartY(res.getBounds().getHeight()); break;
                }
                switch(lg.getEndXSnapped()) {
                    case Start: lg.setEndX(0); break;
                    case Middle: lg.setEndX(res.getBounds().getWidth()/2); break;
                    case End: lg.setEndX(res.getBounds().getWidth()); break;
                }
                switch(lg.getEndYSnapped()) {
                    case Start: lg.setEndY(0); break;
                    case Middle: lg.setEndY(res.getBounds().getHeight() / 2); break;
                    case End: lg.setEndY(res.getBounds().getHeight()); break;
                }
            }
        }
        super.changed();
    }
}
