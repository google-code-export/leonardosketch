package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.draw.LinearGradientFill;
import org.joshy.gfx.draw.RadialGradientFill;
import org.joshy.gfx.event.Event;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.Container;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.util.*;

/**
 * Represents a selection of nodes in a vector document.
 */
public class Selection {
    private Map<SNode,List<Handle>> selected;
    private VectorDocContext context;
    private List<Control> handleControls = new ArrayList<Control>();


    public Selection(VectorDocContext context) {
        this.context = context;
        selected = new HashMap<SNode,List<Handle>>();
    }


    public void setSelectedNode(SNode node) {
        if(selected.containsKey(node)) {
            List<Handle> handles = selected.get(node);
            for(Handle h : handles) {
                h.detach();
            }
            handles.clear();
        }
        selected.clear();
        clearHandleControls();
        selected.put(node, genHandles(node));
        regenHandleControls(node);
        fireEvents();
    }

    public void setSelectedNodes(List<? extends SNode> nodes) {
        selected.clear();
        clearHandleControls();
        for(SNode node : nodes) {
            selected.put(node, genHandles(node));
            regenHandleControls(node);
        }
        fireEvents();
    }


    public void addSelectedNode(SNode node) {
        if(selected.containsKey(node)) {
            List<Handle> handles = selected.get(node);
            for(Handle h : handles) {
                h.detach();
            }
            handles.clear();
        }
        selected.put(node, genHandles(node));
        regenHandleControls(node);
        fireEvents();
    }


    private void fireEvents() {
        EventBus.getSystem().publish(new SelectionChangeEvent(this));
    }


    public void regenHandles(SNode node) {
        List<Handle> handles = selected.get(node);
        Container popupLayer = context.getSketchCanvas().getParent().getStage().getPopupLayer();
        for(Handle h : handles) {
            for(Control c : h.getControls()) {
                popupLayer.remove(c);
                handleControls.remove(c);
            }
        }
        if(selected.containsKey(node)) {
            List<Handle> handles2 = selected.get(node);
            for(Handle h : handles2) {
                h.detach();
            }
            handles2.clear();
        }
        selected.remove(node);
        selected.put(node,genHandles(node));
        regenHandleControls(node);
    }


    public void regenHandleControls(SNode node) {
        for(Handle h : selected.get(node)) {
            if(h.hasControls()) {
                Container popupLayer = context.getSketchCanvas().getParent().getStage().getPopupLayer();
                for(Control c : h.getControls()) {
                    if(!handleControls.contains(c)) {
                        handleControls.add(c);
                        popupLayer.add(c);
                    }
                }
            }
        }
    }


    private void clearHandleControls() {
        for(Control c : handleControls) {
            Container popupLayer = context.getSketchCanvas().getParent().getStage().getPopupLayer();
            popupLayer.remove(c);
        }
        handleControls.clear();
    }


