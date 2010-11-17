package org.joshy.sketch.actions;

import org.joshy.sketch.model.SGroup;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SResizeableNode;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.util.ArrayList;
import java.util.List;

import static org.joshy.gfx.util.localization.Localization.getString;

public class NodeActions {


    public static class RaiseTopSelectedNodeAction extends SAction {
        private VectorDocContext context;

        public RaiseTopSelectedNodeAction(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public String getDisplayName() {
            return getString("menus.raiseNodeTop");
        }

        @Override
        public void execute() {
            SketchDocument doc = (SketchDocument) context.getDocument();
            List<SNode> model = doc.getCurrentPage().model;
            List<SNode> nodes = new ArrayList<SNode>();
            for(SNode node : context.getSelection().items()) {
                nodes.add(node);
            }
            model.removeAll(nodes);
            model.addAll(nodes);
            context.redraw();
        }
    }

    public static class RaiseSelectedNodeAction extends SAction {
        private VectorDocContext context;

        public RaiseSelectedNodeAction(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public String getDisplayName() {
            return getString("menus.raiseNode");
        }

        @Override
        public void execute() {
            if(context.getSelection().isEmpty()) return;
            SketchDocument doc = (SketchDocument) context.getDocument();

            List<SNode> nodes = new ArrayList<SNode>();
            for(SNode node : context.getSelection().items()) {
                nodes.add(node);
            }
            int max = -1;
            for(SNode node : nodes) {
                max = Math.max(max,doc.getCurrentPage().model.indexOf(node));
            }
            //if there is room to move up
            List<SNode> model = doc.getCurrentPage().model;
            if(max+1 < model.size()) {
                SNode nextNode = model.get(max+1);
                model.removeAll(nodes);
                int n = model.indexOf(nextNode);
                model.addAll(n+1,nodes);
            } else {
                //just remove and move all to the top
                model.removeAll(nodes);
                model.addAll(nodes);
            }
            context.redraw();
        }
    }

    public static class LowerSelectedNodeAction extends SAction {
        private VectorDocContext context;

        public LowerSelectedNodeAction(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public String getDisplayName() {
            return getString("menus.lowerNode");
        }

        @Override
        public void execute() {
            if(context.getSelection().isEmpty()) return;
            SketchDocument doc = (SketchDocument) context.getDocument();
            List<SNode> model = doc.getCurrentPage().model;
            List<SNode> nodes = new ArrayList<SNode>();
            for(SNode node : context.getSelection().items()) {
                nodes.add(node);
            }
            int min = Integer.MAX_VALUE;
            for(SNode node : nodes) {
                min = Math.min(model.indexOf(node),min);
            }
            //if there is room to move down
            if(min > 0) {
                SNode prevNode = model.get(min-1);
                model.removeAll(nodes);
                model.addAll(model.indexOf(prevNode),nodes);
            } else {
                //just remove and move all to the bottom
                model.removeAll(nodes);
                model.addAll(0,nodes);
            }
            context.redraw();
        }
    }

    public static class LowerBottomSelectedNodeAction extends SAction {
        private VectorDocContext context;

        public LowerBottomSelectedNodeAction(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public String getDisplayName() {
            return getString("menus.lowerNodeBottom");
        }

        @Override
        public void execute() {
            if(context.getSelection().isEmpty()) return;
            SketchDocument doc = (SketchDocument) context.getDocument();
            List<SNode> model = doc.getCurrentPage().model;
            List<SNode> nodes = new ArrayList<SNode>();
            for(SNode node : context.getSelection().items()) {
                nodes.add(node);
            }
            //just remove and move all to the bottom
            model.removeAll(nodes);
            model.addAll(0,nodes);
            context.redraw();
        }
    }

    public abstract static class NodeAction extends SAction {
        protected VectorDocContext context;

        protected NodeAction(VectorDocContext context) {
            this.context = context;
        }
    }
    public static class AlignTop extends NodeAction {
        public AlignTop(VectorDocContext context) {
            super(context);
        }

        @Override
        public String getDisplayName() {
            return getString("menus.alignNodeTop");
        }

        @Override
        public void execute() {
            double top = Integer.MAX_VALUE;
            for(SNode node: context.getSelection().items()) {
                top = Math.min(top,node.getBounds().getY());
            }
            for(SNode node: context.getSelection().items()) {
                double y = 0;
                if(node instanceof SResizeableNode) {
                    y = ((SResizeableNode)node).getY();
                }
                node.setTranslateY(top-y);
            }
            context.redraw();
        }
    }

