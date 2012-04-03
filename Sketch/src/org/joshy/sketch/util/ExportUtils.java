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
}
