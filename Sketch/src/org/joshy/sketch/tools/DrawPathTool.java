package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.util.u;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.UndoableAddNodeAction;
import org.joshy.sketch.model.SPath;
import org.joshy.sketch.model.SketchDocument;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/*
    add hover cursors
    delete:
        //while drawing, hover over existing point,
        //show delete icon, press and release to delete the point
    complete / close:
        //while drawing, hover over start point, show complete icon,
        //press and release to complete the path
    convert:
        //hold alt while hovering to show convert icon
        //alt + press on point reset the point to straight, then drag out and release to adjust the point, now with controls bound together
        alt + press on control handle to unbind the point and adjust handles separately
    re-edit
        if a single path is selected when switching to the edit path tool,
        then let you add/delete/convert points in existing path.  you
        can't add to the end, though, or complete it.
    hold shift during any drag operation to constrain to 45degree angles
    hold spacebar during drag operation to move the point
        and it's control points instead of just moving the active control point

    double clicking on a path during selection mode opens
        it up for editing, so you can add/delete/convert/move all points

    when re-editing you can click on an existing node to select it. it's control points will then be visible.
        drag a control point to break them and move it
        click and move an anchor point to move it
        option click to turn an anchor point into a corner
        option drag to reshape an anchor point, which rebinds the control points together

        click the X key to cycle between select, delete, reshape as the default action when you click
        
    
 */
public class DrawPathTool extends CanvasTool {
    private SPath node;
    private SPath.PathPoint currentPoint;
    private boolean couldClose;
    private SPath.PathPoint hoverPoint = null;
    private boolean couldDelete;
    private boolean couldReshape;
    private boolean adjusting;
    private boolean spacePressed;
    private boolean altPressed;
    private boolean couldMove;
    private Point2D.Double prev;
    private Point2D.Double curr;
    private Point2D.Double start;
    private boolean editingExisting;
    private SPath.PathTuple addLocation;
    private SPath.PathPoint selectedPoint;
    private boolean adjustingControlPoint;
    private int activeControlPoint;
    private Cursor penCursor;
    private Cursor reshapeCursor;
    private Cursor deleteCursor;
    private Cursor addCursor;

    private enum Tool { Delete, Reshape, Move };
    private Tool defaultTool = Tool.Move;

