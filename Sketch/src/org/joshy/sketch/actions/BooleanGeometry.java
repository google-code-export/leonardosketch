package org.joshy.sketch.actions;

import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Area;

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

    /**
     * Subtract the upper selected nodes from the bottom most selected node
     */
    public static class Subtract extends SAction {
        private VectorDocContext context;

        public Subtract(VectorDocContext context) {
            this.context = context;
        }

        @Override
        public void execute() {
            if(context.getSelection().size() > 1) {
                Area area = new Area();
                int count = 0;
                for(SNode node : context.getSelection().sortedItems(context.getDocument())) {
                    context.getDocument().getCurrentPage().remove(node);
                    if(count == 0) {
                        area.add(toArea(node));
                    } else {
                        area.subtract(toArea(node));
                    }
                    count++;
                }
                context.getSelection().clear();
                context.getDocument().getCurrentPage().add(new SArea(area));
                context.getCanvas().redraw();
            }
        }
    }

    public static class Union extends SAction {
        
        private VectorDocContext context;

        public Union(VectorDocContext context) {
            this.context = context;
        }

        @Override
        public void execute() {
            if(context.getSelection().size() > 1) {
                Area area = new Area();
                for(SNode node : context.getSelection().items()) {
                    context.getDocument().getCurrentPage().remove(node);
                    area.add(toArea(node));
                }
                context.getSelection().clear();
                context.getDocument().getCurrentPage().add(new SArea(area));
                context.getCanvas().redraw();
            }
        }
    }


    public static class Intersection extends SAction {
        private VectorDocContext context;

        public Intersection(VectorDocContext context) {
            this.context = context;
        }

        @Override
        public void execute() {
            Area area = new Area();
            int count = 0;
            for(SNode node : context.getSelection().sortedItems(context.getDocument())) {
                context.getDocument().getCurrentPage().remove(node);
                if(count == 0) {
                    area.add(toArea(node));
                } else {
                    area.intersect(toArea(node));
                }
                count++;
            }
            context.getSelection().clear();
            context.getDocument().getCurrentPage().add(new SArea(area));
            context.getCanvas().redraw();
        }
    }
}
