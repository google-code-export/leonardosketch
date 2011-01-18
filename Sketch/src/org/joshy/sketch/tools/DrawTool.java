package org.joshy.sketch.tools;

import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.sketch.actions.UndoableAddNodeAction;
import org.joshy.sketch.model.AbstractResizeableNode;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Point2D;

/**
 * The parent class for drawing all resizable nodesn
 * User: joshmarinacci
 * Date: Apr 6, 2010
 * Time: 3:13:20 PM
 */
public abstract class DrawTool extends CanvasTool {
    protected AbstractResizeableNode node;
    private Point2D start;

    public DrawTool(VectorDocContext context) {
        super(context);
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
    }

    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        cursor = snapToGrid(cursor);
        start = cursor;

        SketchDocument doc = context.getDocument();
        doc.getCurrentPage().add(node);
        context.getUndoManager().pushAction(new UndoableAddNodeAction(context, node,"node"));
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


    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        cursor = snapToGrid(cursor);
        Point2D diff = GeomUtil.subtract(cursor, start);
        if(diff.getX() >= 0) {
            node.setTranslateX(start.getX());
            node.setWidth(diff.getX());
        } else {
            node.setTranslateX(cursor.getX());
            node.setWidth(-diff.getX());
        }
        if(diff.getY() >= 0) {
            node.setTranslateY(start.getY());
            node.setHeight(diff.getY());
            if(event.isShiftPressed()) {
                double ratio = node.getPreferredAspectRatio();
                node.setHeight(node.getWidth()*ratio);
            }
        } else {
            node.setTranslateY(cursor.getY());
            node.setHeight(-diff.getY());
            if(event.isShiftPressed()) {
                double ratio = node.getPreferredAspectRatio();
                node.setHeight(node.getWidth()*ratio);
                node.setTranslateY(start.getY()-node.getWidth()*ratio);
            }
        }
        context.redraw();
    }

    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        if(node != null) {
            context.getSelection().setSelectedNode(node);
        }
        node = null;
        this.context.releaseControl();
    }

}
