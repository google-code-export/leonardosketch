package org.joshy.sketch.controls;

import org.joshy.gfx.animation.KeyFrameAnimator;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Control;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 9/29/11
 * Time: 12:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class NotificationIndicator extends Control {
    private String string;
    private KeyFrameAnimator anim;

    public void addNotification(String s) {
        this.string = s;
        animate();
    }

    private void animate() {
        if(anim != null) {
            anim.stop();
            anim = null;
        }

        if(anim == null) {
            anim  = KeyFrameAnimator.create(this, "opacity")
            .keyFrame(0,0.0)
            .keyFrame(0.1,1.0)
            .keyFrame(2,1.0)
            .keyFrame(5,0.0);
        }
        this.setOpacity(0);
        anim.start();

    }


    @Override
    public void draw(GFX gfx) {
        if(this.string != null) {
            Font font = Font.name("OpenSans").size(26).resolve();
            double width = font.calculateWidth(string) + 30;
            if(width < getWidth()) {
                width = getWidth();
            }
            gfx.setPaint(FlatColor.hsb(0, 0, 0.8, this.getOpacity() * 0.9));
            gfx.fillRoundRect(0, 0, width, getHeight(), 10, 10);
            gfx.setPaint(FlatColor.hsb(0,0,0.2,this.getOpacity()));
            gfx.drawText(string, font ,15,35);
        }
    }

    @Override
    public Bounds getVisualBounds() {
        return new Bounds(0,0,getWidth(),getHeight());
    }

    @Override
    public void doLayout() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doPrefLayout() {
        this.setWidth(400);
        this.setHeight(50);
    }

    @Override
    public void doSkins() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Bounds getInputBounds() {
        return getVisualBounds();
    }
}
