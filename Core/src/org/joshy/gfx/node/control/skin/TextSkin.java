package org.joshy.gfx.node.control.skin;

import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Skin;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 20, 2010
 * Time: 10:36:51 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TextSkin extends Skin {
    public abstract void drawText(GFX g, String text, Font font, double x, double y);
}
