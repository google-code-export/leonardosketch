package org.joshy.sketch.controls;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.control.Control;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Sep 10, 2010
* Time: 5:12:39 PM
* To change this template use File | Settings | File Templates.
*/
public class Ruler extends Control {
    private boolean vertical;

    public Ruler(boolean vertical) {
        this.vertical = vertical;
    }

    @Override
    public void doLayout() {

    }

    @Override
    public void doSkins() {

    }

    @Override
    public void draw(GFX g) {
        Font fnt = Font.name("Arial").size(10).resolve();
        g.setPaint(FlatColor.BLACK);
        g.drawRect(0,0,getWidth(),getHeight());
        g.setPaint(new FlatColor(0x505050));
        if(vertical) {
            int y = 0;
            int w = (int) getWidth();
            while(true) {
                //major ticks
                g.drawLine(10,y,w,y);
                g.drawText(""+y, fnt,2, y+12);

                for(int i=1; i<=4; i++) {
                    g.drawLine(20,y+i*10,w,y+i*10);
                }

                y+=50;
                if(y > getHeight()) break;
            }
        } else {
            int x = 0;
            int h = (int) getHeight();
            while(true) {
                g.drawLine(x,10,x,h);
                g.drawText(""+x, fnt,x+3, 12);
                for(int i=1; i<=4; i++) {
                    g.drawLine(x+i*10,20,x+i*10,h);
                }
                x+=50;
                if(x > getWidth()) break;
            }
        }
    }
}
