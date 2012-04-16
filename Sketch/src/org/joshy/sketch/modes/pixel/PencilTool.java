package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.event.MouseEvent;
import org.joshy.sketch.pixel.model.PixelGraphics;
import org.joshy.sketch.pixel.model.PixelLayer;

import java.awt.geom.Point2D;

/**
 * The basic pencil tool. For now it just fills in pixels one at a time
 * using the color black.
 */
public class PencilTool extends PixelTool {
    int radius = 9;

    public PencilTool(PixelDocContext context) {
        super(context);
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D cursor) {
        PixelLayer layer = getContext().getDocument().getCurrentLayer();
        PixelGraphics g = layer.getGraphics();
        g.setFill(getContext().getDocument().getForegroundColor());
        g.fillOval((int)cursor.getX()-radius,(int)cursor.getY()-radius,radius*2,radius*2);
        getContext().getCanvas().redraw();
    }

    @Override
    protected void mouseDragged(MouseEvent event,  Point2D cursor) {
        PixelLayer layer = getContext().getDocument().getCurrentLayer();
        PixelGraphics g = layer.getGraphics();
        g.setFill(getContext().getDocument().getForegroundColor());
        g.fillOval((int)cursor.getX()-radius,(int)cursor.getY()-radius,radius*2,radius*2);
        getContext().getCanvas().redraw();
    }

    @Override
    protected void mouseReleased(MouseEvent event,  Point2D cursor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
