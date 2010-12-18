package org.joshy.sketch.actions;

import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.Bounds;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SPath;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.tools.IncrementalRotateTool;
import org.joshy.sketch.tools.TransformTool;

import java.util.List;

public class PathActions {

    abstract static class PathModifyAction extends SAction {
        private VectorDocContext context;

        PathModifyAction(VectorDocContext context) {
            this.context = context;
            EventBus.getSystem().addListener(Selection.SelectionChangeEvent.Changed, new Callback<Selection.SelectionChangeEvent>() {
                public void call(Selection.SelectionChangeEvent selectionChangeEvent) throws Exception {
                    int size = selectionChangeEvent.getSelection().size();
                    if(size != 1) {
                        setEnabled(false);
                        return;
                    }
                    SNode node = selectionChangeEvent.getSelection().firstItem();
                    setEnabled(node instanceof SPath);
                }
            });

        }

        @Override
        public void execute() {
            if(context.getSelection().size() != 1) return;
            SNode node = context.getSelection().firstItem();
            if(!(node instanceof SPath)) return;
            SPath path = (SPath) node;
            Bounds bounds = path.getBounds();
            List<SPath.PathPoint> points = path.getPoints();
            for(SPath.PathPoint pt : points) {
                pt.x = pt.x -bounds.getX()+path.getTranslateX();
                pt.y = pt.y -bounds.getY()+path.getTranslateY();
                pt.cx1 = pt.cx1 -bounds.getX()+path.getTranslateX();
                pt.cy1 = pt.cy1 -bounds.getY()+path.getTranslateY();
                pt.cx2 = pt.cx2 -bounds.getX()+path.getTranslateX();
                pt.cy2 = pt.cy2 -bounds.getY()+path.getTranslateY();

                modifyPoint(bounds,path,pt);
                
                pt.x = pt.x + bounds.getX()-path.getTranslateX();
                pt.y = pt.y + bounds.getY()-path.getTranslateY();
                pt.cx1 = pt.cx1 + bounds.getX()-path.getTranslateX();
                pt.cy1 = pt.cy1 + bounds.getY()-path.getTranslateY();
                pt.cx2 = pt.cx2 + bounds.getX()-path.getTranslateX();
                pt.cy2 = pt.cy2 + bounds.getY()-path.getTranslateY();
            }
            path.setPoints(points);
            context.redraw();
        }
        protected abstract void modifyPoint(Bounds bounds, SPath path, SPath.PathPoint pt);
    }


    public static class Flip extends PathModifyAction {
        private boolean horizontal;
        public Flip(VectorDocContext context, boolean horizontal) {
            super(context);
            this.horizontal = horizontal;
        }

        private double flipH(Bounds bounds, SPath path, double x) {
            return -x + bounds.getWidth();
        }
        private double flipV(Bounds bounds, SPath path, double y) {
            return -y + bounds.getHeight();
        }

        @Override
        protected void modifyPoint(Bounds bounds, SPath path, SPath.PathPoint pt) {
            if(horizontal) {
                pt.x = flipH(bounds,path, pt.x);
                pt.cx1 = flipH(bounds,path, pt.cx1);
                pt.cx2 = flipH(bounds,path, pt.cx2);
            } else {
                pt.y = flipV(bounds,path, pt.y);
                pt.cy1 = flipV(bounds,path, pt.cy1);
                pt.cy2 = flipV(bounds,path, pt.cy2);
            }
        }
    }


    public static class RotateClockwise extends PathModifyAction {
        private double angle;

        public RotateClockwise(VectorDocContext context, double angle) {
            super(context);
            this.angle = angle;
        }

        @Override
        protected void modifyPoint(Bounds bounds, SPath path, SPath.PathPoint pt) {
            //double angle = Math.PI/2;
            double x = 0;
            double y = 0;

            x = pt.x*Math.cos(angle)-pt.y*Math.sin(angle);
            y = pt.x*Math.sin(angle)+pt.y*Math.cos(angle);
            pt.x = x;
            pt.y = y;

            x = pt.cx1*Math.cos(angle)-pt.cy1*Math.sin(angle);
            y = pt.cx1*Math.sin(angle)+pt.cy1*Math.cos(angle);
            pt.cx1 = x;
            pt.cy1 = y;

            x = pt.cx2*Math.cos(angle)-pt.cy2*Math.sin(angle);
            y = pt.cx2*Math.sin(angle)+pt.cy2*Math.cos(angle);
            pt.cx2 = x;
            pt.cy2 = y;
        }
    }

    public static class Rotate extends SAction {
        private VectorDocContext context;

        public Rotate(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            if(context.getSelection().size() != 1) return;
            context.setSelectedTool(new IncrementalRotateTool(context));
            context.redraw();
        }
    }

    public static class Scale extends SAction {
        private VectorDocContext context;

        public Scale(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            if(context.getSelection().size() != 1) return;
            context.setSelectedTool(new TransformTool(context));
            context.redraw();
        }
    }
}
