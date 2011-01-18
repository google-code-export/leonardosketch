package org.joshy.sketch.pixel.model;

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
    BufferedImage buffer;

    public PixelSelection(int w, int h) {
        buffer = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
    }

    public void addRect(int x, int y, int w, int h) {
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
}
