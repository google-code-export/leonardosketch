package org.joshy.gfx.node.control.skin;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.w3c.dom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 20, 2010
 * Time: 10:28:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShadowSkin extends TextSkin {
    private int xoff;
    private int yoff;
    private FlatColor text_color;
    private FlatColor shadow_color;

    public ShadowSkin(Element val) {
        super();
        xoff = Integer.parseInt(val.getAttribute("xoffset"));
        yoff = Integer.parseInt(val.getAttribute("yoffset"));
        text_color = new FlatColor(val.getAttribute("text-color"));
        shadow_color = new FlatColor(val.getAttribute("shadow-color"));
    }

    @Override
    public void drawText(GFX g, String text, Font font, double x, double y) {
        g.setPaint(shadow_color);
        g.drawText(text, font, x+xoff, y+yoff);
        g.setPaint(text_color);
        g.drawText(text, font, x, y);
    }
}
