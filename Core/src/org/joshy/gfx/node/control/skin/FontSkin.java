package org.joshy.gfx.node.control.skin;

import org.joshy.gfx.draw.Font;
import org.joshy.gfx.node.Skin;
import org.joshy.gfx.util.u;
import org.w3c.dom.Element;

public class FontSkin extends Skin {
    private Font font;
    private static Font defaultFont = Font.name("Arial").size(13).resolve(); 
    public static final FontSkin DEFAULT = new FontSkin(defaultFont);

    public FontSkin(Font font) {
        this.font = font;
    }

    public FontSkin(Element element) {
        String name = element.getAttribute("name");
        Float size = Float.parseFloat(element.getAttribute("size"));
        font = Font.name(name).size(size).resolve();
    }

    public Font getFont() {
        return font;
    }
}
