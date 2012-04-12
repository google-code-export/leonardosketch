package org.joshy.sketch.util;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.LinearGradientFill;
import org.joshy.gfx.draw.MultiGradientFill;

import java.awt.geom.Path2D;

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
        MultiGradientFill fill = new LinearGradientFill()
                .setStartX(0).setEndX(0).setStartY(0).setEndY(s * 2)
                .addStop(0, FlatColor.hsb(hue, 0.4, 1.0))
                .addStop(1, FlatColor.hsb(hue, 1.0, 1.0));
        g.setPaint(fill);
        g.translate(x-s,y-s);
        g.fillOval(0,0,s*2,s*2);
        g.translate(-x+s,-y+s);

    }

    public static void drawTriangleHandle(GFX g, double x, double y, FlatColor color, boolean vertical) {
        double s = 0;
        double yoff = 0;
        if(vertical) {
            //shadow
            g.setPaint(new FlatColor(0x404040).deriveWithAlpha(0.5));
            s = 14;
            yoff = 1;
            g.fillPolygon(new double[]{0.0-s/2,0.0+yoff, s/2,0.0 +yoff,0,s+yoff});

            //border
            g.setPaint(FlatColor.WHITE);
            s = 14;
            yoff = 0;
            g.fillPolygon(new double[]{0.0-s/2,0.0+yoff, s/2,0.0 +yoff,0,s+yoff});
            //center
            s = 10;
            yoff = 1;
            double hue = color.getHue();
            MultiGradientFill fill = new LinearGradientFill()
                    .setStartX(0).setEndX(0).setStartY(0).setEndY(s)
                    .addStop(0,FlatColor.hsb(hue,0.4,1.0))
                    .addStop(1,FlatColor.hsb(hue,1.0,1.0))
                    ;
            g.setPaint(fill);
            g.translate(0,0);
            g.fillPolygon(new double[]{0.0-s/2,0.0+yoff, s/2,0.0 +yoff,0,s+yoff});
            g.translate(0,0);
        } else {
            //shadow
            g.setPaint(new FlatColor(0x404040).deriveWithAlpha(0.5));
            s = 14;
            yoff = 1;
            g.fillPolygon(new double[]{0.0+yoff, 0.0-s/2, 0.0+yoff, s/2,0.0+s +yoff,0});


            //border
            g.setPaint(FlatColor.WHITE);
            s = 14;
            yoff = 0;
            g.fillPolygon(new double[]{0.0+yoff, 0.0-s/2, 0.0+yoff, s/2,0.0+s +yoff,0});
            //center
            s = 10;
            yoff = 1;
            double hue = color.getHue();
            MultiGradientFill fill = new LinearGradientFill()
                    .setStartX(0).setEndX(s).setStartY(0).setEndY(0)
                    .addStop(0,FlatColor.hsb(hue,0.4,1.0))
                    .addStop(1,FlatColor.hsb(hue,1.0,1.0))
                    ;
            g.setPaint(fill);
            g.translate(0,0);
            g.fillPolygon(new double[]{0.0+yoff, 0.0-s/2, 0.0+yoff, s/2,0.0+s +yoff,0});
            g.translate(0,0);
        }
    }

    public static Path2D.Double createLeftTriangle(double s) {
        Path2D.Double pth = new Path2D.Double();
        pth.moveTo(0,0);
        pth.lineTo(s,-s/2);
        pth.lineTo(s,s/2);
        pth.closePath();
        return pth;
    }
}
