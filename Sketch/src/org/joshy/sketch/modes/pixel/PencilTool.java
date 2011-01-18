package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.util.u;
import org.joshy.sketch.pixel.model.PixelGraphics;
import org.joshy.sketch.pixel.model.PixelLayer;

/**
 * The basic pencil tool. For now it just fills in pixels one at a time
 * using the color black.
 */
public class PencilTool extends PixelTool {

    public PencilTool(PixelDocContext context) {
        super(context);
    }

    @Override
    protected void mousePressed(MouseEvent event, int x, int y) {
        u.p("mouse pressed at: " + event);

        PixelLayer layer = getContext().getDocument().getCurrentLayer();
        PixelGraphics g = layer.getGraphics();
        g.setFill(FlatColor.PURPLE);
        g.fillRect((int)event.getX(),(int)event.getY(),5,5);
        getContext().getCanvas().redraw();
    }

}
