package org.joshy.gfx.node.layout;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.control.Control;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: May 18, 2010
* Time: 7:32:15 PM
* To change this template use File | Settings | File Templates.
*/
public class StackPanel extends Panel {
    @Override
    public void doLayout() {
        for(Control c : controlChildren()) {
            c.doPrefLayout();
            c.doLayout();
            c.setWidth(getWidth());
            c.setHeight(getHeight());
        }
    }

    @Override
    protected void drawSelf(GFX g) {
        //no op
    }
}
