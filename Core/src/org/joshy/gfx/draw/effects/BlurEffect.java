package org.joshy.gfx.draw.effects;

import org.joshy.gfx.draw.Effect;
import org.joshy.gfx.draw.ImageBuffer;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Aug 5, 2010
* Time: 11:41:55 PM
* To change this template use File | Settings | File Templates.
*/
public class BlurEffect extends Effect {
    float[] matrix = {
        0.111f, 0.111f, 0.111f,
        0.111f, 0.111f, 0.111f,
        0.111f, 0.111f, 0.111f,
    };
    public BlurEffect(double width, double height) {
    }

    @Override
    public void apply(ImageBuffer buf) {
        BufferedImageOp op = new ConvolveOp( new Kernel(3, 3, matrix) );
        BufferedImage blurredImage = op.filter(buf.buf,null);
        buf.buf = blurredImage;
        //org.joshy.gfx.draw.Image finalImage = org.joshy.gfx.draw.Image.create(blurredImage);
        //g.drawImage(finalImage,0,0);
    }
}
