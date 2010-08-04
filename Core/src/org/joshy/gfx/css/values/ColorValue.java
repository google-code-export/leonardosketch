package org.joshy.gfx.css.values;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Jul 28, 2010
* Time: 9:45:54 PM
* To change this template use File | Settings | File Templates.
*/
public class ColorValue extends BaseValue {
    private int rgb;

    public ColorValue(String text) {
        this.rgb = Integer.parseInt(text.substring(1),16);
    }

    @Override
    public String asString() {
        return Integer.toHexString(rgb);
    }

    public int getValue() {
        return rgb;
    }
}
