package org.joshy.gfx.css;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Control;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Jul 30, 2010
* Time: 4:16:12 PM
* To change this template use File | Settings | File Templates.
*/
class SimpleBoxControl extends Control {
    private String text = "long text string";
    private InteractiveTest master;
    private double contentWidth;
    private double contentHeight;
    private boolean hover;
    private boolean pressed;

    SimpleBoxControl(InteractiveTest master) {
        this.master = master;
        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public void call(MouseEvent event) {
//                u.p("event");
                if(event.getType() == MouseEvent.MouseEntered) {
//                    u.p("mouse inside");
                    hover = true;
                    setDrawingDirty();
                }
                if(event.getType() == MouseEvent.MousePressed) {
                    pressed = true;
                    setDrawingDirty();
                }
                if(event.getType() == MouseEvent.MouseReleased) {
                    pressed = false;
                    setDrawingDirty();
                }
                if(event.getType() == MouseEvent.MouseExited) {
//                    u.p("mouse outside");
                    hover = false;
                    setDrawingDirty();
                }
            }
        });
    }
    @Override
    public void doSkins() {
        width = 200;/*
        height = 200;
        if(master.set != null) {
            String name = "box";
            int margin = master.set.findIntegerValue(name,"margin");
            int padding = master.set.findIntegerValue(name,"padding");
            int borderWidth = master.set.findIntegerValue(name, "border-width");
            contentWidth = width-margin*2-padding*2;
            contentHeight = height-margin*2-padding*2;
            //calc the sizes
            if("true".equals(master.set.findStringValue(name,"shrink-to-fit"))) {
                Font font = FontSkin.DEFAULT.getFont();
                contentWidth = font.calculateWidth(text);
                width = margin*2+borderWidth*2+padding*2+contentWidth;
                contentHeight = font.calculateHeight(text);
                height = margin*2+borderWidth*2+padding*2+contentHeight;
            }
        }*/
    }

    @Override
    public void draw(GFX g) {/*
        CSSRuleSet set = master.set;
        if(master.set == null) {
            g.setPaint(FlatColor.BLUE);
            g.fillRect(0,0,20,20);
            return;
        }

        CSSMatcher matcher = new CSSMatcher();
        matcher.element = "box";
        if(hover) {
            matcher.pseudo = "hover";
        }
        if(pressed) {
            matcher.pseudo = "pressed";
        }
        String name = "box";

        int margin = set.findIntegerValue(name,"margin");
        int padding = set.findIntegerValue(name,"padding");
        int borderWidth = set.findIntegerValue(name, "border-width");
        int borderRadius = set.findIntegerValue(name,"border-radius");
        BaseValue background = set.findValue(matcher,"background");
//        u.p("background = " + background);

        Font font = FontSkin.DEFAULT.getFont();



        //draw the background
        double backWidth = width-margin*2;
        double backHeight = height-margin*2;

        //background-color
        g.setPaint(new FlatColor(set.findColorValue(matcher,"background-color")));

        if(borderRadius == 0) {
            g.fillRect(margin,margin,backWidth,backHeight);
        } else {
            g.fillRoundRect(margin,margin,backWidth,backHeight,borderRadius,borderRadius);
        }

        //draw the border
        g.setPaint(new FlatColor(set.findColorValue(matcher,"border-color")));
        g.setStrokeWidth(borderWidth);
        if(borderRadius == 0) {
            g.drawRect(margin+borderWidth/2,margin+borderWidth/2,width-margin*2-borderWidth,height-margin*2-borderWidth);
        } else {
            g.drawRoundRect(margin+borderWidth/2,margin+borderWidth/2,width-margin*2-borderWidth,height-margin*2-borderWidth,borderRadius,borderRadius);
        }

        g.setStrokeWidth(1);

        //draw the internal content
        //g.setPaint(FlatColor.GREEN);
        double contentX = margin+borderWidth+padding;
        double contentY = margin+borderWidth+padding;
        //g.fillRect(contentX,contentY,contentWidth,contentHeight);
        //g.setPaint(FlatColor.BLACK);

        String textAlign = set.findStringValue(name,"text-align");
        g.setPaint(new FlatColor(set.findColorValue(matcher,"color")));
        if("center".equals(textAlign)) {
            Font.drawCentered(g,text,font,contentX,contentY,contentWidth,contentHeight,true);
        } else {
            Font.drawCenteredVertically(g,text,font,contentX,contentY,contentWidth,contentHeight,true);
        }

        //g.setPaint(FlatColor.RED);
        //g.drawRect(0,0,width,height);
        //g.translate(-50,-50);*/
    }

    @Override
    public Bounds getVisualBounds() {
        return new Bounds(getTranslateX(),getTranslateY(),getWidth(),getHeight());
    }

    @Override
    public void doLayout() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public Bounds getInputBounds() {
        return getVisualBounds();
    }
}
