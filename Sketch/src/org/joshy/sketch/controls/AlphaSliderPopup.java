package org.joshy.sketch.controls;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.control.Control;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 10/15/11
 * Time: 7:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlphaSliderPopup extends Control  {
    private double alpha = 0.5;
    private boolean hideOnSelect;
    private AlphaPicker alphaPicker;

    public AlphaSliderPopup(AlphaPicker alphaPicker, int width, int height, boolean hideOnSelect) {
        this.setPrefWidth(width);
        this.setPrefHeight(height);
        this.alphaPicker = alphaPicker;
        this.hideOnSelect = hideOnSelect;
        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public void call(MouseEvent event) {
                processMouse(event);
            }
        });
    }

    private void processMouse(MouseEvent event) {
        if (event.getType() == MouseEvent.MouseDragged) {
            double alpha = event.getY()/getHeight();
            if(alpha < 0) alpha = 0;
            if(alpha > 1) alpha = 1;
            setAlpha(alpha);
        }
        if (event.getType() == MouseEvent.MouseReleased) {
            setDrawingDirty();
            if(hideOnSelect) {
                setVisible(false);
            }
            //setFinalColor(getSelectedColor());
        }
    }

    @Override
    public void doLayout() {
        setWidth(getPrefWidth());
        setHeight(getPrefHeight());
    }

    @Override
    public void doPrefLayout() {
        //noop
    }

    @Override
    public void doSkins() {
    }

    @Override
    public void draw(GFX gfx) {
        if (!isVisible()) return;
        gfx.setPaint(FlatColor.WHITE);
        double arc = getWidth();
        gfx.fillRoundRect(0,0,getWidth(),getHeight(),arc,arc);
        gfx.setPaint(FlatColor.GRAY);
        gfx.drawRoundRect(0,0,getWidth(),getHeight(),arc,arc);
        gfx.setPaint(FlatColor.BLACK);
        double y = getAlpha()*getHeight();
        gfx.fillOval(0,y-3,getWidth(),getWidth());
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.DoubleChanged,this.alpha,this,true));
        alphaPicker.setAlpha(this.alpha);
        setDrawingDirty();
    }

    public void positionAt(double x, double y, double alpha) {
        setTranslateX(x);
        setTranslateY(y - alpha * getHeight());
    }
}
