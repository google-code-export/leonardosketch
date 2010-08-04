package org.joshy.gfx.node.control.skin;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.GridNine;
import org.joshy.gfx.draw.Transform;
import org.joshy.gfx.util.URLUtils;
import org.joshy.gfx.util.u;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 19, 2010
 * Time: 5:41:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class GridNineSkin extends FillSkin {
    private BufferedImage image;
    private int top;
    private int right;
    private int bottom;
    private int left;
    private GridNine grid;
    private boolean flipx = false;

    public GridNineSkin(Element val) throws IOException, URISyntaxException {
        URI resolved = URLUtils.calculateURL(val,val.getAttribute("src"));

        image = ImageIO.read(resolved.toURL());
        top = Integer.parseInt(val.getAttribute("top"));
        bottom = Integer.parseInt(val.getAttribute("bottom"));
        left = Integer.parseInt(val.getAttribute("left"));
        right = Integer.parseInt(val.getAttribute("right"));
        if(val.hasAttribute("flipx")) flipx = Boolean.parseBoolean(val.getAttribute("flipx"));

        grid = GridNine.create(image, top, right, bottom, left, flipx);
    }

    @Override
    public void paint(GFX g, double x, double y, double width, double height) {
        g.drawGridNine(grid, x, y, width, height);
    }

    @Override
    public void paint(GFX g, double x, double y, double width, double height, int rot) {
        g.translate(x,y);
        if(rot == 1) {
            g.rotate(90, Transform.Z_AXIS);
            g.translate(0,-height);
        }
        g.drawGridNine(grid, 0, 0, width, height);
        if(rot == 1) {
            g.translate(0,height);
            g.rotate(-90, Transform.Z_AXIS);
        }
        g.translate(-x,-y);
    }
}