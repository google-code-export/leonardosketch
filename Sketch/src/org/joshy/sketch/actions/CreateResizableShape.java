package org.joshy.sketch.actions;

import org.joshy.gfx.node.Bounds;
import org.joshy.sketch.model.ResizableGrid9Shape;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 18, 2010
 * Time: 11:30:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateResizableShape extends SAction {
    private VectorDocContext context;

    public CreateResizableShape(VectorDocContext context) {
        this.context = context;
    }

    @Override
    public String getDisplayName() {
        return "Create Resizable Shape";
    }

    @Override
    public void execute() {
        if(context.getSelection().isEmpty()) return;
        Bounds sbounds = context.getSelection().calculateBounds();
        ResizableGrid9Shape shape = new ResizableGrid9Shape(0,0,sbounds.getWidth(),sbounds.getHeight());
        shape.setTranslateX(sbounds.getX());
        shape.setTranslateY(sbounds.getY());

        SketchDocument doc = context.getDocument();
        for(SNode node : context.getSelection().sortedItems(doc)) {
            shape.add(node);
            doc.getCurrentPage().remove(node);
        }
        doc.getCurrentPage().add(shape);
        context.getSelection().setSelectedNode(shape);
        context.selectButtonForTool(context.getEditResizableShapeTool());
        context.redraw();
    }

    public static class Edit extends SAction {
        private VectorDocContext context;

        public Edit(VectorDocContext context) {
            this.context = context;
        }

        @Override
        public String getDisplayName() {
            return "Edit Resizable Shape";
        }

        @Override
        public void execute() {
            if(context.getSelection().size() != 1) return;
            SNode shape = context.getSelection().items().iterator().next();
            if(shape instanceof ResizableGrid9Shape) {
                context.setSelectedTool(context.getEditResizableShapeTool());
                context.redraw();
            }
        }
    }
}
