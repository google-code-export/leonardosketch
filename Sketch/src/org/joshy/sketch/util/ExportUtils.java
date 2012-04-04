package org.joshy.sketch.util;

import org.joshy.gfx.draw.FlatColor;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 4/3/12
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExportUtils {
    public static String toHexString(FlatColor color) {
        return "#"+String.format("%06x",color.getRGBA()&0x00FFFFFF);
    }

    public static String toRGBAHexString(FlatColor color) {
        return "#"
                +String.format("%02x",(int)(color.getRed()*255.0))
                +String.format("%02x",(int)(color.getGreen()*255.0))
                +String.format("%02x",(int)(color.getBlue()*255.0))
                +String.format("%02x",(int)(color.getAlpha()*255.0))
                ;
    }
}
