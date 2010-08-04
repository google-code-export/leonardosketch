package org.joshy.sketch.tools;

import org.joshy.gfx.Core;
import org.joshy.gfx.animation.AnimationDriver;
import org.joshy.gfx.animation.KeyFrameAnimator;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.ListModel;
import org.joshy.gfx.node.control.SwatchColorPicker;
import org.joshy.sketch.actions.*;
import org.joshy.sketch.actions.symbols.CreateSymbol;
import org.joshy.sketch.canvas.ResizeHandle;
import org.joshy.sketch.controls.ContextMenu;
import org.joshy.sketch.controls.FloatingPropertiesPanel;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/*

gradient: two buttons for start and end, knob for angle. always fills the rect
checkbox for shadow, set angle, depth, and color
slider for rounding: 0 -> 50
stroke border already set. allow you to use a gradient for the border?
additional inner shadow / glow?

 */
public class SelectMoveTool extends CanvasTool {
    private double startX;
    private double startY;
    private Map<SNode,Point2D> starts = new HashMap<SNode, Point2D>();
    private Handle selectedHandle;
    private boolean showIndicator;
    private boolean pressed;
    private long lastClickTime = 0;
    private ArrayList<ActionItem> menuActions;
    private ListModel<String> menuModel;
    private Point2D dragRectStartPoint;
    private Point2D dragRectEndPoint;
    private List<SNode> tempSelection = new ArrayList<SNode>();
    private double ido;
    private boolean didDuplicate;
    private boolean moved;
    private Handle hoverHandle;
    private ContextMenu contextMenu;
    private Bounds resizeStartBounds;
    private SwatchColorPicker gradientButton;
    private Handle lastHandle;

    public static class ActionItem {
        public SAction action;
        public String label;

        public ActionItem(SAction action, String label) {
            this.action = action;
            this.label = label;
        }

        public String getLabel() {
            if(label != null) return label;
            return action.getDisplayName();
        }
    }
    
