package org.joshy.sketch.actions;

import org.joshy.gfx.util.u;
import org.joshy.sketch.modes.DocContext;

public class CloseAction extends SAction {
    private DocContext context;

    public CloseAction(DocContext context) {
        super();
        this.context = context;
    }

    @Override
    public void execute() {
        context.getDocument().close();
    }
}
