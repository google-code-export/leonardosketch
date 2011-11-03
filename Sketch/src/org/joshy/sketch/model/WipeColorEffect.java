package org.joshy.sketch.model;

import org.joshy.gfx.draw.Effect;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.ImageBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 11/3/11
 * Time: 9:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class WipeColorEffect extends Effect {
    private FlatColor color;

    public WipeColorEffect(FlatColor yellow) {
        this.color = yellow;
    }

    @Override
    public void apply(ImageBuffer imageBuffer) {
        int w = imageBuffer.buf.getWidth();
        int h = imageBuffer.buf.getHeight();
        for(int x=0; x<w; x++) {
            for(int y=0; y<h; y++) {
                int c = imageBuffer.buf.getRGB(x,y);
                int c2 = c & 0xFF000000; //pull out just the alpha
                int c3 = c2 | (color.getRGBA()&0x00FFFFFF);
                imageBuffer.buf.setRGB(x,y,c3);
            }
        }
    }
}
