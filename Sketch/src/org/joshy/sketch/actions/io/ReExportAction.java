package org.joshy.sketch.actions.io;

import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.modes.DocContext;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 10/7/11
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReExportAction extends SAction {
    private DocContext context;

    public ReExportAction(DocContext context) {
        super();
        this.context = context;
    }

    @Override
    public void execute() throws Exception {
        u.p("re-exporting");
        if(context.getLastExportAction() != null) {
            u.p("re doing the last action");
            context.getLastExportAction().exportHeadless();
        }
    }
}
