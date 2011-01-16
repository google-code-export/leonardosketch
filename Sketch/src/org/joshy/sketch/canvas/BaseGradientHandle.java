package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.SwatchColorPicker;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.sketch.model.AbstractResizeableNode;
import org.joshy.sketch.model.Handle;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.tools.MouseEventHandle;
import org.joshy.sketch.util.DrawUtils;
import org.joshy.sketch.util.Util;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/15/11
 * Time: 4:19 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseGradientHandle<G extends MultiGradientFill>
        extends Handle
        implements MouseEventHandle, AbstractResizeableNode.SRectUpdateListener {
    protected SShape shape;
    protected VectorDocContext context;
    protected Point2D.Double hoverPoint;
    protected boolean hovered;
    protected RadialGradientFill.Stop activeStop;
    protected boolean onStop;
    protected boolean onPoint;
    protected boolean draggingStop;
    protected Point2D.Double startPoint;
    protected Map<RadialGradientFill.Stop,Control> controlMap;
    protected ArrayList<Control> controls;
    protected boolean showAddIndicator;

    public BaseGradientHandle(SShape shape, VectorDocContext context) {
        this.shape = shape;
        this.context = context;
        if(shape instanceof AbstractResizeableNode) {
            ((AbstractResizeableNode)this.shape).addListener(this);
        }
        controls = new ArrayList<Control>();
        controlMap = new HashMap<RadialGradientFill.Stop,Control>();
        for(final MultiGradientFill.Stop stop : getFill().getStops()) {
            addStopControl(stop);
        }
        updateControlPositions();
    }

    protected G getFill() {
        return (G) shape.getFillPaint();
    }

    protected void addStopControl(final RadialGradientFill.Stop stop) {
        SwatchColorPicker colorPopup = new CustomSwatchColorPicker();
        colorPopup.setPrefWidth(10);
        colorPopup.setPrefHeight(10);
        colorPopup.setSelectedColor(stop.getColor());
        controls.add(colorPopup);
        controlMap.put(stop,colorPopup);
        colorPopup.onColorSelected(new Callback<ChangedEvent>() {
            public void call(ChangedEvent changedEvent) throws Exception {
                FlatColor val = (FlatColor) changedEvent.getValue();
                if(val == null) {
                    val = FlatColor.WHITE_TRANSPARENT;
                }
                stop.setColor(val);
                refresh();
            }
        });
    }

    protected void refresh() {
        shape.setFillPaint(shape.getFillPaint());
    }

    abstract void updateControlPositions();

    @Override
    public boolean hasControls() {
        return true;
    }

    @Override
    public Iterable<? extends Control> getControls() {
        return controls;
    }

    @Override
    public void detach() {
        if(shape instanceof AbstractResizeableNode) {
            ((AbstractResizeableNode)this.shape).removeListener(this);
        }
    }

    public void mouseMoved(boolean hovered, MouseEvent event, Point2D.Double cursor) {
        this.hovered = hovered;
        hoverPoint = cursor;
        showAddIndicator = true;
        for(MultiGradientFill.Stop stop: getFill().getStops()) {
            Point2D pt = Util.interpolatePoint(getStart(), getEnd(), stop.getPosition());
            if(pt.distance(cursor) < 5) {
                showAddIndicator = false;
            }
        }
    }


    public boolean contains(double x, double y, double scale) {
        double size = this.size/scale;
        if(getEnd().distance(x,y) < 5) {
            return false;
        }
        Line2D.Double line = new Line2D.Double(getStart(),getEnd());
        if(line.ptSegDist(x,y) < size) {
            return true;
        } else {
            return false;
        }
    }

    public void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        if(onStop && !draggingStop) {
            //u.p("did a click");
        } else {
            //u.p("did a drag");
        }
        activeStop = null;
        onStop = false;
        onPoint = false;
        draggingStop = false;
        startPoint = null;
    }

    public void updated() {
        refresh();
        updateControlPositions();
    }


    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        if(!(shape.getFillPaint() instanceof MultiGradientFill)) return;

        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);

        double x = pt.getX();
        double y = pt.getY();

        drawConnectingLine(g, canvas);
        /*
        if(hovered) {
            if(hoverPoint.getY()-shape.getBounds().getY()-getY() < 5) {
                DrawUtils.drawStandardHandle(g, x, y, FlatColor.GREEN);
            } else {
                DrawUtils.drawStandardHandle(g, x, y, FlatColor.RED);
            }
        } else {
            DrawUtils.drawStandardHandle(g, x, y, FlatColor.GREEN);
        }
          */
        DrawUtils.drawStandardHandle(g, x, y, FlatColor.GREEN);

        drawStopHandles(g,canvas);
        drawNewStopIndicator(g, canvas);
    }

    protected void drawNewStopIndicator(GFX g, SketchCanvas canvas) {
        if(showAddIndicator && hovered) {
            Point2D hp = canvas.transformToDrawing(hoverPoint.getX(),hoverPoint.getY());
            g.translate(hp.getX(),hp.getY());

            double angle = GeomUtil.calcAngle(getStart(),getEnd());
            g.rotate(-Math.toDegrees(angle), Transform.Z_AXIS);
            Path2D.Double path = createHandlePath();
            g.setPaint(FlatColor.WHITE);
            g.fillPath(path);
            g.setPaint(FlatColor.BLACK);
            g.drawPath(path);
            g.rotate(Math.toDegrees(angle), Transform.Z_AXIS);
            g.translate(-hp.getX(),-hp.getY());
        }
    }

    protected void drawStopHandles(GFX g, SketchCanvas canvas) {
        g.setPureStrokes(true);
        //draw the handles
        for(RadialGradientFill.Stop stop: getFill().getStops()) {
            if(getFill().isFirst(stop)) continue;
            if(getFill().isLast(stop)) continue;
            Point2D pt = getDragHandlePosition(stop,canvas);
            g.translate(pt.getX(),pt.getY());

            double angle = GeomUtil.calcAngle(getStart(),getEnd());
            g.rotate(-Math.toDegrees(angle), Transform.Z_AXIS);
            Path2D.Double path = createHandlePath();
            g.setPaint(stop.getColor());
            g.fillPath(path);
            g.setPaint(FlatColor.BLACK);
            g.drawPath(path);
            g.rotate(Math.toDegrees(angle), Transform.Z_AXIS);
            g.translate(-pt.getX(),-pt.getY());
        }
        g.setPureStrokes(false);
    }

    private Path2D.Double createHandlePath() {
        Path2D.Double path = new Path2D.Double();
        double s = 9;
        path.moveTo(0,-s/2);
        path.lineTo(s, 0);
        path.lineTo(0,s/2);
        path.lineTo(-s,0);
        path.closePath();
        return path;
    }

    protected abstract Point2D getDragHandlePosition(MultiGradientFill.Stop stop, SketchCanvas canvas);

    protected void drawConnectingLine(GFX g, SketchCanvas canvas) {
        Point2D s = getConnectingLineStart();
        Point2D e = getConnectingLineEnd();
        //draw the gradient line
        g.setPaint(FlatColor.BLACK);
        g.drawLine(s.getX(), s.getY(), e.getX(), e.getY());
        g.setPaint(FlatColor.WHITE);
        g.drawLine(s.getX()+1, s.getY()+1, e.getX()+1,e.getY()+1);
    }

    abstract Point2D getConnectingLineStart();

    abstract Point2D getConnectingLineEnd();

    protected abstract Point2D getStart();

    protected abstract Point2D getEnd();

    private class CustomSwatchColorPicker extends SwatchColorPicker {
        @Override
        public void draw(GFX g) {
            if(!isVisible())return;
            g.setPureStrokes(true);
            g.setPaint(FlatColor.BLACK);
            g.fillOval(0, 0, getWidth(), getHeight());
            g.setPaint(FlatColor.WHITE);
            g.fillOval(0 + 1, 0 + 1, getWidth() - 2, getHeight() - 2);
            g.setPaint(getSelectedColor());
            g.fillOval(0 + 2, 0 + 2, getWidth() - 4, getHeight() - 4);
            g.setPureStrokes(false);
        }
    }
}