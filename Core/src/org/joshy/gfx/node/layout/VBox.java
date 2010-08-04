package org.joshy.gfx.node.layout;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Insets;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.util.u;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 23, 2010
 * Time: 8:18:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class VBox extends Panel {
    private Insets padding = new Insets(0,0,0,0);

    @Override
    public void doLayout() {
        double y = 0 + padding.getTop();
        double x = 0 + padding.getLeft();
        for(Node child : children()) {
            if(child instanceof Control) {
                Control control = (Control) child;
                control.doLayout();
            }
            Bounds bounds = child.getVisualBounds();
            child.setTranslateX(x);
            child.setTranslateY(y);
            y+=bounds.getHeight();
        }
    }

    public void setPadding(Insets padding) {
        this.padding = padding;
    }

}
