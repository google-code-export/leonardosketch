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
        width = 200;
    }

    @Override
    public void draw(GFX g) {
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
