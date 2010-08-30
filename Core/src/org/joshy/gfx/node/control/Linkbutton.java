package org.joshy.gfx.node.control;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Node;

import java.io.IOException;

public class Linkbutton extends Button {
    public Linkbutton(String text) {
        super(text);
        selectable = false;
    }

    @Override
    public void draw(GFX g) {
        g.setPaint(FlatColor.BLACK);
        if(pressed) {
            g.setPaint(FlatColor.BLUE);
        }
        double x = insets.getLeft();

        if(text == null) return;
        if(text.length() == 0) return;
        double tw = font.getWidth(text);
        double th = font.getAscender();
//        if(includeDescender) {
//            th += font.getDescender();
//        }
        double ty = 0 + (height -th)/2 + font.getAscender(); 
        g.drawText(text,font,x, ty);

        if(hovered) {
            g.drawLine(x,ty+1,tw,ty+1);
        }
    }
}
