package org.joshy.sketch.controls;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.stage.Stage;

import java.awt.geom.Point2D;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: 4/26/11
* Time: 7:44 PM
* To change this template use File | Settings | File Templates.
*/
public class FreerangeColorPicker extends Button {
    FlatColor selectedColor = FlatColor.RED;
    private FreerangeColorPickerPopup popup;

    public boolean isRecenterOnSelect() {
        return recenterOnSelect;
    }

    public void setRecenterOnSelect(boolean recenterOnSelect) {
        this.recenterOnSelect = recenterOnSelect;
    }

    private boolean recenterOnSelect = true;

    @Override
    protected void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (pressed) {
            if (popup == null) {
                popup = new FreerangeColorPickerPopup(this,200,100,true);
                //popup = new HSVColorPicker(this,150,150);
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
            EventBus.getSystem().setPressedNode(popup);
        } else {
            if(popup != null) popup.setVisible(false);
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
}