    public static class AlignBottom extends NodeAction {
        public AlignBottom(VectorDocContext context) {
            super(context);
        }

        @Override
        public String getDisplayName() {
            return getString("menus.alignNodeBottom");
        }

        @Override
        public void execute() {
            double bottom = Integer.MIN_VALUE;
            for(SNode node: context.getSelection().items()) {
                bottom = Math.max(bottom, node.getBounds().getY() + node.getBounds().getHeight());
            }
            for(SNode node: context.getSelection().items()) {
                double y = 0;
                if(node instanceof SResizeableNode) {
                    y = ((SResizeableNode)node).getY();
                }
                node.setTranslateY(bottom - node.getBounds().getHeight() - y);
            }
            context.redraw();
        }
    }

    public static class AlignLeft extends NodeAction {
        public AlignLeft(VectorDocContext context) {
            super(context);
        }

        @Override
        public String getDisplayName() {
            return getString("menus.alignNodeLeft");
        }

        @Override
        public void execute() {
            double left = Integer.MAX_VALUE;
            for(SNode node: context.getSelection().items()) {
                left = Math.min(left,node.getBounds().getX());
            }
            for(SNode node: context.getSelection().items()) {
                double x = 0;
                if(node instanceof SResizeableNode) {
                    x = ((SResizeableNode)node).getX();
                }
                node.setTranslateX(left - x);
            }
            context.redraw();
        }
    }

    public static class AlignRight extends NodeAction {
        public AlignRight(VectorDocContext context) {
            super(context);
        }

        @Override
        public String getDisplayName() {
            return getString("menus.alignNodeRight");
        }

        @Override
        public void execute() {
            double right = Integer.MIN_VALUE;
            for(SNode node: context.getSelection().items()) {
                right = Math.max(right,node.getBounds().getX()+node.getBounds().getWidth());
            }
            for(SNode node: context.getSelection().items()) {
                double x = 0;
                if(node instanceof SResizeableNode) {
                    x = ((SResizeableNode)node).getX();
                }
                node.setTranslateX(right-node.getBounds().getWidth()-x);
            }
            context.redraw();
        }
    }

    public static class AlignCenterV extends NodeAction {
        public AlignCenterV(VectorDocContext context) {
            super(context);
        }

        @Override
        public String getDisplayName() {
            return getString("menus.alignNodeCenterVertical");
        }

        @Override
        public void execute() {
            double top = Integer.MAX_VALUE;
            double height = Integer.MIN_VALUE;
            for(SNode node: context.getSelection().items()) {
                top = Math.min(top,node.getBounds().getY());
                height = Math.max(top,node.getBounds().getHeight());
            }
            for(SNode node: context.getSelection().items()) {
                double diff = height - node.getBounds().getHeight();
                double y = 0;
                if(node instanceof SResizeableNode) {
                    y = ((SResizeableNode)node).getY();
                }
                node.setTranslateY(top+diff/2-y);
            }
            context.redraw();
        }
    }

    public static class AlignCenterH extends NodeAction {
        public AlignCenterH(VectorDocContext context) {
            super(context);
        }

        @Override
        public String getDisplayName() {
            return getString("menus.alignNodeCenterHorizontal");
        }

        @Override
        public void execute() {
            double left = Integer.MAX_VALUE;
            double width = Integer.MIN_VALUE;
            for(SNode node: context.getSelection().items()) {
                left = Math.min(left,node.getBounds().getX());
                width = Math.max(left,node.getBounds().getWidth());
            }
            for(SNode node: context.getSelection().items()) {
                double diff = width - node.getBounds().getWidth();
                double x = 0;
                if(node instanceof SResizeableNode) {
                    x = ((SResizeableNode)node).getX();
                }
                node.setTranslateX(left+diff/2-x);
            }
            context.redraw();
        }
    }

    public static class GroupSelection extends SAction {
        private VectorDocContext context;

        public GroupSelection(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public String getDisplayName() {
            return getString("menus.groupSelection");
        }

        @Override
        public void execute() {
            if(context.getSelection().size() < 2) return;
            SketchDocument doc = (SketchDocument) context.getDocument();

            List<SNode> model = doc.getCurrentPage().model;

            List<SNode> nodes = new ArrayList<SNode>();
            for(SNode node : model) {
                if(context.getSelection().contains(node)) {
                    nodes.add(node);
                }
            }

            model.removeAll(nodes);
            SGroup group = new SGroup();
            group.addAll(nodes);
            model.add(group);
            context.getSelection().clear();
            context.getSelection().setSelectedNode(group);
            context.redraw();
        }
    }

