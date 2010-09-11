package org.joshy.sketch.controls;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.ScrollPane;
import org.joshy.gfx.node.control.Scrollbar;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Sep 10, 2010
* Time: 5:12:39 PM
* To change this template use File | Settings | File Templates.
*/
public class Ruler extends Control {
    private boolean vertical;
    private double offset;

    public Ruler(boolean vertical, ScrollPane scrollPane) {
        this.vertical = vertical;

        Scrollbar sp;
        if(vertical) {
            sp = scrollPane.getVerticalScrollBar();
        } else {
            sp = scrollPane.getHorizontalScrollBar();
        }

        EventBus.getSystem().addListener(sp, ChangedEvent.DoubleChanged, new Callback<ChangedEvent>(){
            public void call(ChangedEvent event) {
                offset = (Double) event.getValue();
                setDrawingDirty();
            }
        });
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
        Bounds oldBounds = g.getClipRect();
        g.setClipRect(new Bounds(0,0,getWidth(),getHeight()));

        g.setPaint(FlatColor.RED);
        if(vertical) {
            g.setPaint(new GradientFill(FlatColor.hsb(0,0,0.9),FlatColor.hsb(0,0,0.5),90,false,0,0,getWidth(),0));
        } else {
            g.setPaint(new GradientFill(FlatColor.hsb(0,0,0.9),FlatColor.hsb(0,0,0.5),90,false,0,0,0,getHeight()));
        }
        g.fillRect(0,0,getWidth()-1,getHeight()-1);
        g.setPaint(FlatColor.BLACK);
        g.drawRect(0,0,getWidth()-1,getHeight()-1);
        g.setPaint(new FlatColor(0x505050));
        int o = (int) offset;
        int step = 50;
        if(vertical) {
            int y = 0;
            int w = (int) getWidth();
            while(true) {
                if(y-o > -step) {
                    //major ticks
                    g.drawLine(10,y-o,w,y-o);
                    g.drawText(""+y, fnt,2, y+12-o);

                    for(int i=1; i<=4; i++) {
                        g.drawLine(20,y+i*10-o,w,y+i*10-o);
                    }
                }

                y+=step;
                if(y-o > getHeight()) break;
            }
        } else {
            int x = 0;
            int h = (int) getHeight();
            while(true) {
                if(x-o > -step) {
                    g.drawLine(x-o,10,x-o,h);
                    g.drawText(""+x, fnt,x+3-o, 12);
                    for(int i=1; i<=4; i++) {
                        g.drawLine(x+i*10-o,20,x+i*10-o,h);
                    }
                }
                x+=step;
                if(x-o > getWidth()) break;
            }
        }
        g.setClipRect(oldBounds);
    }
}