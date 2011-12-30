package org.joshy.sketch.controls;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.Container;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.util.u;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 12/29/11
 * Time: 3:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenericColorPickerPopup extends Container {
    private FreerangeColorPickerPopup freerange;
    private HSVColorPicker hsv;
    private AbstractColorPickerPopup active;
    private Button freeButton;
    private Button hsvButton;
    private FreerangeColorPicker delegate;

    public GenericColorPickerPopup(FreerangeColorPicker delegate, int width, int height, boolean hideOnSelect) {
        super();
        this.delegate = delegate;
        freerange = new FreerangeColorPickerPopup(this,200,100,true);
        hsv = new HSVColorPicker(this,150,150);
        active = freerange;
        freerange.setVisible(true);
        active = freerange;
        hsv.setVisible(false);
        freeButton = new Button("free");
        hsvButton = new Button("hsv");
        add(freeButton);
        add(hsvButton);
        add(freerange);
        add(hsv);

        freeButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) throws Exception {
                active = freerange;
                freerange.setVisible(true);
                hsv.setVisible(false);
            }
        });

        hsvButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) throws Exception {
                active = hsv;
                freerange.setVisible(false);
                hsv.setVisible(true);
            }
        });
    }

    @Override
    public void doLayout() {
        freeButton.setWidth(55);
        freeButton.setHeight(35);
        hsvButton.setWidth(55);
        hsvButton.setHeight(35);
        freeButton.setTranslateX(0);
        freeButton.setTranslateY(0);
        hsvButton.setTranslateX(freeButton.getVisualBounds().getX2()+1);
        hsvButton.setTranslateY(0);
        freerange.setTranslateX(0);
        freerange.setTranslateY(freeButton.getVisualBounds().getY2()+1);
        hsv.setTranslateX(0);
        hsv.setTranslateY(freeButton.getVisualBounds().getY2()+1);
        super.doLayout();
    }

    public void positionAt(double x, double y, FlatColor color) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public void takeoverDrag() {
        active.startDrag();
        EventBus.getSystem().setPressedNode((Node) active);
    }

    public void setSelectedColor(FlatColor color) {
        if(delegate != null) {
            delegate.setSelectedColor(color);
        }
    }

    @Override
    public void draw(GFX g) {
        if(!isVisible()) return;
        g.setOpacity(getOpacity());
        //drawSelf(g);
        for(Node child : children) {
            if(!child.isVisible()) continue;
            g.translate(child.getTranslateX(),child.getTranslateY());
            child.draw(g);
            g.translate(-child.getTranslateX(),-child.getTranslateY());
        }
        this.drawingDirty = false;
        g.setOpacity(1.0);
    }
}
