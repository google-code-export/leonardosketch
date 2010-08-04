package org.joshy.gfx.node.control.skin;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Skin;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 19, 2010
 * Time: 5:50:46 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class FillSkin extends Skin {
    public static final FillSkin SKELETON = new FillSkin(){
        @Override
        public void paint(GFX g, double x, double y, double width, double height) {
            g.setPaint(FlatColor.WHITE);
            g.fillRect(x,y,width,height);
            g.setPaint(FlatColor.BLACK);
            g.drawRect(x,y,width,height);
            g.drawLine(0,0,width,height);
            g.drawLine(width,0,0,height);
        }

        @Override
        public void paint(GFX g, double x, double y, double width, double height, int rot) {
            paint(g,x,y,width,height);
        }
    };

    public abstract void paint(GFX g, double x, double y, double width, double height);
    public abstract void paint(GFX g, double x, double y, double width, double height, int rot);

    protected FlatColor stringToColor(String srgb) {
        if(srgb.startsWith("#")) {
            srgb = srgb.substring(1);
        }
        int rgb = Integer.valueOf(srgb,16);
        return new FlatColor(rgb);
    }

    protected double stringToDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return fallback;
        }
    }

}
