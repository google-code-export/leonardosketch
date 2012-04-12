package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.event.MouseEvent;
import org.joshy.sketch.pixel.model.PixelGraphics;
import org.joshy.sketch.pixel.model.PixelLayer;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 6/18/11
 * Time: 5:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class BrushTool extends PixelTool {
    public BrushTool(PixelDocContext pixelDocContext) {
        super(pixelDocContext);
    }

    @Override
    protected void mousePressed(MouseEvent event,  Point2D cursor) {
        PixelLayer layer = getContext().getDocument().getCurrentLayer();
        PixelGraphics g = layer.getGraphics();
        g.setFill(getContext().getDocument().getForegroundColor());
        g.fillPixel((int)cursor.getX(),(int)cursor.getY());
        getContext().getCanvas().redraw();
    }

    @Override
    protected void mouseDragged(MouseEvent event,  Point2D cursor) {
        PixelLayer layer = getContext().getDocument().getCurrentLayer();
        PixelGraphics g = layer.getGraphics();
        g.setFill(getContext().getDocument().getForegroundColor());
        g.fillPixel((int)cursor.getX(),(int)cursor.getY());
        getContext().getCanvas().redraw();
    }

    @Override
    protected void mouseReleased(MouseEvent event,  Point2D cursor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
