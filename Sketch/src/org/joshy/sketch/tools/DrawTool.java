package org.joshy.sketch.tools;

import org.joshy.gfx.event.MouseEvent;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.actions.UndoableAddNodeAction;
import org.joshy.sketch.model.AbstractResizeableNode;
import org.joshy.sketch.model.SketchDocument;

import java.awt.geom.Point2D;

/**
 * The parent class for drawing all resizable nodesn
 * User: joshmarinacci
 * Date: Apr 6, 2010
 * Time: 3:13:20 PM
 */
public abstract class DrawTool extends CanvasTool {
    protected AbstractResizeableNode node;
    private double startX;
    private double startY;

    public DrawTool(VectorDocContext context) {
        super(context);
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
    }

    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        startX = cursor.getX();
        startY = cursor.getY();
        SketchDocument doc = (SketchDocument) context.getDocument();
        doc.getCurrentPage().add(node);
        context.getUndoManager().pushAction(new UndoableAddNodeAction(context, node,"node"));
        context.redraw();
    }


    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        double diffx = cursor.getX() - startX;
        double diffy = cursor.getY() - startY;
        if(diffx >= 0) {
            node.setTranslateX(startX);
            node.setWidth(diffx);
        } else {
            node.setTranslateX(cursor.getX());
            node.setWidth(-diffx);
        }
        if(diffy >= 0) {
            node.setTranslateY(startY);
            node.setHeight(diffy);
        } else {
            node.setTranslateY(cursor.getY());
            node.setHeight(-diffy);
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
