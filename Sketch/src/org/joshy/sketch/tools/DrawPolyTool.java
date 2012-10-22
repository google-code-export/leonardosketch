package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.sketch.actions.UndoableAddNodeAction;
import org.joshy.sketch.model.SPoly;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Point2D;

public class DrawPolyTool extends CanvasTool {
    private SPoly poly;
    private Point2D.Double point;
    private boolean dragging;
    private Point2D hotspot;

    public DrawPolyTool(VectorDocContext context) {
        super(context);
    }

    @Override
    public void call(KeyEvent event) {
    }

    public void drawOverlay(GFX g) {
        if(dragging) {
            g.setPaint(FlatColor.GRAY);
            if(point.distance(this.hotspot) < 10) {
                g.setPaint(FlatColor.RED);
            }
            Point2D delta = context.getSketchCanvas().transformToDrawing(this.hotspot.getX(), this.hotspot.getY());
            g.translate(delta.getX(), delta.getY());
            g.drawOval(-5,-5,10,10);
            g.translate(-delta.getX(),-delta.getY());
        }
    }

    public void disable() {
        super.disable();
        endPolygon();
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        if(poly == null) {
            poly = new SPoly();
            poly.addPoint(snapPoint(cursor));
            hotspot = cursor;
            SketchDocument doc = context.getDocument();
            doc.getCurrentPage().add(poly);
            context.getUndoManager().pushAction(new UndoableAddNodeAction(context,poly,"polygon"));
            point = new Point2D.Double();
            point.setLocation(cursor);
            poly.addPoint(point);
            context.redraw();
        } else {
            point = new Point2D.Double();
            point.setLocation(cursor);
            poly.addPoint(point);
            context.redraw();
        }
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
        dragging = true;
        if(event.isShiftPressed() && poly.pointCount() >= 2) {
            Point2D prevPoint = poly.getPoint(poly.pointCount()-2);
            double angle = GeomUtil.calcAngle(prevPoint,cursor);
            angle = GeomUtil.snapTo45(angle);
            Point2D ptx = GeomUtil.calcPoint(prevPoint,angle,cursor.distance(prevPoint));
            point.setLocation(snapPoint(ptx));
        } else {
            point.setLocation(snapPoint(cursor));
        }
        context.redraw();
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        dragging = false;
        if(poly != null && point != null) {
            if(point.distance(hotspot) < 10) {
                point.setLocation(hotspot);
                poly.setClosed(true);
                //remove the last point since it would be a duplicate
                poly.removePoint(point);
                context.getSelection().setSelectedNode(poly);
                Bounds bounds = poly.getTransformedBounds();
                poly.setAnchorX(bounds.getCenterX());
                poly.setAnchorY(bounds.getCenterY());
                poly = null;
                hotspot = null;
                point = null;
                context.releaseControl();
            }
        }
        context.redraw();
    }
    
    private void endPolygon() {
        if(poly != null) {
            poly.setClosed(false);
            poly = null;
            hotspot = null;
            point = null;
        }
    }

}
