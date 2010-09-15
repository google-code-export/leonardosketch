package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.event.Event;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.u;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a selection of nodes in a vector document.
 */
public class Selection {
    private Map<SNode,List<Handle>> selected;
    private VectorDocContext context;

    public Selection(VectorDocContext context) {
        this.context = context;
        selected = new HashMap<SNode,List<Handle>>();
    }

    public void setSelectedNode(SNode node) {
        selected.clear();
        selected.put(node, genHandles(node));
        fireEvents();
    }
    
    public void addSelectedNode(SNode node) {
        selected.put(node, genHandles(node));
        fireEvents();
    }

    private void fireEvents() {
        EventBus.getSystem().publish(new SelectionChangeEvent(this));
    }

    public void regenHandles(SNode node) {
        selected.put(node,genHandles(node));
    }
    //generate handles. for now only resizable shapes have handles
    private List<Handle> genHandles(SNode node) {
        ArrayList<Handle> hs = new ArrayList<Handle>();
        if(node instanceof SResizeableNode) {
            SResizeableNode rnode = (SResizeableNode) node;
            hs.add(new ResizeHandle(rnode, ResizeHandle.Position.TopLeft));
            hs.add(new ResizeHandle(rnode, ResizeHandle.Position.TopRight));
            hs.add(new ResizeHandle(rnode, ResizeHandle.Position.BottomLeft));
            hs.add(new ResizeHandle(rnode, ResizeHandle.Position.BottomRight));
            if(rnode instanceof SRect) {
                SRect rect = (SRect) rnode;
                if(rect.getFillPaint() instanceof GradientFill) {
                    hs.add(new GradientHandle(rect,GradientHandle.GradientPosition.Start));
                    hs.add(new GradientHandle(rect,GradientHandle.GradientPosition.End));
                }
            }
        }
        if(node instanceof SArrow) {
            u.p("created handles for: "+ node);
            SArrow arrow = (SArrow) node;
            hs.add(new ArrowHandle(arrow,ArrowHandle.Position.Start));
            hs.add(new ArrowHandle(arrow,ArrowHandle.Position.End));
        }
        return hs;
    }

    public boolean isEmpty() {
        return selected.isEmpty();
    }

    public Iterable<? extends SNode> items() {
        return selected.keySet();
    }

    public void clear() {
        selected.clear();
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
        double w = Double.MIN_VALUE;
        double h = Double.MIN_VALUE;
        for(SNode n : items()) {
            x = Math.min(x, n.getBounds().getX());
            y = Math.min(y, n.getBounds().getY());
            w = Math.max(w, n.getBounds().getX() + n.getBounds().getWidth());
            h = Math.max(h, n.getBounds().getY() + n.getBounds().getHeight());
        }
        return new Bounds(x,y,w-x,h-y);
    }

    public Iterable<? extends SNode> sortedItems(SketchDocument doc) {
        List<SNode> sorted = new ArrayList<SNode>();
        for(SNode node : doc.getCurrentPage().model) {
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
