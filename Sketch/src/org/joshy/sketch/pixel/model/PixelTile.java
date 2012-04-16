package org.joshy.sketch.pixel.model;

import org.joshy.gfx.draw.Image;

import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/17/11
 * Time: 5:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class PixelTile {
    private Image image;
    BufferedImage buffer;

    public PixelTile() {
        buffer = new BufferedImage(256,256, BufferedImage.TYPE_INT_ARGB);
        image = Image.create(buffer);
    }

    public Image getImage() {
        return image;
    }

    public BufferedImage getBuffer() {
        return buffer;
    }
}
