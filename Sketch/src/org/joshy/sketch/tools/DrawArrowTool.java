package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.sketch.actions.UndoableAddNodeAction;
import org.joshy.sketch.model.SArrow;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Point2D;

public class DrawArrowTool extends CanvasTool {
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
        cursor = snapToGrid(cursor);
        node = new SArrow(cursor,cursor);
        node.setStrokeWidth(3);
        node.setFillPaint(FlatColor.BLACK);
        context.getDocument().getCurrentPage().add(node);
        context.getUndoManager().pushAction(new UndoableAddNodeAction(context,node,"arrow"));
        context.redraw();
    }


    private Point2D.Double snapToGrid(Point2D.Double point) {
        double nx = point.getX();
        double ny = point.getY();
        if(context.getDocument().isSnapGrid()) {
            nx = ((int)(nx/context.getDocument().getGridWidth()))*context.getDocument().getGridWidth();
            ny = ((int)(ny/context.getDocument().getGridHeight()))*context.getDocument().getGridHeight();
        }
        return new Point2D.Double(nx,ny);
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        cursor = snapToGrid(cursor);
        node.setStart(cursor);
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
