package org.joshy.sketch.actions;

import org.joshy.gfx.util.u;
import org.joshy.sketch.model.SImage;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.modes.vector.VectorDocContext;

public class ImageActions {
    public static class SetMaskAction extends SAction {
        private VectorDocContext context;

        public SetMaskAction(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public CharSequence getDisplayName() {
            return "Apply Mask";
        }


        @Override
        public void execute() throws Exception {
            SImage image = null;
            SShape shape = null;
            for(SNode node : context.getSelection().items()) {
                if(node instanceof SImage) image = (SImage) node;
                if(node instanceof SShape) shape = (SShape) node;
            }
            image.setMask(shape);
            u.p("image =  " + image.getTranslateX());
            u.p("shape = " + shape.getTranslateX());
            context.getDocument().getCurrentPage().remove(shape);
            context.getSelection().clear();
            context.getSelection().setSelectedNode(image);
        }
    }
}
