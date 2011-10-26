package org.joshy.sketch.tools;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.sketch.actions.UndoableAddNodeAction;
import org.joshy.sketch.model.SArrow;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Point2D;

public class
        DrawArrowTool extends CanvasTool {
    private Point2D start;
    private SArrow node;

    public DrawArrowTool(VectorDocContext context) {
        super(context);
    }

    @Override
    public void call(KeyEvent event) {
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {

    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        start = event.getPointInNodeCoords(context.getCanvas());
        node = new SArrow(start,start);
        context.getDocument().getCurrentPage().add(node);
        context.getUndoManager().pushAction(new UndoableAddNodeAction(context,node,"arrow"));
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        Point2D current = event.getPointInNodeCoords(context.getCanvas());
        node.setStart(current);
        context.redraw();
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        context.redraw();
        context.getSelection().setSelectedNode(node);
        node = null;
        context.releaseControl();
    }

    public void drawOverlay(GFX g) {

    }

}
