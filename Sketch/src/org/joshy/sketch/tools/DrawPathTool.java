package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Transform;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Togglebutton;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.layout.FlexBox;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.UndoManager;
import org.joshy.sketch.actions.UndoableAddNodeAction;
import org.joshy.sketch.model.SPath;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
    private SPath.PathPoint undoReference;
    private SPath.PathPoint redoReference;
    private FlexBox panel;
    private Togglebutton moveButton;
    private Togglebutton deleteButton;
    private Togglebutton reshapeButton;
    private Button endButton;
    private SPath.SubPath hoverSubpath;

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
        panel = new HFlexBox().setBoxAlign(HFlexBox.Align.Baseline);
        moveButton = new Togglebutton("move");
        moveButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                defaultTool = Tool.Move;
                updateToolButtons();
            }
        });
        panel.add(moveButton);
        deleteButton = new Togglebutton("delete");
        deleteButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                defaultTool = Tool.Delete;
                updateToolButtons();
            }
        });
        panel.add(deleteButton);
        reshapeButton = new Togglebutton("reshape");
        reshapeButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                defaultTool = Tool.Reshape;
                updateToolButtons();
            }
        });
        panel.add(reshapeButton);
        endButton = new Button("end");
        endButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                DrawPathTool.this.context.releaseControl();
            }
        });
        panel.add(endButton);
        panel.setTranslateX(100);
        panel.setTranslateY(20);

        defaultTool = Tool.Move;
        updateToolButtons();
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
        updateToolButtons();
        context.redraw();
    }

    private void updateToolButtons() {
        moveButton.setSelected(defaultTool==Tool.Move);
        deleteButton.setSelected(defaultTool == Tool.Delete);
        reshapeButton.setSelected(defaultTool == Tool.Reshape);
    }

    public void enable() {
        super.enable();
        setCursor(penCursor);
        NodeUtils.doSkins(panel);
        panel.doPrefLayout();
        panel.doLayout();
        panel.setFill(FlatColor.BLACK.deriveWithAlpha(0.3));
        context.getCanvas().getParent().getStage().getPopupLayer().add(panel);
        Point2D pt = NodeUtils.convertToScene(context.getCanvas(), 20, 20);
        panel.setTranslateX(pt.getX());
        panel.setTranslateY(pt.getY());
    }

    public void disable() {
        if(enabled) {
            setDefaultCursor();
            if(node != null) {
                if(!editingExisting) {
                    SketchDocument doc = context.getDocument();
                    doc.getCurrentPage().add(node);
                    context.getUndoManager().pushAction(new UndoableAddNodeAction(context,node,"path"));
                }
                node = null;
                clear();
                editingExisting = false;
            }
        }
        super.disable();
        context.getCanvas().getParent().getStage().getPopupLayer().remove(panel);
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
        curr = toolToModel(cursor);
        refreshStates();
    }

    private double getPointThreshold() {
        return 10.0/context.getSketchCanvas().getScale();
    }

    private void refreshStates() {
        if(node != null && !adjusting && curr != null) {
            hoverPoint = null;
            hoverSubpath = null;
            couldClose = false;
            couldDelete = false;
            couldReshape = false;
            couldMove = false;

            setCursor(penCursor);
            
            for(SPath.SubPath sub : node.getSubPaths()) {
                //hovering over close point
                SPath.PathPoint start = sub.getPoint(0);
                if(start.distance(curr.x,curr.y) < getPointThreshold() && !sub.closed()) {
                    hoverSubpath = sub;
                    couldClose = true;
                    hoverPoint = start;
                    context.redraw();
                    return;
                }

                for(SPath.PathPoint pt : sub.getPoints()) {
                    if(pt.distance(curr.x,curr.y)<getPointThreshold()) {
                        hoverSubpath = sub;
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
            }

            boolean foundClose = false;
            for(SPath.SubPath sub : node.getSubPaths()) {
                for(SPath.PathSegment seg : sub.calculateSegments()) {
                    SPath.PathTuple closest = seg.closestDistance(curr);
                    if(closest.distance < getPointThreshold()) {
                        addLocation = closest;
                        hoverSubpath = sub;
                        foundClose = true;
                    }
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
        start = toolToModel(cursor);
        prev = toolToModel(cursor);
        if(node == null) {
            node = new SPath();
            currentPoint = new SPath.PathPoint(start.x,start.y);
            currentPoint.startPath = true;
            node.addPoint(currentPoint);
            context.getUndoManager().pushAction(new UndoableAddNodeAction(context,node,"path"));
            context.redraw();
            return;
        }

        if(selectedPoint != null && (defaultTool == Tool.Move || defaultTool == Tool.Reshape)) {
            undoReference = selectedPoint.copy();
            redoReference = selectedPoint;
            if(start.distance(selectedPoint.x,selectedPoint.y) < getPointThreshold()) {
                //u.p("can reshape the center of the point");
                adjusting = true;
                context.redraw();
                return;
            }
            if(start.distance(selectedPoint.cx1,selectedPoint.cy1) < getPointThreshold()) {
                //u.p("can adjust the control point 1");
                adjustingControlPoint = true;
                activeControlPoint = 1;
                context.redraw();
                return;
            }
            if(start.distance(selectedPoint.cx2,selectedPoint.cy2) < getPointThreshold()) {
                //u.p("can adjust the control point 2");
                adjustingControlPoint = true;
                activeControlPoint = 2;
                context.redraw();
                return;
            }
        }

        if(couldMove) {
            adjusting = true;
            selectedPoint = hoverPoint;
            undoReference = selectedPoint.copy();
            redoReference = selectedPoint;
            context.redraw();
            return;
        }

        if(spacePressed && hoverPoint != null) {
            context.redraw();
            return;
        }
        
        undoReference = null;
        redoReference = null;

        if(couldClose) {
            SPath.PathPoint start = hoverSubpath.getPoint(0);
            if(start.distance(start.x,start.y) < getPointThreshold()) {
                couldClose = false;
                hoverSubpath.doAutoclose();
                SketchDocument doc = context.getDocument();
                doc.getCurrentPage().add(node);
                //node already added at this point
                //context.getUndoManager().pushAction(new UndoableAddNodeAction(context,node,"path"));
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
            undoReference = selectedPoint.copy();
            redoReference = selectedPoint;
            context.redraw();
            return;
        }

        if(couldDelete) {
            deletePoint(hoverPoint);
            return;
        }
        
        if(addLocation != null) {
            insertPoint();
            return;
        }


        if(!editingExisting) {
            addPoint(start);
            return;
        }
    }

    private void addPoint(Point2D.Double start) {
        if(start == null) return;
        if(hoverSubpath == null) {
            hoverSubpath = node.getSubPaths().get(0);
        }
        currentPoint = new SPath.PathPoint(start.x,start.y);
        final SPath.PathPoint temp = currentPoint;
        hoverSubpath.addPoint(currentPoint);
        final SPath.SubPath tempsub = hoverSubpath;
        context.redraw();        
        context.getUndoManager().pushAction(new UndoManager.UndoableAction(){
            public void executeUndo() {
                tempsub.removePoint(temp);
            }
            public void executeRedo() {
                tempsub.addPoint(temp);
            }
            public String getName() {
                return "add point";
            }
        });        
    }

    private void insertPoint() {
        if(hoverSubpath == null) return;
        if(addLocation == null) return;
        final SPath.PathTuple temp = addLocation.copy();
        final SPath.PathPoint a = addLocation.a.copy();
        final SPath.PathPoint b = addLocation.b.copy();
        final SPath.PathPoint pt = hoverSubpath.splitPath(addLocation);
        final SPath.SubPath tempsub = hoverSubpath;
        context.getUndoManager().pushAction(new UndoManager.UndoableAction(){
            public void executeUndo() {
                tempsub.unSplitPath(temp, a, b, pt);
            }
            public void executeRedo() {
                tempsub.splitPath(temp);
            }
            public String getName() {
                return "insert point";
            }
        });
        context.redraw();
        addLocation = null;
    }

    private void deletePoint(SPath.PathPoint hoverPoint) {
        if(hoverPoint == null) return;
        if(hoverSubpath == null) return;
        final SPath.PathPoint temp = hoverPoint.copy();
        final SPath.SubPath cs = hoverSubpath;
        final int tempIndex = hoverSubpath.getPoints().indexOf(hoverPoint);
        hoverSubpath.removePoint(hoverPoint);
        context.getUndoManager().pushAction(new UndoManager.UndoableAction(){
            public void executeUndo() {
                cs.getPoints().add(tempIndex, temp);
            }
            public void executeRedo() {
                cs.getPoints().remove(temp);
            }
            public String getName() {
                return "remove point";
            }
        });
        context.redraw();
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        SPath.PathPoint point = null;
        curr = toolToModel(cursor);

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
                double nx = curr.x;
                double ny = curr.y;
                SketchDocument doc = context.getDocument();
                if(doc.isSnapGrid()) {
                    nx = ((int)(nx/doc.getGridWidth()))*doc.getGridWidth();
                    ny = ((int)(ny/doc.getGridHeight()))*doc.getGridHeight();
                }

                boolean hsnap = false;
                boolean vsnap = false;
                //snap with guidelines next if not already snapped
                nx += node.getTranslateX();
                ny += node.getTranslateY();
                for(SketchDocument.Guideline gl : context.getDocument().getCurrentPage().getGuidelines()) {
                    double threshold = 15;
                    if(!gl.isVertical()) {
                        if(!vsnap) {
                            if(Math.abs(ny - gl.getPosition()) < threshold) {
                                ny = gl.getPosition();
                                context.getCanvas().showVSnap(gl.getPosition());
                                vsnap = true;
                            }
                        }
                    } else {
                        if(!hsnap) {
                            if(Math.abs(nx - gl.getPosition()) < threshold) {
                                nx = gl.getPosition();
                                context.getCanvas().showHSnap(gl.getPosition());
                                hsnap = true;
                            }
                        }
                    }
                }
                nx -= node.getTranslateX();
                ny -= node.getTranslateY();

                double px = point.x;
                double py = point.y;
                point.x = nx;
                point.y = ny;
                double dx = point.x - px;
                double dy = point.y - py;
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
        prev = toolToModel(cursor);
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        if(adjusting || adjustingControlPoint) {
            final SPath.PathPoint startReference = undoReference.copy();
            final SPath.PathPoint endReference = redoReference.copy();
            final SPath.PathPoint tempPathPoint = redoReference;
            context.getUndoManager().pushAction(new UndoManager.UndoableAction(){
                public void executeUndo() {
                    tempPathPoint.copyFrom(startReference);
                    context.redraw();
                }
                public void executeRedo() {
                    tempPathPoint.copyFrom(endReference);
                    context.redraw();
                }
                public String getName() {
                    return "modify point";
                }
            });
        }

        if(node != null) {
            node.recalcPath();
        }
        clear();
    }

    public void drawOverlay(GFX g) {

        //draw overlays
        if(node != null) {

            //draw the path and handle overlays
            g.setPureStrokes(true);
            Path2D.Double path = SPath.toPath(node);

            g.translate(context.getSketchCanvas().getPanX(), context.getSketchCanvas().getPanY());
            g.scale(context.getSketchCanvas().getScale(), context.getSketchCanvas().getScale());
            g.translate(node.getTranslateX(), node.getTranslateY());
            g.translate(node.getAnchorX(), node.getAnchorY());
            g.rotate(node.getRotate(), Transform.Z_AXIS);
            g.scale(node.getScaleX(), node.getScaleY());
            g.translate(-node.getAnchorX(), -node.getAnchorY());
            g.setPaint(FlatColor.GREEN);
            g.drawPath(path);
            if(hoverSubpath != null) {
                Path2D.Double subpath = SPath.toPath(hoverSubpath);
                g.setPaint(FlatColor.RED);
                g.drawPath(subpath);
            }

            g.translate(node.getAnchorX(), node.getAnchorY());
            g.scale(1 / node.getScaleX(), 1 / node.getScaleY());
            g.rotate(-node.getRotate(), Transform.Z_AXIS);
            g.translate(-node.getAnchorX(), -node.getAnchorY());
            g.translate(-node.getTranslateX(),-node.getTranslateY());
            g.scale(1/context.getSketchCanvas().getScale(), 1/context.getSketchCanvas().getScale());
            g.translate(-context.getSketchCanvas().getPanX(), -context.getSketchCanvas().getPanY());
            g.setPureStrokes(false);



            //draw the add location
            if(addLocation != null) {
                g.setPaint(FlatColor.RED);
                Point2D pt = modelToScreen(addLocation.point);
                double size = 10.0/context.getSketchCanvas().getScale();
                g.drawOval(pt.getX()-size/2,pt.getY()-size/2,size,size);
            }


            if(adjusting) {
                drawHandles(g,hoverPoint);
            }
            if(selectedPoint != null) {
                drawHandles(g,selectedPoint);
            }
            drawHandles(g,node,5.0,1.0);

            //draw text notifications
            if(couldClose && hoverSubpath != null) {
                SPath.PathPoint point = hoverSubpath.getPoint(0);
                Point2D pt = modelToScreen(point.x,point.y);
                g.setPaint(FlatColor.RED);
                g.drawRect(pt.getX()-2,pt.getY()-2,5,5);

                g.setPaint(FlatColor.BLACK);
                g.drawText("close", Font.DEFAULT,pt.getX()-3,pt.getY()+30);
            }

            if(hoverPoint != null) {
                Point2D.Double hp = modelToScreen(hoverPoint.x,hoverPoint.y);
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

        SPath.PathPoint last = null;
        for(SPath.SubPath path : node.getSubPaths()) {
            for(SPath.PathPoint point : path.getPoints()) {
                last = modelToScreen(point);
                g.setPaint(FlatColor.BLACK);
                g.fillRect(last.x - size / 2,last.y - size / 2, size, size);
                g.setPaint(FlatColor.WHITE);
                g.fillRect(last.x-size/2+sw,last.y-size/2+sw,size-sw*2,size-sw*2);
            }
        }
        
        if(last != null) {
            g.setPaint(FlatColor.BLACK);
            g.fillRect(last.cx1-2-1,last.cy1-2-1,size+2,size+2);
            g.fillRect(last.cx2-2-1,last.cy2-2-1,size+2,size+2);
            g.setPaint(FlatColor.RED);
            g.fillRect(last.cx1-2,last.cy1-2,size,size);
            g.fillRect(last.cx2-2,last.cy2-2,size,size);
            g.setPaint(FlatColor.RED);
            g.drawLine(last.x,last.y,last.cx1,last.cy1);
            g.drawLine(last.x,last.y,last.cx2,last.cy2);
        }
        g.setPureStrokes(false);
    }

    private void drawHandles(GFX g, SPath.PathPoint point) {
        double size = 5.0;
        point = modelToScreen(point);

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

    private SPath.PathPoint modelToScreen(SPath.PathPoint point) {
        Point2D.Double xy = modelToScreen(point.x,point.y);
        Point2D.Double c1 = modelToScreen(point.cx1,point.cy1);
        Point2D.Double c2 = modelToScreen(point.cx2,point.cy2);
        return new SPath.PathPoint(xy.x,xy.y,c1.x,c1.y,c2.x,c2.y);
    }
    private Point2D.Double modelToScreen(Point2D pt) {
        return modelToScreen(pt.getX(), pt.getY());
    }
    private Point2D.Double modelToScreen(double x, double y) {
        return context.getSketchCanvas().transformToDrawing(modelToTool(new Point2D.Double(x,y),node));
    }
    private Point2D.Double modelToTool(Point2D pt, SPath node) {
        if(node != null) {
            AffineTransform af = new AffineTransform();
            af.translate(node.getTranslateX(),node.getTranslateY());
            af.translate(node.getAnchorX(),node.getAnchorY());
            af.rotate(Math.toRadians(node.getRotate()));
            af.scale(node.getScaleX(), node.getScaleY());
            af.translate(-node.getAnchorX(), -node.getAnchorY());
            Point2D pt2 = af.transform(pt, null);
            return new Point2D.Double(pt2.getX(),pt2.getY());
        }
        return new Point2D.Double(pt.getX(),pt.getY());
    }

    private Point2D.Double toolToModel(Point2D pt) {
        if(node != null) {
            try {
                AffineTransform af = new AffineTransform();
                af.translate(node.getTranslateX(),node.getTranslateY());
                af.translate(node.getAnchorX(),node.getAnchorY());
                af.rotate(Math.toRadians(node.getRotate()));
                af.scale(node.getScaleX(), node.getScaleY());
                af.translate(-node.getAnchorX(), -node.getAnchorY());
                Point2D pt2 = af.inverseTransform(pt, null);
                return new Point2D.Double(pt2.getX(),pt2.getY());
            } catch (NoninvertibleTransformException e) {
                e.printStackTrace();
            }
        }
        return new Point2D.Double(pt.getX(),pt.getY());
    }

    public void startEditing(SPath path) {
        clear();
        this.node = path;
        editingExisting = true;
        context.redraw();
    }
}
