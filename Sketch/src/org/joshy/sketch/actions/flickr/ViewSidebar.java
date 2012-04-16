package org.joshy.sketch.actions.flickr;

import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.node.layout.TabPanel;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.modes.vector.VectorDocContext;

import static org.joshy.gfx.util.localization.Localization.getString;

/**
 * Shows the sidebar
 */
public class ViewSidebar extends SAction {
    private VectorDocContext context;
    private String key;
    private Panel panel;


    public ViewSidebar(String s, VectorDocContext context, Panel flickrPanel) {
        super();
        this.key = s;
        this.panel = flickrPanel;
        this.context = context;
    }

    @Override
    public CharSequence getDisplayName() {
        return getString(key);
    }

    @Override
    public void execute() {
        TabPanel sideBar = context.getSidebar();
        if(sideBar != null) {
            sideBar.setSelected(panel);
        }
    }
}
