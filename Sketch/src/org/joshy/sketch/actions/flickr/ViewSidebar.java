package org.joshy.sketch.actions.flickr;

import org.joshy.gfx.node.layout.TabPanel;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.modes.vector.VectorDocContext;

/**
 * Shows the sidebar
 */
public class ViewSidebar extends SAction {
    private VectorDocContext context;


    public ViewSidebar(VectorDocContext context) {
        super();
        this.context = context;
    }

    @Override
    public void execute() {
        TabPanel sideBar = context.getSidebar();
        if(sideBar != null) {
            sideBar.setSelected(context.symbolPanel);
        }
    }
}
