package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.util.u;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/17/11
 * Time: 10:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelectionTool extends PixelTool {
    public SelectionTool(PixelDocContext pixelDocContext) {
        super(pixelDocContext);
    }

    @Override
    protected void mousePressed(MouseEvent event, int x, int y) {
        u.p("selection pressed");
    }
}
