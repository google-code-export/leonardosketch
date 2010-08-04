package org.joshy.gfx.node.control.skin;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.w3c.dom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 20, 2010
 * Time: 8:58:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class FlatColorSkin extends FillSkin {
    private FlatColor color;

    public FlatColorSkin(Element val) {
        super();
        color = stringToColor(val.getAttribute("rgb"));
    }

    @Override
    public void paint(GFX g, double x, double y, double width, double height) {
        g.setPaint(color);
        g.fillRect(x,y,width,height);
    }

    @Override
    public void paint(GFX g, double x, double y, double width, double height, int rot) {
        paint(g,x,y,width,height);        
    }

    public FlatColor getColor() {
        return color;
    }
}
