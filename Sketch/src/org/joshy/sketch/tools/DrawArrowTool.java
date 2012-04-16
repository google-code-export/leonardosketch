package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.sketch.actions.UndoableAddNodeAction;
import org.joshy.sketch.model.SArrow;
import org.joshy.sketch.model.SketchDocument;
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
        cursor = snapPoint(cursor);
        node = new SArrow(cursor,cursor);
        node.setStrokeWidth(3);
        node.setFillPaint(FlatColor.BLACK);
        context.getDocument().getCurrentPage().add(node);
        context.getUndoManager().pushAction(new UndoableAddNodeAction(context,node,"arrow"));
        context.redraw();
    }


    private Point2D.Double snapPoint(Point2D cursor) {
        double nx = cursor.getX();
        double ny = cursor.getY();
        SketchDocument doc = context.getDocument();
        if(doc.isSnapGrid()) {
            nx = ((int)(nx/doc.getGridWidth()))*doc.getGridWidth();
            ny = ((int)(ny/doc.getGridHeight()))*doc.getGridHeight();
        }
        return new Point2D.Double(nx,ny);
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        if(event.isShiftPressed()) {
            Point2D prevPoint = node.getEnd();
            double angle = GeomUtil.calcAngle(prevPoint, cursor);
            angle = GeomUtil.snapTo45(angle);
            Point2D ptx = GeomUtil.calcPoint(prevPoint,angle,cursor.distance(prevPoint));
            cursor = snapPoint(ptx);
        } else {
            cursor = snapPoint(cursor);
        }
        context.redraw();

        node.setStart(cursor);
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