    public DrawPathTool(VectorDocContext context) {
        super(context);
        try {
            BufferedImage cursorImage = ImageIO.read(Main.class.getResource("resources/pentool_cursor.png"));
            penCursor = java.awt.Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new java.awt.Point(5,0),"pen");
            BufferedImage reshapeCursorImage = ImageIO.read(Main.class.getResource("resources/pen_^.png"));
            reshapeCursor = java.awt.Toolkit.getDefaultToolkit().createCustomCursor(reshapeCursorImage, new java.awt.Point(5,0),"pen");
            BufferedImage deleteCursorImage = ImageIO.read(Main.class.getResource("resources/pen_-.png"));
            deleteCursor = java.awt.Toolkit.getDefaultToolkit().createCustomCursor(deleteCursorImage, new java.awt.Point(5,0),"pen");
            BufferedImage addCursorImage = ImageIO.read(Main.class.getResource("resources/pen_+.png"));
            addCursor = java.awt.Toolkit.getDefaultToolkit().createCustomCursor(addCursorImage, new java.awt.Point(5,0),"pen");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void call(KeyEvent event) {
        if(event.getType() == KeyEvent.KeyPressed) {
            if(event.getKeyCode() == KeyEvent.KeyCode.KEY_SPACE) {
                spacePressed = true;
            }
            if(event.getKeyCode() == KeyEvent.KeyCode.KEY_ALT) {
                altPressed = true;
            }
            if(event.getKeyCode() == KeyEvent.KeyCode.KEY_X) {
                cycleTool();
            }
            refreshStates();
        }
        if(event.getType() == KeyEvent.KeyReleased) {
            if(event.getKeyCode() == KeyEvent.KeyCode.KEY_SPACE) {
                spacePressed = false;
            }
            if(event.getKeyCode() == KeyEvent.KeyCode.KEY_ALT) {
                altPressed = false;
            }
            if(event.getKeyCode() == KeyEvent.KeyCode.KEY_ESCAPE) {
                context.releaseControl();
            }
            refreshStates();
        }
        context.redraw();
    }

    private void setCursor(Cursor cursor) {
        Frame frame = (Frame) context.getStage().getNativeWindow();
        frame.setCursor(cursor);
    }

    private void setDefaultCursor() {
        Frame frame = (Frame) context.getStage().getNativeWindow();
        frame.setCursor(Cursor.getDefaultCursor());
    }


    private void cycleTool() {
        switch(defaultTool) {
            case Move: defaultTool = Tool.Delete; break;
            case Delete: defaultTool = Tool.Reshape; break;
            case Reshape: defaultTool = Tool.Move; break;
        }
        context.redraw();
    }

    public void enable() {
        super.enable();
        setCursor(penCursor);
    }

    public void disable() {
        if(enabled) {
            setDefaultCursor();
            if(node != null) {
                if(!editingExisting) {
                    SketchDocument doc = (SketchDocument) context.getDocument();
                    doc.getCurrentPage().add(node);
                    context.getUndoManager().pushAction(new UndoableAddNodeAction(context,node,"path"));
                    node.close(false);
                }
                node = null;
                clear();
                editingExisting = false;
            }
        }
        super.disable();
    }


    private void clear() {
        adjusting = false;
        currentPoint = null;
        couldClose = false;
        couldDelete = false;
        couldReshape = false;
        couldMove = false;
        spacePressed = false;
        hoverPoint = null;
        adjustingControlPoint = false;
        activeControlPoint = 0;
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
        curr = translate(cursor,node);
        refreshStates();
    }

    private Point2D.Double translate(Point2D pt, SPath node) {
        if(node != null) {
            return new Point2D.Double(
                    pt.getX()-node.getTranslateX(),
                    pt.getY()-node.getTranslateY()
            );
        }
        return new Point2D.Double(pt.getX(),pt.getY());
    }

    private double getPointThreshold() {
        return 10.0/context.getSketchCanvas().getScale();
    }
    private void refreshStates() {
        if(node != null && !adjusting) {
            hoverPoint = null;
            couldClose = false;
            couldDelete = false;
            couldReshape = false;
            couldMove = false;

            setCursor(penCursor);
            
            //main for hovering over close point
            SPath.PathPoint start = node.points.get(0);
            if(start.distance(curr.x,curr.y) < getPointThreshold() && !node.isClosed()) {
                couldClose = true;
                hoverPoint = start;
                context.redraw();
                return;
            }

            for(SPath.PathPoint pt : node.points) {
                if(pt.distance(curr.x,curr.y)<getPointThreshold()) {
                    if(!couldClose) {
                        hoverPoint = pt;
                        if(defaultTool == Tool.Move) {
                            couldMove = true;
                            setCursor(penCursor);
                            addLocation = null;
                            context.redraw();
                            return;
                        }
                        if(defaultTool == Tool.Reshape) {
                            couldReshape = true;
                            setCursor(reshapeCursor);
                            addLocation = null;
                            context.redraw();
                            return;
                        }
                        if(defaultTool == Tool.Delete) {
                            couldDelete = true;
                            setCursor(deleteCursor);
                            addLocation = null;
                            context.redraw();
                            return;
                        }
                    }
                }
            }

            boolean foundClose = false;
            for(SPath.PathSegment seg : node.calculateSegments()) {
                SPath.PathTuple closest = seg.closestDistance(curr);
                if(closest.distance < getPointThreshold()) {
                    addLocation = closest;
                    foundClose = true;
                }
            }
            if(!foundClose) {
                addLocation = null;
            } else {
                setCursor(addCursor);                
            }

            //main for hovering over possible deletion point
            context.redraw();
        }
    }


    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        start = translate(cursor,node);
        prev = translate(cursor,node);
        if(node == null) {
            node = new SPath();
            currentPoint = new SPath.PathPoint(start.x,start.y);
            node.addPoint(currentPoint);
            context.redraw();
            return;
        }

        if(selectedPoint != null && (defaultTool == Tool.Move || defaultTool == Tool.Reshape)) {
            if(start.distance(selectedPoint.x,selectedPoint.y) < getPointThreshold()) {
                u.p("can reshape the center of the point");
                adjusting = true;
                context.redraw();
                return;
            }
            if(start.distance(selectedPoint.cx1,selectedPoint.cy1) < getPointThreshold()) {
                u.p("can adjust the control point 1");
                adjustingControlPoint = true;
                activeControlPoint = 1;
                context.redraw();
                return;
            }
            if(start.distance(selectedPoint.cx2,selectedPoint.cy2) < getPointThreshold()) {
                u.p("can adjust the control point 2");
                adjustingControlPoint = true;
                activeControlPoint = 2;
                context.redraw();
                return;
            }
        }

        if(couldMove) {
            adjusting = true;
            selectedPoint = hoverPoint;
            context.redraw();
            return;
        }

        if(spacePressed && hoverPoint != null) {
            context.redraw();
            return;
        }

        if(couldClose) {
            SPath.PathPoint start = node.points.get(0);
            if(start.distance(start.x,start.y) < getPointThreshold()) {
                couldClose = false;
                node.close(true);
                SketchDocument doc = (SketchDocument) context.getDocument();
                doc.getCurrentPage().add(node);
                context.getUndoManager().pushAction(new UndoableAddNodeAction(context,node,"path"));
                node.normalize();
                currentPoint = null;
                node = null;
                context.releaseControl();
            }
            context.redraw();
            return;
        }

        if(couldReshape) {
            hoverPoint.cx1 = hoverPoint.x;
            hoverPoint.cx2 = hoverPoint.x;
            hoverPoint.cy1 = hoverPoint.y;
            hoverPoint.cy2 = hoverPoint.y;
            selectedPoint = hoverPoint;
            adjusting = true;
            context.redraw();
            return;
        }

        if(couldDelete) {
            node.points.remove(hoverPoint);
            context.redraw();
            return;
        }
        
        if(addLocation != null) {
            node.splitPath(addLocation);
            context.redraw();
            addLocation = null;
            return;
        }


        if(!editingExisting) {
            currentPoint = new SPath.PathPoint(start.x,start.y);
            node.addPoint(currentPoint);
            context.redraw();
        }
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        SPath.PathPoint point = null;
        curr = translate(cursor,node);

        if(this.currentPoint != null) {
            point = this.currentPoint;
        }
        if(adjusting) {
            point = hoverPoint;
        }
        if(adjustingControlPoint) {
            point = selectedPoint;
        }
        
        if(point != null) {

            if(adjustingControlPoint) {
                if(activeControlPoint == 1) {
                    point.cx1 = curr.x;
                    point.cy1 = curr.y;
                }
                if(activeControlPoint == 2) {
                    point.cx2 = curr.x;
                    point.cy2 = curr.y;
                }
            } else if(spacePressed || couldMove) {
                //move a point uniformly
                double dx = curr.x-prev.x;
                double dy = curr.y-prev.y;
                point.x +=dx;
                point.y +=dy;
                point.cx1 +=dx;
                point.cx2 +=dx;
                point.cy1 +=dy;
                point.cy2 +=dy;
            } else {
                //adjust the control points
                point.cx2 = curr.x;
                point.cy2 = curr.y;
                double dx = point.cx2-point.x;
                double dy = point.cy2-point.y;
                point.cx1 = point.x-dx;
                point.cy1 = point.y-dy;
            }
            context.redraw();
        }
        prev = translate(cursor,node);
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        if(node != null) {
            node.recalcPath();
        }
        clear();
    }

