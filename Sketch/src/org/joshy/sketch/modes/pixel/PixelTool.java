package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/17/11
 * Time: 9:49 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PixelTool implements Callback<Event> {
    private boolean enabled;
    private PixelDocContext context;

    public PixelTool(PixelDocContext context) {
        enabled = false;
        this.context = context;
         EventBus.getSystem().addListener(context.getCanvas(), MouseEvent.MouseAll, this);
         EventBus.getSystem().addListener(context.getCanvas(), KeyEvent.KeyAll, this);
         EventBus.getSystem().addListener(context.getCanvas(), ScrollEvent.ScrollAll, this);
    }

    public PixelDocContext getContext() {
        return context;
    }
    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public void call(Event event) throws Exception {
        if(!enabled) return;
        //if(event instanceof KeyEvent) call((KeyEvent)event);
        if(event instanceof MouseEvent) call((MouseEvent)event);
        //if(event instanceof ScrollEvent) call((ScrollEvent)event);
    }

    private void call(MouseEvent event) {
        if(event.getSource() != context.getCanvas()) return;
        //Point2D.Double cursor = context.getSketchCanvas().transformToCanvas(event.getX(),event.getY());
        if(MouseEvent.MousePressed == event.getType()) {
            mousePressed(event, 0, 0);
            return;
        }
        if(MouseEvent.MouseDragged == event.getType()) {
            mouseDragged(event, 0, 0);
            return;
        }
        if(MouseEvent.MouseReleased == event.getType()) {
            mouseReleased(event, 0, 0);
            return;
        }
    }
    //protected abstract void mouseMoved(MouseEvent event, Point2D.Double cursor, );
    protected abstract void mousePressed(MouseEvent event, int x, int y);
    protected abstract void mouseDragged(MouseEvent event, int x, int y);
    protected abstract void mouseReleased(MouseEvent event, int x, int y);

    public void drawOverlay(GFX gfx) {

    }
}
