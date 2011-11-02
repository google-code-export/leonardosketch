package org.joshy.sketch.actions;

import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.util.List;

/**
 *
 * A set of basic clipboard actions for nodes
 */
public class Clipboard {
    private static List<SNode> clipboardNodes;

    public static class CutAction extends SAction {
        private DocContext context;

        public CutAction(DocContext context) {
            super();
            this.context = context;

        }

        @Override
        public void execute() {
            if(context instanceof VectorDocContext) {
                VectorDocContext vc = (VectorDocContext) context;
                clipboardNodes = vc.getSelection().duplicate((SketchDocument) context.getDocument());
                vc.deleteSelectedNodeAction.execute();
            }
        }
    }

    public static class CopyAction extends SAction {
        private DocContext context;

        public CopyAction(DocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            if(context instanceof VectorDocContext) {
                VectorDocContext vc = (VectorDocContext) context;
                clipboardNodes = vc.getSelection().duplicate((SketchDocument) context.getDocument());
            }
        }
    }

    public static class PasteAction extends SAction {
        private DocContext context;

        public PasteAction(DocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            if(context instanceof VectorDocContext) {
                VectorDocContext vc = (VectorDocContext) context;
                vc.getSelection().clear();
                SketchDocument doc = (SketchDocument) context.getDocument();
                for(SNode node : clipboardNodes) {
                    SNode dupe = node.duplicate(null);
                    doc.getCurrentPage().add(dupe);
                    doc.setDirty(true);
                    vc.getSelection().addSelectedNode(dupe);
                }
            }
        }
    }
}