    public void drawOverlay(GFX g) {
        //draw the current tool indicator
        g.setPaint(FlatColor.BLACK);
        g.drawText(defaultTool.toString(), Font.DEFAULT, 100,100);

        //draw overlays
        if(node != null) {

            //draw the path and handle overlays
            g.translate(context.getSketchCanvas().getPanX(),context.getSketchCanvas().getPanY());
            g.scale(context.getSketchCanvas().getScale(),context.getSketchCanvas().getScale());
            g.translate(node.getTranslateX(),node.getTranslateY());
            node.drawPath(g,node);

            //draw the add location
            if(addLocation != null) {
                g.setPaint(FlatColor.RED);
                double size = 10.0/context.getSketchCanvas().getScale();
                g.drawOval(addLocation.point.getX()-size/2,addLocation.point.getY()-size/2,size,size);
            }

            g.translate(-node.getTranslateX(),-node.getTranslateY());
            g.scale(1/context.getSketchCanvas().getScale(),1/context.getSketchCanvas().getScale());
            g.translate(-context.getSketchCanvas().getPanX(),-context.getSketchCanvas().getPanY());
            if(adjusting) {
                drawHandles(g,hoverPoint);
            }
            if(selectedPoint != null) {
                drawHandles(g,selectedPoint);
            }
            drawHandles(g,node,5.0,1.0);

            //draw text notifications
            if(couldClose) {
                SPath.PathPoint point = node.points.get(0);
                Point2D.Double pt = context.getSketchCanvas().transformToDrawing(node.getTranslateX()+point.x,node.getTranslateY()+point.y);
                g.setPaint(FlatColor.RED);
                g.drawRect(pt.x-2,pt.y-2,5,5);

                g.setPaint(FlatColor.BLACK);
                g.drawText("close", Font.DEFAULT,pt.x-3,pt.y+30);
            }
            
            if(hoverPoint != null) {
                Point2D.Double hp = context.getSketchCanvas().transformToDrawing(node.getTranslateX()+hoverPoint.x,node.getTranslateY()+hoverPoint.y);
                if(couldMove && hoverPoint != null) {
                    g.setPaint(FlatColor.RED);
                    g.drawRect(hp.x-2,hp.y-2,5,5);

                    g.setPaint(FlatColor.BLACK);
                    g.drawText("move", Font.DEFAULT,hp.x-3,hp.y+30);
                }
                if(couldDelete) {
                    g.setPaint(FlatColor.RED);
                    g.drawRect(hp.x-2,hp.y-2,5,5);

                    g.setPaint(FlatColor.BLACK);
                    g.drawText("delete", Font.DEFAULT,hp.x-3,hp.y+30);
                }
                if(couldReshape && hoverPoint != null) {
                    g.setPaint(FlatColor.RED);
                    g.drawRect(hp.x-2,hp.y-2,5,5);

                    g.setPaint(FlatColor.BLACK);
                    if(spacePressed) {
                        g.drawText("move", Font.DEFAULT,hp.x-3,hp.y+30);
                    } else {
                        g.drawText("reshape", Font.DEFAULT,hp.x-3,hp.y+30);
                    }
                }
            }
        }
    }

