package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.RadialGradientFill;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.SwatchColorPicker;
import org.joshy.gfx.node.layout.Container;
import org.joshy.sketch.model.AbstractResizeableNode;
import org.joshy.sketch.model.Handle;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.tools.MouseEventHandle;
import org.joshy.sketch.util.DrawUtils;
import org.joshy.sketch.util.Util;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/15/11
 * Time: 1:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class RadialGradientCenterHandle extends Handle implements MouseEventHandle, AbstractResizeableNode.SRectUpdateListener {
    private SShape shape;
    private VectorDocContext context;
    private Point2D.Double hoverPoint;
    private boolean hovered;
    private RadialGradientFill.Stop activeStop;
    private boolean onStop;
    private boolean onPoint;
    private boolean draggingStop;
    private Point2D.Double startPoint;
    private Map<RadialGradientFill.Stop,Control> controlMap;
    private ArrayList<Control> controls;
    private boolean showAddIndicator;

    public RadialGradientCenterHandle(SShape shape, VectorDocContext context) {
        super();
        this.shape = shape;
        if(shape instanceof AbstractResizeableNode) {
            ((AbstractResizeableNode)this.shape).addListener(this);
        }

        this.context = context;

        controls = new ArrayList<Control>();
        controlMap = new HashMap<RadialGradientFill.Stop,Control>();

        for(final RadialGradientFill.Stop stop : getFill().getStops()) {
            addStopControl(stop);
        }
        updateControls();
    }

    private void addStopControl(final RadialGradientFill.Stop stop) {
        SwatchColorPicker colorPopup = new SwatchColorPicker();
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

    private void refresh() {
        shape.setFillPaint(getFill());
    }

    void updateControls() {
        Container popupLayer = context.getSketchCanvas().getParent().getStage().getPopupLayer();
        for(RadialGradientFill.Stop stop : getFill().getStops()){
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

    @Override
    public boolean hasControls() {
        return true;
    }
    @Override
    public Iterable<? extends Control> getControls() {
        return controls;
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
        updateControls();
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
        updateControls();
    }

    //override contains so we can handle the Stops area as well
    @Override
    public boolean contains(double x, double y, double scale) {
        double size = this.size/scale;


        if(x >= getX()-size && x <= getX()+size+10) {
            //check the line
            if(y >= getY()+5 && y <= getY()+getFill().getRadius()-5) {
                //u.p("on the line!");
                return true;
            }

            //check the point
            if(y >= getY()-size && y <= getY()+size) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        if(!(shape.getFillPaint() instanceof RadialGradientFill)) return;

        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);

        double x = pt.getX();
        double y = pt.getY();

        RadialGradientFill fill = getFill();

        //draw the gradient line
        g.setPaint(FlatColor.BLACK);
        g.drawLine(x,y,x,y+fill.getRadius());
        g.setPaint(FlatColor.WHITE);
        g.drawLine(x+1,y,x+1,y+fill.getRadius());

        if(hovered) {
            if(hoverPoint.getY()-shape.getBounds().getY()-getY() < 5) {
                DrawUtils.drawStandardHandle(g, x, y, FlatColor.GREEN);
            } else {
                DrawUtils.drawStandardHandle(g, x, y, FlatColor.RED);
            }
        } else {
            DrawUtils.drawStandardHandle(g, x, y, FlatColor.GREEN);
        }


        //draw the handles
        for(RadialGradientFill.Stop stop: fill.getStops()) {
            if(fill.isFirst(stop)) continue;
            if(fill.isLast(stop)) continue;
            g.translate(x+2, y + stop.getPosition() * fill.getRadius() -0 );
            Path2D.Double path = DrawUtils.createLeftTriangle(9);
            g.setPaint(stop.getColor());
            g.fillPath(path);
            g.setPaint(FlatColor.BLACK);
            g.drawPath(path);
            g.translate(-x-2, -(y + stop.getPosition() * fill.getRadius()-0));
        }

        if(showAddIndicator && hovered) {
            Point2D hp = canvas.transformToDrawing(getX(),hoverPoint.getY());
            g.translate(hp.getX(),hp.getY());
            g.setPaint(FlatColor.BLACK);
            g.drawRect(-5, -5, 10, 10);
            g.setPaint(FlatColor.WHITE);
            g.drawRect(-4,-4,8,8);
            g.translate(-hp.getX(),-hp.getY());
        }
    }

    public RadialGradientFill getFill() {
        return (RadialGradientFill) this.shape.getFillPaint();
    }

    public void mouseMoved(boolean hovered, MouseEvent event, Point2D.Double cursor) {
        this.hovered = hovered;
        hoverPoint = cursor;
        RadialGradientFill.Stop stop = findStop(cursor);
        if(stop == null) {
            showAddIndicator = true;
        } else {
            showAddIndicator = false;
        }
    }

    private RadialGradientFill.Stop findStop(Point2D.Double cursor) {
        double ny = cursor.getY();
        ny -=shape.getBounds().getY();
        ny -= getFill().getCenterY();
        if(ny <= 5) {
            return null;
        }
        for(RadialGradientFill.Stop stop: getFill().getStops()) {
            double y = stop.getPosition()*getFill().getRadius();
            if(ny > y-5 && ny < y+5) {
                return stop;
            }
        }
        return null;
    }

    public void mousePressed(MouseEvent event, Point2D.Double cursor) {
        showAddIndicator = false;
        double ny = cursor.getY();
        ny-=shape.getBounds().getY();
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
                updateControls();
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
            updateControls();
            refresh();
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
        updateControls();
    }
}