    public SelectMoveTool(VectorDocContext context) {
        super(context);
        menuActions = new ArrayList<ActionItem>();
        menuActions.add(new ActionItem(new NodeActions.AlignLeft(context),"Left"));
        menuActions.add(new ActionItem(new NodeActions.GroupSelection(context),"Group"));
        menuActions.add(new ActionItem(new NodeActions.UngroupSelection(context),"Ungroup"));

        menuModel = new ListModel<String>() {
            public String get(int i) {
                return menuActions.get(i).label;
            }
            public int size() {
                return menuActions.size();
            }
        };

        gradientButton = new SwatchColorPicker();
        EventBus.getSystem().addListener(gradientButton, ChangedEvent.ColorChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                if(lastHandle instanceof GradientHandle) {
                    ((GradientHandle)lastHandle).setColor((FlatColor)event.getValue());
                }
            }
        });
        context.getStage().getPopupLayer().add(gradientButton);
        gradientButton.setVisible(false);

    }


    protected void call(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_BACKSPACE) {
            context.getDeleteSelectedNodeAction().execute();
            context.getPropPanel().setVisible(false);
        }
        double amount = 1.0;
        if(event.isShiftPressed()) {
            amount = 10.0;
        }
        switch(event.getKeyCode()) {
            case KEY_LEFT_ARROW:
                for(SNode node : context.getSelection().items()) {
                    node.setTranslateX(node.getTranslateX()-amount);
                }
                break;
            case KEY_RIGHT_ARROW:
                for(SNode node : context.getSelection().items()) {
                    node.setTranslateX(node.getTranslateX()+amount);
                }
                break;
            case KEY_UP_ARROW:
                for(SNode node : context.getSelection().items()) {
                    node.setTranslateY(node.getTranslateY()-amount);
                }
                break;
            case KEY_DOWN_ARROW:
                for(SNode node : context.getSelection().items()) {
                    node.setTranslateY(node.getTranslateY()+amount);
                }
                break;
            case KEY_ENTER:
                switchToEdit();
                break;
        }
        context.redraw();
    }


    @Override
    public void enable() {
        super.enable();
        context.getSketchCanvas().setShowSelection(true);
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
        Map<SNode,List<Handle>> handles = context.getSelection().getHandles();
        boolean setHover = false;
        for(SNode r : handles.keySet()) {
            for(Handle h : handles.get(r)) {
                if(h.contains(cursor)) {
                    hoverHandle = h;
                    setHover = true;
                    context.redraw();
                }
            }
        }
        if(!setHover && hoverHandle != null) {
            hoverHandle = null;
            context.redraw();
        }
    }

    private void switchToEdit() {
        if(context.getSelection().size() != 1) return;
        SNode node = context.getSelection().items().iterator().next();
        if(node instanceof SText) {
            context.switchToTextEdit(node);
            return;
        }
        if(node instanceof SPath) {
            context.getSelection().clear();
            context.switchToPathEdit((SPath) node);
            return;
        }
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        if(!(context.getDocument() != null)) return;

        //hide the context menu if showing
        ContextMenu.hideAll();

        SketchDocument doc = context.getDocument();
        if(event.getButton() == 3) { // check for right clicks to open the context menu
            showContextMenu(event);
            return;
        }
        pressed = true;
        Core.getShared().getFocusManager().setFocusedNode(context.getCanvas());
        //always hide the selector when doing manipulation
        fadeOut(context.getPropPanel());

        //check for double clicks
        if(new Date().getTime() - lastClickTime < 500 && context.getSelection().size() == 1) {
            SNode node = context.getSelection().items().iterator().next();
            if(node instanceof SText) {
                context.switchToTextEdit(node);
                return;
            }                                        
            if(node instanceof SPath) {
                context.getSelection().clear();
                context.switchToPathEdit((SPath) node);
                return;
            }
            if(node instanceof ResizableGrid9Shape) {
                //main.selectButtonForTool(main.editResizableShapeTool);
                ResizableGrid9Shape grid9 = (ResizableGrid9Shape) node;
                if(grid9.hasTextChild()) {
                    context.switchToTextEdit(grid9);
                }
            }
        }

        lastClickTime = new Date().getTime();

        //process selection handles under the cursor
        Map<SNode,List<Handle>> handles = context.getSelection().getHandles();
        for(SNode r : handles.keySet()) {
            for(Handle h : handles.get(r)) {
                if(h.contains(cursor)) {
                    SResizeableNode sn = (SResizeableNode) r;
                    resizeStartBounds = new Bounds(sn.getX(),sn.getY(),sn.getWidth(),sn.getHeight());
                    selectedHandle = h;
                    fadeInIndicator();
                    return;
                }
            }
        }

        //process nodes under the cursor
        List<SNode> underCursor = new ArrayList<SNode>();
        for(SNode node : doc.getCurrentPage().model) {
            if(node.contains(cursor)) {
                underCursor.add(node);
            }
        }

        //clear selection if nothing under the cursor
        if(underCursor.isEmpty()) {
            if(!event.isShiftPressed()) {
                context.getSelection().clear();
            }
            dragRectStartPoint = cursor;
            dragRectEndPoint = cursor;
            context.redraw();
        }

        //update selection if something under the cursor
        if(!underCursor.isEmpty()) {
            startX = cursor.getX();
            startY = cursor.getY();
            SNode selectedNode = underCursor.get(underCursor.size()-1);
            if(event.isShiftPressed()) {
                context.getSelection().addSelectedNode(selectedNode);
            } else {
                if(!context.getSelection().contains(selectedNode)) {
                    context.getSelection().setSelectedNode(selectedNode);
                }
            }

            //if alt key on one node, then do duplicate
            if(event.isAltPressed() && !context.getSelection().isEmpty()) {
                duplicateAndReplaceSelection();
            }

            //initialize the start points for everything in the selection
            for(SNode r : context.getSelection().items()) {
                starts.put(r,new Point2D.Double(r.getTranslateX(), r.getTranslateY()));
            }

            fadeInIndicator();
            context.redraw();
        }
    }

    private void showContextMenu(MouseEvent event) {
        contextMenu = new ContextMenu();
        contextMenu.addActions(
            new DeleteSelectedNodeAction(context),
            new NodeActions.RaiseTopSelectedNodeAction(context),
            new NodeActions.RaiseSelectedNodeAction(context),
            new NodeActions.LowerSelectedNodeAction(context),
            new NodeActions.LowerBottomSelectedNodeAction(context)
        );

        if(context.getSelection().size() > 1) {
            contextMenu.addActions(new NodeActions.AlignLeft(context),
                    new NodeActions.AlignRight(context),
                    new NodeActions.AlignTop(context),
                    new NodeActions.AlignBottom(context),
                    new NodeActions.GroupSelection(context));
        }

        contextMenu.addActions(new CreateSymbol(context));
        contextMenu.addActions(new CreateResizableShape(context));
        contextMenu.addActions(new CreateResizableShape.Edit(context));

        contextMenu.setWidth(100);
        contextMenu.setHeight(200);
        contextMenu.show(context.getCanvas(),event.getX(),event.getY());
    }

    private void duplicateAndReplaceSelection() {
        context.getDuplicateNodeAction().execute();
        didDuplicate = true;
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        if(!pressed) return;

        // process drag rect
        if(dragRectStartPoint != null) {
            dragRectEndPoint = cursor;
            Bounds dragRectBounds = new Bounds(dragRectStartPoint,dragRectEndPoint);
            tempSelection.clear();
            for(SNode node : context.getDocument().getCurrentPage().model) {
                if(dragRectBounds.intersects(node.getBounds())) {
                    if(!tempSelection.contains(node)) {
                        tempSelection.add(node);
                    }
                }
            }
            context.getDocument().setDirty(true);
            context.redraw();
            return;
        }

        // process handle if handle has been selected.
        if(selectedHandle != null) {
            double nx = cursor.getX();
            double ny = cursor.getY();
            SketchDocument doc = context.getDocument();
            if(doc.isSnapGrid()) {
                nx = ((int)(nx/doc.getGridWidth()))*doc.getGridWidth();
                ny = ((int)(ny/doc.getGridHeight()))*doc.getGridHeight();
            }
            selectedHandle.setX(nx,event.isShiftPressed());
            selectedHandle.setY(ny,event.isShiftPressed());
            context.getDocument().setDirty(true);
            context.redraw();
            return;
        }

        // do nothing if no selection
        if(context.getSelection().isEmpty()) return;

        // move selected nodes  
        moved = true;


        boolean hsnap = false;
        boolean vsnap = false;
        //snap with other nodes first
        if(context.getDocument().isSnapNodeBounds()) {
            for(SNode node : context.getDocument().getCurrentPage().getNodes()) {
                if(!context.getSelection().contains(node)) {
                    if(snapHorizontalBounds(cursor, node.getBounds())) {
                        hsnap = true;
                    }
                    if(snapVerticalBounds(cursor, node.getBounds())) {
                        vsnap = true;
                    }
                }
            }
        }

        //snap with doc bounds next if not already snapped
        if(context.getDocument().isSnapDocBounds()) {
            Bounds docBounds = new Bounds(0,0,context.getDocument().getWidth(),context.getDocument().getHeight());
            if(!hsnap) {
                hsnap = snapHorizontalBounds(cursor, docBounds);
            }
            if(!vsnap) {
                vsnap = snapVerticalBounds(cursor,docBounds);
            }
        }
        
        if(!hsnap) context.getCanvas().hideHSnap();
        if(!vsnap) context.getCanvas().hideVSnap();
        
        basicDragMove(cursor, event, hsnap, vsnap);
        context.getDocument().setDirty(true);
        context.redraw();
    }

    private void basicDragMove(Point2D.Double cursor, MouseEvent event, boolean hsnap, boolean vsnap) {
        for(SNode r : context.getSelection().items()) {
            //grab where the point started
            Point2D start = starts.get(r);
            //calc where the point would be without any snapping
            double nx = start.getX() + cursor.getX() - startX;
            double ny = start.getY() + cursor.getY() - startY;
            SketchDocument doc = context.getDocument();
            //calc snap points
            if(doc.isSnapGrid()) {
                nx = ((int)(nx/doc.getGridWidth()))*doc.getGridWidth();
                ny = ((int)(ny/doc.getGridHeight()))*doc.getGridHeight();
            }

            //constrain movement to horiz and vert from the original drag point
            if(didDuplicate && event.isShiftPressed()) {
                double dx = Math.abs(cursor.getX()-startX);
                double dy = Math.abs(cursor.getY()-startY);
                if(dx < dy) {
                    nx = start.getX();
                } else {
                    ny = start.getY();
                }
            }
            if(!hsnap) r.setTranslateX(nx);
            if(!vsnap) r.setTranslateY(ny);        
        }
    }

    private boolean snapHorizontalBounds(Point2D.Double cursor, Bounds doc) {
        Bounds selb = calculateUnSnappedSelectionBounds(cursor);
        double threshold = 15;
        //u.p("selb = " + selb);
        //snap to left side of doc bounds
        if(Math.abs(selb.getX()-doc.getX()) < threshold) {
            for(SNode n : context.getSelection().items()) {
                //bounds.x + offset within the bounds
                Point2D start = starts.get(n);
                //calc where the edge would be with no snapping
                double dx = start.getX() + cursor.getX()-startX;
                dx = dx-selb.getX();
                n.setTranslateX(doc.getX()+dx);
            }
            context.getCanvas().showHSnap(doc.getX());
            return true;
        }

        //snap to right side of doc bounds
        if(Math.abs(selb.getX()+selb.getWidth()-doc.getX2()) < threshold) {
            for(SNode n : context.getSelection().items()) {
                //bounds.x + offset within the bounds
                Point2D start = starts.get(n);
                //calc where the edge would be with no snapping
                double dx = start.getX() + cursor.getX()-startX;
                dx = dx-selb.getX(); //calc x within the bounds of the selection
                n.setTranslateX(doc.getX2()-selb.getWidth()+dx);
            }
            context.getCanvas().showHSnap(doc.getX2());
            return true;
        }

        //snap to center width of target bounds
        if(Math.abs(selb.getCenterX()-doc.getCenterX()) < threshold) {
            for(SNode n : context.getSelection().items()) {
                Point2D start = starts.get(n);
                //calc where the edge would be with no snapping
                double dx = start.getX() + cursor.getX()-startX;
                dx = dx-selb.getX(); // calc x within the bounds of the selection
                n.setTranslateX(doc.getCenterX()-selb.getWidth()/2+dx);
            }
            context.getCanvas().showHSnap(doc.getCenterX());
            return true;
        }
        return false;
    }


    private boolean snapVerticalBounds(Point2D.Double cursor, Bounds doc) {
        Bounds selb = calculateUnSnappedSelectionBounds(cursor);
        double threshold = 15;
        //u.p("selb = " + selb);
        //snap to top of doc bounds
        if(Math.abs(selb.getY()-doc.getY()) < threshold) {
            for(SNode n : context.getSelection().items()) {
                Point2D start = starts.get(n);
                //calc where the edge would be with no snapping
                double dy = start.getY() + cursor.getY()-startY;
                dy = dy-selb.getY();
                n.setTranslateY(doc.getY()+dy);
            }
            context.getCanvas().showVSnap(doc.getY());
            return true;
        }

        //snap to bottom of target bounds
        if(Math.abs(selb.getY()+selb.getHeight()-doc.getY2()) < threshold) {
            for(SNode n : context.getSelection().items()) {
                Point2D start = starts.get(n);
                //calc where the edge would be with no snapping
                double dy = start.getY() + cursor.getY()-startY;
                dy = dy-selb.getY(); // calc y within the bounds of the selection
                n.setTranslateY(doc.getY2()-selb.getHeight()+dy);
            }
            context.getCanvas().showVSnap(doc.getY2());
            return true;
        }

        //snap to center height of target bounds
        if(Math.abs(selb.getCenterY()-doc.getCenterY()) < threshold) {
            for(SNode n : context.getSelection().items()) {
                Point2D start = starts.get(n);
                //calc where the edge would be with no snapping
                double dy = start.getY() + cursor.getY()-startY;
                dy = dy-selb.getY(); // calc y within the bounds of the selection
                n.setTranslateY(doc.getCenterY()-selb.getHeight()/2+dy);
            }
            context.getCanvas().showVSnap(doc.getCenterY());
            return true;
        }

        return false;
    }

    private Bounds calculateUnSnappedSelectionBounds(Point2D.Double cursor) {

        double x = Double.MAX_VALUE;
        double y = Double.MAX_VALUE;
        double w = Double.MIN_VALUE;
        double h = Double.MIN_VALUE;
        for(SNode n : context.getSelection().items()) {
            Point2D start = starts.get(n);
            double ux = start.getX() + cursor.getX()-startX;
            double uy = start.getY() + cursor.getY()-startY;
            x = Math.min(x, ux);
            y = Math.min(y, uy);
            w = Math.max(w, ux + n.getBounds().getWidth());
            h = Math.max(h, uy + n.getBounds().getHeight());
        }
        return new Bounds(x,y,w-x,h-y);
    }

    //clear everything on mouse release
    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        context.getCanvas().hideHSnap();
        context.getCanvas().hideVSnap();
        if(moved) {
            final Map<SNode,Point2D> oldPoints = new HashMap<SNode,Point2D>();
            for(SNode r : context.getSelection().items()) {
                oldPoints.put(r,starts.get(r));
            }
            final Map<SNode,Point2D> newPoints = new HashMap<SNode,Point2D>();
            for(SNode r : context.getSelection().items()) {
                newPoints.put(r, new Point2D.Double(r.getTranslateX(),r.getTranslateY()));
            }

            context.getUndoManager().pushAction(new UndoManager.UndoableAction() {
                public void executeUndo() {
                    for(SNode s : oldPoints.keySet()) {
                        Point2D pt = oldPoints.get(s);
                        s.setTranslateX(pt.getX());
                        s.setTranslateY(pt.getY());
                    }
                    context.redraw();
                }

                public void executeRedo() {
                    for(SNode s : newPoints.keySet()) {
                        Point2D pt = newPoints.get(s);
                        s.setTranslateX(pt.getX());
                        s.setTranslateY(pt.getY());
                    }
                    context.redraw();
                }

                public String getName() {
                    return "move";
                }
            });
            moved = false;
        }

        

        for(SNode s : tempSelection) {
            if(!context.getSelection().contains(s)){
                context.getSelection().addSelectedNode(s);
            }
        }
        tempSelection.clear();
        dragRectStartPoint = null;
        dragRectEndPoint = null;
        pressed = false;
        if(selectedHandle instanceof ResizeHandle) {
            ResizeHandle rh = (ResizeHandle) selectedHandle;
            final Bounds startBounds = this.resizeStartBounds;
            final SResizeableNode sn = rh.getResizeableNode();
            final Bounds endBounds = new Bounds(sn.getX(), sn.getY(), sn.getWidth(), sn.getHeight());
            context.getUndoManager().pushAction(new UndoManager.UndoableAction(){
                public void executeUndo() {
                    sn.setX(startBounds.getX());
                    sn.setY(startBounds.getY());
                    sn.setWidth(startBounds.getWidth());
                    sn.setHeight(startBounds.getHeight());
                }
                public void executeRedo() {
                    sn.setX(endBounds.getX());
                    sn.setY(endBounds.getY());
                    sn.setWidth(endBounds.getWidth());
                    sn.setHeight(endBounds.getHeight());
                }
                public String getName() {
                    return "Resize node";
                }
            });
        }
        if(selectedHandle instanceof GradientHandle) {
            gradientButton.setTranslateX(selectedHandle.getX()+30);
            gradientButton.setTranslateY(selectedHandle.getY());
            gradientButton.setVisible(true);
        } else {
            gradientButton.setVisible(false);
        }
        lastHandle = selectedHandle;
        selectedHandle = null;
        fadeOutIndicator();
        context.redraw();
        starts.clear();
        if (!context.getSelection().isEmpty()) {
            fadeIn(context.getPropPanel());
        } else {
            fadeOut(context.getPropPanel());
        }
        didDuplicate = false;
    }

    private void fadeOut(FloatingPropertiesPanel propPanel) {
        context.getPropPanel().setVisible(false);
    }

    private void fadeIn(FloatingPropertiesPanel propPanel) {
        context.getPropPanel().setVisible(true);
    }


    private Font moveInfoFont = Font.name("Helvetica").size(11f).resolve();
    NumberFormat moveInfoFormatter = DecimalFormat.getIntegerInstance();


    private void fadeInIndicator() {
        showIndicator = true;
        KeyFrameAnimator kf = KeyFrameAnimator.create(this,"indicatorOpacity")
                .keyFrame(0,  0.0)
                .keyFrame(0.2,1.0);
        AnimationDriver.start(kf);
    }

    private void fadeOutIndicator() {
        KeyFrameAnimator kf = KeyFrameAnimator.create(this,"indicatorOpacity")
                .keyFrame(0,  1.0)
                .keyFrame(0.2,0.0)
                .doAfter(new Callback() {
                    public void call(Object event) {
                        showIndicator = false;
                        context.redraw();
                    }
                });
        AnimationDriver.start(kf);
    }

    public void setIndicatorOpacity(double ido) {
        this.ido = ido;
        context.redraw();
    }
    
    //draw the info on the current node we are moving
    public void drawOverlay(GFX g) {

        //draw the drag rectangle
        if(dragRectStartPoint != null) {
            double x1 = context.getSketchCanvas().transformToDrawing(dragRectStartPoint).getX();
            double x2 = context.getSketchCanvas().transformToDrawing(dragRectEndPoint).getX();
            if(x1 > x2) {
                double t = x1;
                x1 = x2;
                x2 = t;
            }
            double y1 = context.getSketchCanvas().transformToDrawing(dragRectStartPoint).getY();
            double y2 = context.getSketchCanvas().transformToDrawing(dragRectEndPoint).getY();
            if(y1 > y2) {
                double t = y1;
                y1 = y2;
                y2 = t;
            }

            g.setPaint(new FlatColor(0.1,0.2,1.0,0.1));
            g.fillRect(x1,y1,x2-x1,y2-y1);
            g.setPaint(new FlatColor(0.1,0.2,1.0,0.8));
            g.drawRect(x1,y1,x2-x1,y2-y1);
        }

        for(SNode shape : tempSelection) {
            if(shape instanceof SResizeableNode) {
                context.getSketchCanvas().drawSelection(g, shape);
            }
        }


        //draw a hover over the hovered handle
        if(hoverHandle != null) {
            g.setPaint(FlatColor.RED);
            Point2D.Double pt = context.getSketchCanvas().transformToDrawing(hoverHandle.getX(),hoverHandle.getY());
            double s = 6;
            g.setPaint(FlatColor.BLACK);
            g.fillOval(pt.x-s,pt.y-s,s*2,s*2);

            s = 5;
            g.setPaint(FlatColor.RED);
            g.fillOval(pt.x-s,pt.y-s,s*2,s*2);
        }
        

        //draw the move position indicator
        if(!showIndicator) return;
        Bounds sb = context.getSketchCanvas().selection.calculateBounds();
        String l1;
        String l2;
        if(selectedHandle != null) {
            l1 = "w: "+moveInfoFormatter.format(sb.getWidth());
            l2 = "h: "+moveInfoFormatter.format(sb.getHeight());
        } else {
            l1 = "x: "+moveInfoFormatter.format(sb.getX());
            l2 = "y: "+moveInfoFormatter.format(sb.getY());
        }
        sb = context.getSketchCanvas().transformToDrawing(sb);

        g.setPaint(FlatColor.hsb(0,0,0.6,ido));
        g.fillRoundRect(sb.getX()+sb.getWidth()+20,sb.getY()+sb.getHeight()/2-20,50,40,10,10);
        g.setPaint(new FlatColor(1.0,1.0,1.0,ido));
        g.drawText(l1,moveInfoFont,sb.getX()+sb.getWidth()+20+10,sb.getY()+sb.getHeight()/2-20+15);
        g.drawText(l2,moveInfoFont,sb.getX()+sb.getWidth()+20+10,sb.getY()+sb.getHeight()/2-20+15+15);
        g.setPaint(FlatColor.hsb(0,0,0.4,ido));
        g.drawRoundRect(sb.getX()+sb.getWidth()+20,sb.getY()+sb.getHeight()/2-20,50,40,10,10);
    }

}