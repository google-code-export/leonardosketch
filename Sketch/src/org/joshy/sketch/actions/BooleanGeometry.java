package org.joshy.sketch.actions;

import org.joshy.sketch.model.SArea;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SOval;
import org.joshy.sketch.model.SRect;
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

    public static class Union extends SAction {
        
        private VectorDocContext context;

        public Union(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            if(context.getSelection().size() > 1) {
                Area area = new Area();
                for(SNode node : context.getSelection().items()) {
                    context.getDocument().getCurrentPage().remove(node);
                    if(node instanceof SRect) {
                        SRect r = (SRect) node;
                        area.add(new Area(new java.awt.Rectangle.Double(
                                r.getX()+node.getTranslateX(),
                                r.getY()+node.getTranslateY(),
                                r.getWidth(),r.getHeight()
                        )));
                    }
                    if(node instanceof SOval) {
                        SOval o = (SOval) node;
                        area.add(new Area(new java.awt.geom.Ellipse2D.Double(
                                o.getX()+node.getTranslateX(),
                                o.getY()+node.getTranslateY(),
                                o.getWidth(),
                                o.getHeight()
                        )));
                    }
                }
                context.getSelection().clear();
                context.getDocument().getCurrentPage().add(new SArea(area));
                context.getCanvas().redraw();
            }
        }
    }


}
