package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.MultiGradientFill;
import org.joshy.gfx.draw.RadialGradientFill;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.Container;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.util.Util;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/15/11
 * Time: 1:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class RadialGradientCenterHandle extends BaseGradientHandle<RadialGradientFill> {

    public RadialGradientCenterHandle(SShape shape, VectorDocContext context) {
        super(shape, context);
    }

    @Override
    public double getX() {
        return shape.getBounds().getX() + getFill().getCenterX();
    }

    @Override
    public void setX(double x, boolean constrain) {
        x -= shape.getBounds().getX();
        getFill().setCenterX(x);
        refresh();
        updateControlPositions();
    }

    @Override
    public double getY() {
        return shape.getBounds().getY()+getFill().getCenterY();
    }

    @Override
    public void setY(double y, boolean constrain) {
        y-= shape.getBounds().getY();
        getFill().setCenterY(y);
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
        Point2D pt = new Point2D.Double(getX(),getY());
        pt = context.getSketchCanvas().transformToDrawing(pt);
        pt.setLocation(pt.getX(),pt.getY()+getFill().getRadius());
        return pt;
    }

    @Override
    protected Point2D getDragHandlePosition(MultiGradientFill.Stop stop, SketchCanvas canvas) {
        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);
        double x = pt.getX();
        double y = pt.getY();
        return new Point2D.Double(
                x+2,
                y+stop.getPosition() * getFill().getRadius()-0
        );
    }

    public RadialGradientFill getFill() {
        return (RadialGradientFill) this.shape.getFillPaint();
    }

    @Override
    protected Point2D getStart() {
        return new Point2D.Double(
            getFill().getCenterX() + shape.getBounds().getX(),
            getFill().getCenterY() + shape.getBounds().getY()
        );
    }

    @Override
    protected Point2D getEnd() {
        return new Point2D.Double(
            getFill().getCenterX() + shape.getBounds().getX(),
            getFill().getCenterY()+getFill().getRadius() + shape.getBounds().getY()
        );
    }

    public void mousePressed(MouseEvent event, Point2D.Double cursor) {
        showAddIndicator = false;
        double ny = cursor.getY();
        ny-=shape.getBounds().getY();
        double nx = cursor.getX();
        nx -= shape.getBounds().getX();
        if(ny - getFill().getCenterY() > 5) {
            ny -= getFill().getCenterY();
            for(RadialGradientFill.Stop stop: getFill().getStops()) {
                double y = stop.getPosition()*getFill().getRadius();
                if(ny > y-5 && ny < y+5) {
                    onStop = true;
                    activeStop = stop;
                }
            }
            if(!onStop) {
                RadialGradientFill.Stop stop = new RadialGradientFill.Stop(ny/getFill().getRadius(),FlatColor.GREEN);
                getFill().addStop(stop);
                addStopControl(stop);
                context.getSelection().regenHandleControls(shape);
                updateControlPositions();
            }
        } else {
            onPoint = true;
        }

        startPoint = cursor;
    }

    public void mouseDragged(double nx, double ny, boolean shiftPressed, Point2D.Double cursor) {
        if(onPoint) {
            setX(nx,shiftPressed);
            setY(ny, shiftPressed);
        }

        if(onStop && !draggingStop) {
            if(cursor.distance(startPoint) > 5) {
                draggingStop = true;
            }
        }
        if(onStop) {
            ny -= shape.getBounds().getY();
            ny -= getFill().getCenterY();

            double pos = ny/getFill().getRadius();
            pos = Util.clamp(0.001, pos, 0.999);
            activeStop.setPosition(pos);
            updateControlPositions();
            refresh();
        }
    }

    void updateControlPositions() {
        Container popupLayer = context.getSketchCanvas().getParent().getStage().getPopupLayer();
        for(MultiGradientFill.Stop stop : getFill().getStops()){
            Point2D pt = context.getSketchCanvas().transformToDrawing(
                    getX(),
                    getY() + getFill().getRadius()*stop.getPosition());
            pt = NodeUtils.convertToScene(context.getSketchCanvas(), pt);
            pt = NodeUtils.convertFromScene(popupLayer, pt);
            Control control = controlMap.get(stop);
            control.setTranslateX((int)(pt.getX()+11));
            control.setTranslateY((int)(pt.getY()-5));
        }
    }
}
