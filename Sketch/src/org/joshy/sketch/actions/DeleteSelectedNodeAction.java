package org.joshy.sketch.actions;

import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SketchDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Apr 6, 2010
 * Time: 7:50:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeleteSelectedNodeAction extends SAction {
    private VectorDocContext context;

    public DeleteSelectedNodeAction(VectorDocContext context) {
        this.context = context;
    }

    @Override
    public String getDisplayName() {
        return "Delete";
    }

    @Override
    public void execute() {
        final List<SNode> shp = new ArrayList<SNode>();
        final SketchDocument doc = (SketchDocument) context.getDocument();
        for(SNode node : context.getSelection().items()) {
            doc.getCurrentPage().remove(node);
            shp.add(node);
        }
        context.getSelection().clear();
        context.redraw();

        context.getUndoManager().pushAction(new UndoManager.UndoableAction(){
            public void executeUndo() {
                for(SNode shape : shp) {
                    doc.getCurrentPage().add(shape);
                }
                context.redraw();
            }
            public void executeRedo() {
                for(SNode shape : shp) {
                    doc.getCurrentPage().remove(shape);
                }
                context.redraw();
            }
            public String getName() {
                return "delete";
            }
        });

    }
}
