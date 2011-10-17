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
 * User: josh
 * Date: 10/15/11
 * Time: 7:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlphaPicker extends Button {
    private double selectedAlpha;
    private AlphaSliderPopup popup;
    private double alpha;


    @Override
    protected void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (pressed) {
            if (popup == null) {
                popup = new AlphaSliderPopup(this,10,100,true);
                popup.setVisible(false);
                Stage stage = getParent().getStage();
                stage.getPopupLayer().add(popup);
            }
            Point2D pt = NodeUtils.convertToScene(this, 0, getHeight());

            double x = pt.getX();
            double y = pt.getY();
            popup.setAlpha(getAlpha());
            popup.setVisible(true);
            popup.setHeight(100);
            popup.positionAt(x, y, getAlpha());
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
        g.setPaint(FlatColor.GRAY);
        g.fillOval(0 + 2, 0 + 2, getWidth() - 4, getHeight() - 4);
        g.setPureStrokes(false);
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.DoubleChanged,alpha,this));
        setDrawingDirty();
    }

    public void onAlphaSelected(Callback<ChangedEvent> callback) {
        EventBus.getSystem().addListener(this, ChangedEvent.DoubleChanged, callback);
    }

    public double getAlpha() {
        return alpha;
    }
}
