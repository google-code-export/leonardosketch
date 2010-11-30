package org.joshy.sketch.actions;

import org.joshy.sketch.model.SArea;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Oct 8, 2010
 * Time: 7:54:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class BooleanGeometry {

    private static Area toArea(SNode node) {
        if(node instanceof SShape) {
            return ((SShape)node).toArea();
        }
        return null;
    }

    public static class Union extends BooleanOpAction {
        public Union(VectorDocContext context) {
            super(context);
        }

        @Override
        public String getDisplayName() {
            return "Union shape";
        }

        @Override
        protected void applyToArea(Area area, SNode node, int count) {
            area.add(toArea(node));
        }
    }

    /**
     * Subtract the upper selected nodes from the bottom most selected node
     */
    public static class Subtract extends BooleanOpAction {
        public Subtract(VectorDocContext context) {
            super(context);
        }

        @Override
        public String getDisplayName() {
            return "Subtract shapes";
        }

        @Override
        protected void applyToArea(Area area, SNode node, int count) {
            if(count == 0) {
                area.add(toArea(node));
            } else {
                area.subtract(toArea(node));
            }
        }
    }

    public static class Intersection extends BooleanOpAction {
        public Intersection(VectorDocContext context) {
            super(context);
        }

        @Override
        public String getDisplayName() {
            return "Intersect shapes";
        }

        @Override
        protected void applyToArea(Area area, SNode node, int count) {
            if(count == 0) {
                area.add(toArea(node));
            } else {
                area.intersect(toArea(node));
            }
        }
    }

    public abstract static class BooleanOpAction extends SAction {
        
        private VectorDocContext context;

        public BooleanOpAction(VectorDocContext context) {
            this.context = context;
        }

        @Override
        public void execute() {
            if(context.getSelection().size() > 1) {
                final List<SNode> selection = new ArrayList<SNode>();
                final SketchDocument.SketchPage page = context.getDocument().getCurrentPage();
                for(SNode node : context.getSelection().sortedItems(context.getDocument())) {
                    selection.add(node);
                }

                Area area = new Area();
                int count = 0;
                for(SNode node : selection) {
                    page.remove(node);
                    applyToArea(area, node, count);
                    count++;
                }
                final SArea sarea = new SArea(area);
                page.add(sarea);

                context.getSelection().setSelectedNode(sarea);
                context.getCanvas().redraw();

                context.getUndoManager().pushAction(new UndoManager.UndoableAction(){
                    public void executeUndo() {
                        page.remove(sarea);
                        context.getSelection().clear();
                        for(SNode node : selection) {
                            page.add(node);
                            context.getSelection().addSelectedNode(node);
                        }
                    }

                    public void executeRedo() {
                        context.getSelection().clear();
                        for(SNode node : selection) {
                            page.remove(node);
                        }
                        page.add(sarea);
                        context.getSelection().setSelectedNode(sarea);
                    }

                    public CharSequence getName() {
                        return BooleanOpAction.this.getDisplayName();
                    }
                });
            }
        }

        protected abstract void applyToArea(Area area, SNode node, int count);
    }


}
