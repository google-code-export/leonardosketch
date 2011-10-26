package org.joshy.sketch.actions;

import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.model.SketchDocument;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: May 17, 2010
* Time: 6:09:00 PM
* To change this template use File | Settings | File Templates.
*/
public class UndoableAddNodeAction implements UndoManager.UndoableAction {
    private SShape node;
    private String name;
    private VectorDocContext context;

    public UndoableAddNodeAction(VectorDocContext context, SShape node, String name) {
        this.context = context;
        this.node = node;
        this.name = name;
    }

    /*public UndoableAddNodeAction(SketchCanvas canvas, SShape node) {
        this(canvas,node,"node");
    }*/

    public void executeUndo() {
        SketchDocument doc = (SketchDocument) context.getDocument();
        doc.getCurrentPage().remove(node);
        context.getSelection().clear();
    }

    public void executeRedo() {
        SketchDocument doc = (SketchDocument) context.getDocument();
        doc.getCurrentPage().add(node);
    }
    
    public String getName() {
        return "create " + name;
    }
    
}
