package org.joshy.sketch.model;

import org.joshy.gfx.draw.*;

import javax.imageio.ImageIO;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Apr 7, 2010
 * Time: 7:51:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class Button9 extends AbstractResizeableNode {
    private GridNine g9;

    public Button9(double x, double y, double w, double h) {
        super(x, y, w, h);
        try {
            BufferedImage img = ImageIO.read(this.getClass().getResource("resources/button1.png"));
            g9 = GridNine.create(img,10,10,10,10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(GFX g) {
        g.setPaint(FlatColor.BLACK);
        g.drawGridNine(g9, getX(), getY(), getWidth(), getHeight());
        Font.drawCentered(g,"Button",Font.DEFAULT, getX(), getY(), getWidth(), getHeight(),true);
    }

    @Override
    public Area toArea() {
        return new Area();
    }
}
