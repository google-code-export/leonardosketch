package org.joshy.sketch.pixel.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.modes.pixel.PixelDocContext;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/17/11
 * Time: 5:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class PixelSelection {
    public BufferedImage buffer;
    private boolean empty;

    public PixelSelection(int w, int h) {
        buffer = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        empty= true;
    }

    public void addRect(int x, int y, int w, int h) {
        empty = false;
        Graphics2D g = buffer.createGraphics();
        g.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());
        g.setPaint(new java.awt.Color(0,0,0,0));
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, buffer.getWidth(),buffer.getHeight());
        g.setComposite(AlphaComposite.Src);
        g.setPaint(java.awt.Color.WHITE);
        g.fillRect(x,y,w,h);
        g.dispose();
        /*
        try {
            ImageIO.write(buffer,"png",new File("testa.png"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        */
    }

    public void clear() {
        empty = true;
        Graphics2D g = buffer.createGraphics();
        g.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());
        g.setPaint(new java.awt.Color(0,0,0,0));
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, buffer.getWidth(),buffer.getHeight());
        g.dispose();
    }

    public void add(Rectangle rectangle) {
        empty = false;
        Graphics2D g = buffer.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setPaint(java.awt.Color.RED);
        g.fill(rectangle);
        g.dispose();
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
            g.setFill(FlatColor.PURPLE);
            g.fillSelection(context.getCanvas().getSelection());
            context.getCanvas().getSelection().clear();
            context.getCanvas().redraw();
        }
    }
}
