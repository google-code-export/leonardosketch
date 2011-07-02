package org.joshy.sketch.pixel.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.modes.pixel.PixelDocContext;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/17/11
 * Time: 5:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class PixelSelection {
    public PixelLayer layer;
    private boolean empty;

    public PixelSelection() {
        layer = new PixelLayer();
        empty = true;
    }

    public void addRect(int x, int y, int w, int h) {
        PixelGraphics g = layer.getGraphics();
        g.setFill(FlatColor.YELLOW);
        g.fillRect(x, y, w, h);
        empty = false;
    }

    public void clear() {
        layer.clearAll();
        empty = true;
    }

    public void add(Rectangle rectangle) {
        empty = false;
        PixelGraphics g = layer.getGraphics();
        g.setFill(FlatColor.BLUE);
        g.fillRect((int)rectangle.getX(),(int)rectangle.getY(),(int)rectangle.getWidth(),(int)rectangle.getHeight());
    }

    public boolean isEmpty() {
        return empty;
    }

    public static class FillWithColor extends SAction {
        private PixelDocContext context;

        public FillWithColor(PixelDocContext pixelDocContext) {
            super();
            context = pixelDocContext;
        }

        @Override
        public void execute() throws Exception {
            PixelGraphics g = context.getDocument().getCurrentLayer().getGraphics();
            FlatColor color = context.getDocument().getForegroundColor();
            g.setFill(color);
            g.fillSelection(context.getCanvas().getSelection());
            context.getCanvas().getSelection().clear();
            context.getCanvas().redraw();
        }
    }
}
