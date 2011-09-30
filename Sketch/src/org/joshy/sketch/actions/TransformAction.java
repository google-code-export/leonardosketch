package org.joshy.sketch.actions;

import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.HasTransformedBounds;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.STransformNode;
import org.joshy.sketch.modes.vector.VectorDocContext;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 9/29/11
 * Time: 12:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransformAction extends SAction {
    private VectorDocContext context;

    public TransformAction(VectorDocContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws Exception {
        Selection sel = context.getSelection();
        if(sel.isEmpty()) return;
        if(sel.size() > 1) return;
        SNode node = sel.firstItem();
        if(!(node instanceof HasTransformedBounds)) return;

        context.getDocument().getCurrentPage().remove(node);
        STransformNode trans = new STransformNode(node,context);
        context.getDocument().getCurrentPage().add(trans);

        sel.setSelectedNode(trans);
        context.redraw();
    }
}
