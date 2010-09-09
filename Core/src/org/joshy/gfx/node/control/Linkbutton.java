package org.joshy.gfx.node.control;

import org.joshy.gfx.css.CSSMatcher;
import org.joshy.gfx.css.CSSSkin;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Bounds;

public class Linkbutton extends Button {
    public Linkbutton(String text) {
        super(text);
        selectable = false;
    }

    @Override
    public void draw(GFX g) {
        if(!isVisible()) return;
        if(cssSkin != null) {
            if(size == null) {
                doPrefLayout();
            }
            CSSMatcher matcher = cssSkin.createMatcher(this, CSSSkin.State.None);
            if(hovered) {
                matcher = cssSkin.createMatcher(this, CSSSkin.State.Hover);
            }
            if(pressed) {
                matcher = cssSkin.createMatcher(this, CSSSkin.State.Pressed);
            }
            Bounds bounds = new Bounds(0,0,getWidth(),getHeight());
            cssSkin.drawBackground(g, matcher, "", bounds);
            cssSkin.drawBorder(g, matcher, "", bounds);
            cssSkin.drawText(g,matcher,"",bounds,text);
            return;
        }
    }
}
