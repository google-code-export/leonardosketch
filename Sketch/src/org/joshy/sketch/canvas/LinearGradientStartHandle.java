package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.LinearGradientFill;
import org.joshy.gfx.draw.MultiGradientFill;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.Container;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.gfx.util.u;
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
        return shape.getBounds().getX() + getFill().getStartX();
    }

    @Override
    public void setX(double x, boolean constrain) {
        x -= shape.getBounds().getX();
        getFill().setStartX(x);
        refresh();
        updateControlPositions();
    }

    @Override
    public double getY() {
        return shape.getBounds().getY()+getFill().getStartY();
    }

    @Override
    public void setY(double y, boolean constrain) {
        y-= shape.getBounds().getY();
        getFill().setStartY(y);
        refresh();
        updateControlPositions();
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
                getFill().getEndX() + shape.getBounds().getX(),
                getFill().getEndY()+shape.getBounds().getY());
        pt = context.getSketchCanvas().transformToDrawing(pt);
        return pt;
    }

    @Override
    protected Point2D getDragHandlePosition(MultiGradientFill.Stop stop, SketchCanvas canvas) {
        Point2D start = new Point2D.Double(
                getFill().getStartX()+shape.getBounds().getX()
                ,getFill().getStartY()+shape.getBounds().getY());
        Point2D end = new Point2D.Double(
                getFill().getEndX() + shape.getBounds().getX(),
                getFill().getEndY() + shape.getBounds().getY()
        );
        start = context.getSketchCanvas().transformToDrawing(start);
        end = context.getSketchCanvas().transformToDrawing(end);
        Point2D pt = Util.interpolatePoint(start, end, stop.getPosition());
        return pt;
    }

    @Override
    protected Point2D getStart() {
        return new Point2D.Double(
            getFill().getStartX() + shape.getBounds().getX(),
            getFill().getStartY() + shape.getBounds().getY()
        );
    }

    @Override
    protected Point2D getEnd() {
        return new Point2D.Double(
            getFill().getEndX() + shape.getBounds().getX(),
            getFill().getEndY() + shape.getBounds().getY()
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

        for(MultiGradientFill.Stop stop: getFill().getStops()) {
            Point2D pt = Util.interpolatePoint(start,end,stop.getPosition());
            if(pt.distance(cursor) < 5) {
                u.p("on the stop. ready for dragging");
                onStop = true;
                activeStop = stop;
            }
        }

        if(!onStop) {
            double dx = fractionOf(start,end,cursor);
            MultiGradientFill.Stop stop = new MultiGradientFill.Stop(dx, FlatColor.GREEN);
            getFill().addStop(stop);
            addStopControl(stop);
            context.getSelection().regenHandleControls(shape);
            updateControlPositions();
        }

    }

    private double fractionOf(Point2D start, Point2D end, Point2D.Double cursor) {
        double dx = (cursor.getX()-start.getX())/(end.getX()-start.getX());
        return dx;
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
            double pos = fractionOf(getStart(),getEnd(),cursor);
            //u.p("pos = " + pos);
            //ny -= shape.getBounds().getY();
            //ny -= getFill().getCenterY();

            //double pos = ny/getFill().getRadius();
            pos = Util.clamp(0.001, pos, 0.999);
            activeStop.setPosition(pos);
            updateControlPositions();
            refresh();
        }
    }

    @Override
    void updateControlPositions() {
        Container popupLayer = context.getSketchCanvas().getParent().getStage().getPopupLayer();
        Point2D start = new Point2D.Double(
                getFill().getStartX()+shape.getBounds().getX()
                ,getFill().getStartY()+shape.getBounds().getY());
        Point2D end = new Point2D.Double(
                getFill().getEndX() + shape.getBounds().getX(),
                getFill().getEndY() + shape.getBounds().getY()
        );
        start = context.getSketchCanvas().transformToDrawing(start);
        end = context.getSketchCanvas().transformToDrawing(end);
        double angle = GeomUtil.calcAngle(start,end);

        for(MultiGradientFill.Stop stop : getFill().getStops()){
            Point2D pt = Util.interpolatePoint(start,end,stop.getPosition());
            pt = NodeUtils.convertToScene(context.getSketchCanvas(), pt);
            pt = NodeUtils.convertFromScene(popupLayer, pt);
            pt = GeomUtil.calcPoint(pt,Math.toDegrees(angle)+90,15);
            Control control = controlMap.get(stop);
            control.setTranslateX((int)(pt.getX()-5));
            control.setTranslateY((int)(pt.getY()-5));

        }
    }

}