    public static class UngroupSelection extends SAction {
        private VectorDocContext context;

        public UngroupSelection(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public String getDisplayName() {
            return getString("menus.ungroupSelection");
        }

        @Override
        public void execute() {
            if(context.getSelection().size() != 1) return;
            SNode n = context.getSelection().items().iterator().next();
            if(!(n instanceof SGroup)) return;
            SGroup group = (SGroup) n;

            SketchDocument doc = (SketchDocument) context.getDocument();
            List<SNode> model = doc.getCurrentPage().model;
            model.remove(group);
            model.addAll(group.getNodes());
            context.getSelection().clear();
            for(SNode node : group.getNodes()) {
                node.setTranslateX(node.getTranslateX()+group.getTranslateX());
                node.setTranslateY(node.getTranslateY()+group.getTranslateY());
                context.getSelection().addSelectedNode(node);
            }
            context.redraw();
        }
    }

    public static class SameWidth extends NodeAction {
        private boolean doWidth;

        public SameWidth(VectorDocContext context, boolean doWidth) {
            super(context);
            this.doWidth = doWidth;
        }

        @Override
        public String getDisplayName() {
            return getString("menus.matchNodeWidth");
        }

        @Override
        public void execute() {
            //must have at least one firstItem selected
            if(context.getSelection().size() < 1) return;

            //copy into typed list
            //only resize if *all* nodes are resizeable
            List<SResizeableNode> nodes = new ArrayList<SResizeableNode>();
            for(SNode node: context.getSelection().items()) {
                if(!(node instanceof SResizeableNode)) {
                    return;
                }
                nodes.add((SResizeableNode)node);
            }

            double width = nodes.get(0).getWidth();
            double height = nodes.get(0).getHeight();
            for(SResizeableNode n : nodes) {
                if(doWidth) {
                    n.setWidth(width);
                } else {
                    n.setHeight(height);
                }
            }
            context.redraw();
        }
    }

    public static class DuplicateNodesAction extends SAction {
        private boolean offset;
        private VectorDocContext context;

        public DuplicateNodesAction(VectorDocContext context, boolean offset) {
            this.context = context;
            this.offset = offset;
        }

        @Override
        public String getDisplayName() {
            return getString("menus.matchNodeHeight");
        }

        @Override
        public void execute() {
            //duplicate the selection
            final List<SNode> dupes = new ArrayList<SNode>();
            SketchDocument doc = (SketchDocument) context.getDocument();
            for(SNode node : context.getSelection().sortedItems(doc)) {
                SNode dupe = node.duplicate(null);
                if(offset) {
                    dupe.setTranslateX(dupe.getTranslateX()+100);
                    dupe.setTranslateY(dupe.getTranslateY()+100);
                }
                dupes.add(dupe);
            }

            //make it undoable
            context.getUndoManager().pushAction(new UndoManager.UndoableAction(){
                public void executeUndo() {
                    SketchDocument doc = (SketchDocument) context.getDocument();
                    for(SNode dupe : dupes) {
                        doc.getCurrentPage().model.remove(dupe);
                    }
                    context.getSelection().clear();
                }
                public void executeRedo() {
                    SketchDocument doc = (SketchDocument) context.getDocument();
                    for(SNode dupe : dupes) {
                        doc.getCurrentPage().model.add(dupe);
                        doc.setDirty(true);
                    }
                }
                public String getName() {
                    return "duplicate";
                }
            });

            //clear selection
            context.getSelection().clear();
            //add to the doc
            for(SNode dupe : dupes) {
                doc.getCurrentPage().model.add(dupe);
                doc.setDirty(true);
                context.getSelection().addSelectedNode(dupe);
            }

        }
    }

    public static class ClearSelection extends SAction {
        private VectorDocContext context;

        public ClearSelection(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            context.getSelection().clear();
        }
    }

    public static class ResetTransforms extends SAction {
        private VectorDocContext context;

        public ResetTransforms(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            for(SNode node : context.getSelection().items()) {
                node.setScaleX(1);
                node.setScaleY(1);
                node.setRotate(0);
            }
            context.redraw();
        }
    }
}
