package org.joshy.sketch.actions;

import org.joshy.sketch.model.*;
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
            MaskedImage mask = new MaskedImage();
            mask.setImage(image);
            mask.setShape(shape);
            context.getDocument().getCurrentPage().remove(shape);
            context.getDocument().getCurrentPage().remove(image);
            context.getDocument().getCurrentPage().add(mask);
            context.getSelection().clear();
            context.getSelection().setSelectedNode(mask);
        }
    }

    public static class UnsetMaskAction extends SAction {
        private VectorDocContext context;

        public UnsetMaskAction(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public CharSequence getDisplayName() {
            return "Remove Mask";
        }

        @Override
        public void execute() throws Exception {
            MaskedImage img = (MaskedImage) context.getSelection().firstItem();
            SketchDocument.SketchPage p = context.getDocument().getCurrentPage();
            p.remove(img);
            p.add(img.getShape());
            p.add(img.getImage());
            context.getSelection().clear();
            context.getSelection().setSelectedNode(img.getImage());
            context.getSelection().setSelectedNode(img.getShape());
        }

    }
}
