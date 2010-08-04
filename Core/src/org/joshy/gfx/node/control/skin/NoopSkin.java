package org.joshy.gfx.node.control.skin;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Skin;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Feb 4, 2010
 * Time: 1:18:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class NoopSkin extends FillSkin {

    @Override
    public void paint(GFX g, double x, double y, double width, double height) {
    }

    @Override
    public void paint(GFX g, double x, double y, double width, double height, int rot) {
    }
}
