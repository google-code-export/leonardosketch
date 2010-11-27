package org.joshy.sketch.util;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.util.u;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Nov 27, 2010
 * Time: 3:12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DrawUtils {

    public static void drawStandardHandle(GFX g, double x, double y, FlatColor color) {
        double s = 0;
        //shadow
        s = 7;
        g.setPaint(new FlatColor(0x404040).deriveWithAlpha(0.5));
        g.fillOval(x-s,y-s+1,s*2,s*2);
        //border
        s = 7;
        g.setPaint(FlatColor.WHITE);
        g.fillOval(x-s,y-s,s*2,s*2);

        //center
        s = 5;
        //g.setPaint(new FlatColor(0xa00000));
        double hue = color.getHue();
        GradientFill fill = new GradientFill(
                FlatColor.hsb(hue,0.4,1.0)
                ,FlatColor.hsb(hue,1.0,1.0)
                ,90,true, 0,0, 0,s*2);
        g.setPaint(fill);
        g.translate(x-s,y-s);
        g.fillOval(0,0,s*2,s*2);
        g.translate(-x+s,-y+s);

    }
}