    private void drawHandles(GFX g, SPath node, double size, double sw) {
        g.setPureStrokes(true);
        g.setPaint(FlatColor.BLACK);

        int last = node.points.size()-1;
        for(int i=0; i<node.points.size(); i++) {
            SPath.PathPoint point = node.points.get(i);
            Point2D.Double xy = context.getSketchCanvas().transformToDrawing(point.x+node.getTranslateX(),point.y+node.getTranslateY());
            Point2D.Double c1 = context.getSketchCanvas().transformToDrawing(point.cx1+node.getTranslateX(),point.cy1+node.getTranslateY());
            Point2D.Double c2 = context.getSketchCanvas().transformToDrawing(point.cx2+node.getTranslateX(),point.cy2+node.getTranslateY());

            g.setPaint(FlatColor.BLACK);
            g.fillRect(xy.x-size/2,xy.y-size/2,size,size);
            g.setPaint(FlatColor.WHITE);
            g.fillRect(xy.x-size/2+sw,xy.y-size/2+sw,size-sw*2,size-sw*2);
            if(i == last) {
                g.setPaint(FlatColor.BLACK);
                g.fillRect(c1.x-2-1,c1.y-2-1,size+2,size+2);
                g.fillRect(c2.x-2-1,c2.y-2-1,size+2,size+2);
                g.setPaint(FlatColor.RED);
                g.fillRect(c1.x-2,c1.y-2,size,size);
                g.fillRect(c2.x-2,c2.y-2,size,size);
                g.setPaint(FlatColor.RED);
                g.drawLine(xy.x,xy.y,c1.x,c1.y);
                g.drawLine(xy.x,xy.y,c2.x,c2.y);
            }
        }
        g.setPureStrokes(false);
    }

    private void drawHandles(GFX g, SPath.PathPoint point) {
        double size = 5.0;
        point = convertToDrawing(point);

        //the point itself
        g.drawRect(point.x-2,point.y-2,size,size);

        //the control points
        g.drawRect(point.cx1-2,point.cy1-2,size,size);
        g.drawRect(point.cx2-2,point.cy2-2,size,size);
        //line connecting the control points
        g.setPaint(FlatColor.RED);
        g.drawLine(point.x,point.y,point.cx1,point.cy1);
        g.drawLine(point.x,point.y,point.cx2,point.cy2);
    }

    private SPath.PathPoint convertToDrawing(SPath.PathPoint point) {
        Point2D.Double xy = context.getSketchCanvas().transformToDrawing(point.x+node.getTranslateX(),point.y+node.getTranslateY());
        Point2D.Double c1 = context.getSketchCanvas().transformToDrawing(point.cx1+node.getTranslateX(),point.cy1+node.getTranslateY());
        Point2D.Double c2 = context.getSketchCanvas().transformToDrawing(point.cx2+node.getTranslateX(),point.cy2+node.getTranslateY());
        return new SPath.PathPoint(xy.x,xy.y,c1.x,c1.y,c2.x,c2.y);
    }

    public static Path2D toPath2D(SPath node) {
        Path2D.Double pt = new Path2D.Double();
        for(int i=0; i<node.points.size(); i++) {
            SPath.PathPoint point = node.points.get(i);
            if(i == 0) {
                pt.moveTo(point.x,point.y);
                continue;
            }
            SPath.PathPoint prev = node.points.get(i - 1);
            pt.curveTo(prev.cx2,prev.cy2,
                    point.cx1,point.cy1,
                    point.x,point.y
                    );
            if(i == node.points.size()-1) {
                if(node.isClosed()) {
                SPath.PathPoint first = node.points.get(0);
                pt.curveTo(point.cx2,point.cy2,
                        first.cx1,first.cy1,
                        first.x,first.y
                        );
                }
                pt.closePath();
            }
        }
        return pt;
    }

    public void startEditing(SPath path) {
        clear();
        this.node = path;
        editingExisting = true;
        context.redraw();
    }
}
