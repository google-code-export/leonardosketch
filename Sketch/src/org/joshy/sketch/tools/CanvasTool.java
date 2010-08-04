package org.joshy.sketch.tools;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Point2D;

/*
//new abstract tool class
//handles coordinate conversion between real mouse and canvas
//preregisters event listener and breaks it out into separate methods
//handles enabled boolean
//filters out global events like command+drag to pan the canvas, escape to go back

 */
public abstract class CanvasTool implements Callback<Event> {
    protected boolean enabled;
    private boolean doingPanZoom;
    private Point2D panZoomStart;
    protected VectorDocContext context;

    protected CanvasTool(VectorDocContext context) {
        this.context = context;
         EventBus.getSystem().addListener(context.getCanvas(), MouseEvent.MouseAll, this);
         EventBus.getSystem().addListener(context.getCanvas(), KeyEvent.KeyAll, this);
         EventBus.getSystem().addListener(context.getCanvas(), ScrollEvent.ScrollAll, this);
    }

    public abstract void drawOverlay(GFX g);

    public final void call(Event event) {
        if(!enabled) return;
        if(event instanceof KeyEvent) call((KeyEvent)event);
        if(event instanceof MouseEvent) call((MouseEvent)event);
        if(event instanceof ScrollEvent) call((ScrollEvent)event);
    }

    protected void call(ScrollEvent event) {
//        u.p("scrolled: " + event.getAmount() + " type = " + event.getType());
        double scale = -2.0;
        Point2D diff = new Point2D.Double(0,event.getAmount()*scale);
        if(event.getType() == ScrollEvent.ScrollHorizontal) {
            diff = new Point2D.Double(event.getAmount()*scale,0);
        }
        context.getSketchCanvas().setPanX(context.getSketchCanvas().getPanX()+diff.getX());
        context.getSketchCanvas().setPanY(context.getSketchCanvas().getPanY()+diff.getY());
    }

    protected abstract void call(KeyEvent event);
    
    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }


    private void call(MouseEvent event) {
        if(event.getSource() != context.getCanvas()) return;

        Point2D.Double cursor = context.getSketchCanvas().transformToCanvas(event.getX(),event.getY());

        
        if(MouseEvent.MouseMoved == event.getType()) {
            mouseMoved(event,cursor);
            return;
        }
        if(MouseEvent.MousePressed == event.getType()) {
            if(event.isCommandPressed()) {
                doingPanZoom = true;
                panZoomStart = event.getPointInNodeCoords(context.getCanvas());
                return;
            }
            mousePressed(event,cursor);
            return;
        }
        if(MouseEvent.MouseDragged == event.getType()) {
            if(doingPanZoom) {
                Point2D current = event.getPointInNodeCoords(context.getCanvas());
                Point2D diff = GeomUtil.subtract(current,panZoomStart);
                if(event.isShiftPressed()) {
                    double scale = 0.97;
                    double xoff = 5;
                    double yoff = 5;
                    if(diff.getY() < 0) {
                        scale = 1/scale;
                        xoff *= -1;
                        yoff *= -1;
                    }
                    context.getSketchCanvas().setPanX(context.getSketchCanvas().getPanX()+xoff);
                    context.getSketchCanvas().setPanY(context.getSketchCanvas().getPanY()+yoff);
                    context.getSketchCanvas().setScale(context.getSketchCanvas().getScale()*scale);
                } else {
                    context.getSketchCanvas().setPanX(context.getSketchCanvas().getPanX()+diff.getX());
                    context.getSketchCanvas().setPanY(context.getSketchCanvas().getPanY()+diff.getY());
                }
                panZoomStart = current;
                return;
            }
            mouseDragged(event,cursor);
            return;
        }
        if(MouseEvent.MouseReleased == event.getType()) {
            if(doingPanZoom) {
                doingPanZoom = false;
                return;
            }
            mouseReleased(event,cursor);
            return;
        }
    }

    protected abstract void mouseMoved(MouseEvent event, Point2D.Double cursor);
    protected abstract void mousePressed(MouseEvent event, Point2D.Double cursor);
    protected abstract void mouseDragged(MouseEvent event, Point2D.Double cursor);
    protected abstract void mouseReleased(MouseEvent event, Point2D.Double cursor);
}
