package org.joshy.sketch.controls;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.stage.Stage;

import java.awt.geom.Point2D;
import java.util.Date;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: 4/26/11
* Time: 7:44 PM
* To change this template use File | Settings | File Templates.
*/
public class FreerangeColorPicker extends Control {
    FlatColor selectedColor = FlatColor.RED;
    private GenericColorPickerPopup popup;
    private long lastPressTime;
    private double startX;
    private double startY;


    public FreerangeColorPicker() {
        EventBus.getSystem().addListener(this,MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public void call(MouseEvent event) throws Exception {
                if(event.getType() == MouseEvent.MousePressed) {
                    doPress(event);
                }
                if(event.getType() == MouseEvent.MouseDragged) {
                    doDrag(event);
                }
                if(event.getType() == MouseEvent.MouseReleased) {
                    doRelease();
                }
            }
        });
    }

    public boolean isRecenterOnSelect() {
        return recenterOnSelect;
    }

    public void setRecenterOnSelect(boolean recenterOnSelect) {
        this.recenterOnSelect = recenterOnSelect;
    }

    private boolean recenterOnSelect = false;

    protected void doPress(MouseEvent e ) {
        startX = e.getX();
        startY = e.getY();

        if (popup == null) {
            popup = new GenericColorPickerPopup(this,200,100,true);
            popup.setVisible(false);
            Stage stage = getParent().getStage();
            stage.getPopupLayer().add(popup);
        }
        Point2D pt = NodeUtils.convertToScene(this, 0, getHeight());

        double x = pt.getX();
        double y = pt.getY();
        if(recenterOnSelect) {
            FlatColor color = this.getSelectedColor();
            popup.positionAt(x, y, color);
        } else {
            popup.setTranslateX(x);
            popup.setTranslateY(y);
        }
        popup.setVisible(true);
        lastPressTime = new Date().getTime();
    }


    private void doDrag(MouseEvent event) {
        double dx = event.getX()-startX;
        double dy = event.getY()-startY;
        if(Math.abs(dx) > 5 || Math.abs(dy) > 5 && (new Date().getTime() - lastPressTime) > 200) {
            popup.takeoverDrag();
        }
    }


    protected void doRelease() {
        if(new Date().getTime() - lastPressTime < 500) {
            EventBus.getSystem().setPressedNode(popup);
        } else {
        }
    }

    @Override
    public void draw(GFX g) {
        if(!isVisible())return;
        g.setPureStrokes(true);
        g.setPaint(FlatColor.BLACK);
        g.fillOval(0, 0, getWidth(), getHeight());
        g.setPaint(FlatColor.WHITE);
        g.fillOval(0 + 1, 0 + 1, getWidth() - 2, getHeight() - 2);
        g.setPaint(selectedColor);
        g.fillOval(0 + 2, 0 + 2, getWidth() - 4, getHeight() - 4);
        g.setPureStrokes(false);
    }

    public void setSelectedColor(FlatColor color) {
        this.selectedColor = color;
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.ColorChanged,selectedColor,this));
        setDrawingDirty();
    }

    public void onColorSelected(Callback<ChangedEvent> callback) {
        EventBus.getSystem().addListener(this, ChangedEvent.ColorChanged, callback);
    }

    public FlatColor getSelectedColor() {
        return selectedColor;
    }

    public void setFinalColor(FlatColor selectedColor) {
    }

    @Override
    public void doLayout() {
        this.setWidth(this.getPrefWidth());
        setHeight(getPrefHeight());
    }

    @Override
    public void doPrefLayout() {
    }

    @Override
    public void doSkins() {
    }
}
