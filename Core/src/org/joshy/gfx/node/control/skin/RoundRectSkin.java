package org.joshy.gfx.node.control.skin;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.w3c.dom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 23, 2010
 * Time: 9:11:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class RoundRectSkin extends FillSkin {
    private FlatColor fill;
    private FlatColor border;
    private double arcWidth;
    private double arcHeight;

    public RoundRectSkin(Element element) {
        super();
        fill = stringToColor(element.getAttribute("fill"));
        arcWidth = stringToDouble(element.getAttribute("arcWidth"),10);
        arcHeight = stringToDouble(element.getAttribute("arcWidth"),10);
        border = stringToColor(element.getAttribute("border"));
    }

    @Override
    public void paint(GFX g, double x, double y, double width, double height) {
        g.setPaint(fill);
        g.fillRoundRect(x,y,width,height,arcWidth,arcHeight);
        g.setPaint(border);
        g.drawRoundRect(x,y,width,height,arcWidth,arcHeight);
    }

    @Override
    public void paint(GFX g, double x, double y, double width, double height, int rot) {
        paint(g,x,y,width,height);
    }
}
