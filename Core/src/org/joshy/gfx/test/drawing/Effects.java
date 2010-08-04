package org.joshy.gfx.test.drawing;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Aug 3, 2010
 * Time: 1:14:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class Effects implements Runnable {
    public static void main(String ... args) throws InvocationTargetException, InterruptedException {
        Core.init();
        Core.getShared().defer(new Effects());
    }

    public void run() {

        Node node = new Node() {

            @Override
            public void draw(GFX g) {
                g.setPaint(FlatColor.RED);
                g.drawLine(0,0,100,100);

                float[] matrix = {
                    0.111f, 0.111f, 0.111f,
                    0.111f, 0.111f, 0.111f,
                    0.111f, 0.111f, 0.111f,
                };


                BufferedImage sourceImage = new BufferedImage(50,50,BufferedImage.TYPE_INT_ARGB);
                Graphics2D gfx = sourceImage.createGraphics();
                gfx.setPaint(java.awt.Color.BLACK);
                gfx.drawString("ABCabc",20,20);
                gfx.drawLine(0,0,50,50);
                gfx.dispose();

                BufferedImageOp op = new ConvolveOp( new Kernel(3, 3, matrix) );
                BufferedImage blurredImage = op.filter(sourceImage,null);
                Image finalImage = Image.create(blurredImage);
                g.drawImage(finalImage,0,50);        
            }

            @Override
            public Bounds getVisualBounds() {
                return new Bounds(0,0,100,100);
            }

            @Override
            public Bounds getInputBounds() {
                return getVisualBounds();
            }
        };

        Stage stage = Stage.createStage();
        stage.setContent(node);
    }
}