    //generate handles. for now only resizable shapes have handles
    private List<Handle> genHandles(SNode node) {
        ArrayList<Handle> hs = new ArrayList<Handle>();
        if(node instanceof SShape) {
            SShape shape = (SShape) node;
            if(shape.getFillPaint() instanceof GradientFill) {
                hs.add(new GradientHandle(shape, GradientHandle.GradientPosition.Start, context));
                hs.add(new GradientHandle(shape, GradientHandle.GradientPosition.End, context));
            }
            /*
            if(shape.getFillPaint() instanceof PatternPaint) {
                PatternMoveHandle h1 = new PatternMoveHandle(shape,context);
                hs.add(h1);
                hs.add(new PatternResizeHandle(h1, shape, context));
            }
            */
            if(shape.getFillPaint() instanceof RadialGradientFill) {
                RadialGradientCenterHandle h1 = new RadialGradientCenterHandle(shape, context);
                hs.add(h1);
                hs.add(new RadialGradientRadiusHandle(h1,shape,context));
            }
            if(shape.getFillPaint() instanceof LinearGradientFill) {
                LinearGradientStartHandle h1 = new LinearGradientStartHandle(shape, context);
                hs.add(h1);
                hs.add(new LinearGradientEndHandle(h1,shape,context));
            }
        }
        if(node instanceof SResizeableNode) {
            SResizeableNode rnode = (SResizeableNode) node;
            if(rnode instanceof SRect) {
                SRect rect = (SRect) rnode;
                hs.add(new SRect.RoundRectMasterHandle(rect));
            }
            if(rnode instanceof SText) {
                hs.add(new TextResizeHandle((SText) rnode, ResizeHandle.Position.TopLeft));
                hs.add(new TextResizeHandle((SText) rnode, ResizeHandle.Position.TopRight));
                hs.add(new TextResizeHandle((SText) rnode, ResizeHandle.Position.BottomLeft));
                hs.add(new TextResizeHandle((SText) rnode, ResizeHandle.Position.BottomRight));
            } else {
                hs.add(new ResizeHandle(rnode, ResizeHandle.Position.TopLeft));
                hs.add(new ResizeHandle(rnode, ResizeHandle.Position.TopRight));
                hs.add(new ResizeHandle(rnode, ResizeHandle.Position.BottomLeft));
                hs.add(new ResizeHandle(rnode, ResizeHandle.Position.BottomRight));
            }
        }
        if(node instanceof SArrow) {
            SArrow arrow = (SArrow) node;
            hs.add(new ArrowHandle(arrow,ArrowHandle.Position.Start));
            hs.add(new ArrowHandle(arrow,ArrowHandle.Position.End));
        }
        if(node instanceof STransformNode) {
            STransformNode trans = (STransformNode) node;
            hs.add(new STransformNode.TransformRotateHandle(trans));
            hs.add(new STransformNode.TransformScaleHandle(trans));
        }
        return hs;
    }


    public boolean isEmpty() {
        return selected.isEmpty();
    }


    public Iterable<? extends SNode> items() {
        return Collections.unmodifiableSet(selected.keySet());
    }


    public void clear() {
        for(SNode n : selected.keySet()) {
            List<Handle> handles2 = selected.get(n);
            for(Handle h : handles2) {
                h.detach();
            }
            handles2.clear();
        }

        selected.clear();
        clearHandleControls();
        fireEvents();
    }


    public boolean contains(SNode node) {
        return selected.containsKey(node);
    }


    public Map<SNode,List<Handle>> getHandles() {
        return selected;
    }


    public int size() {
        return selected.size();
    }


    public Bounds calculateBounds() {
        double x = Double.MAX_VALUE;
        double y = Double.MAX_VALUE;
        double x2 = Double.NEGATIVE_INFINITY;
        double y2 = Double.NEGATIVE_INFINITY;
        for(SNode n : items()) {
            Bounds b = n.getBounds();
            if(n instanceof HasTransformedBounds) {
                b = ((HasTransformedBounds)n).getTransformedBounds();
            }
            x  = Math.min(x,  b.getX());
            y  = Math.min(y,  b.getY());
            x2 = Math.max(x2, b.getX2());
            y2 = Math.max(y2, b.getY2());
        }
        return new Bounds(x,y,x2-x,y2-y);
    }


    public Iterable<? extends SNode> sortedItems(SketchDocument doc) {
        List<SNode> sorted = new ArrayList<SNode>();
        for(SNode node : doc.getCurrentPage().getNodes()) {
            if(contains(node)) {
                sorted.add(node);
            }
        }
        return sorted;
    }


    public SNode firstItem() {
        return items().iterator().next();
    }


    public List<SNode> duplicate(SketchDocument doc) {
        final List<SNode> dupes = new ArrayList<SNode>();
        for(SNode node : sortedItems(doc)) {
            SNode dupe = node.duplicate(null);
            dupes.add(dupe);
        }
        return dupes;
    }


    public SketchDocument getDocument() {
        return this.context.getDocument();
    }


    public  static class SelectionChangeEvent extends Event {
        public static final EventType Changed = new EventType("Changed");
        private Selection selection;

        public SelectionChangeEvent(Selection selection) {
            super(SelectionChangeEvent.Changed);
            this.selection = selection;
        }

        public Selection getSelection() {
            return selection;
        }
    }
}
