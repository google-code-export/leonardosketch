package org.joshy.sketch.tools;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Point2D;

public class PanCanvasTool extends CanvasTool {
    private Point2D start;

    public PanCanvasTool(VectorDocContext context) {
        super(context);
    }

    @Override
    public void call(KeyEvent event) {
        //do nothing
    }

    public void drawOverlay(GFX g) {
        //do nothing
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
        start = event.getPointInNodeCoords(context.getCanvas());
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {

    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        double deltaX = cursor.getX()-start.getX();
        double deltaY = cursor.getY()-start.getY();
//        context.getSketchCanvas().setPanX(context.getSketchCanvas().getPanX()+deltaX);
//        context.getSketchCanvas().setPanY(context.getSketchCanvas().getPanY()+deltaY);
        start = cursor;
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
    }

}
