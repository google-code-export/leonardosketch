package org.joshy.gfx.node.control;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.stage.Stage;

import java.awt.geom.Point2D;

public class PopupMenuButton<E> extends Button implements SelectableControl {
    private ListModel model;
    private int selectedIndex;
    private PopupMenu popup;

    public PopupMenuButton()  {
        setWidth(200);
        setHeight(25);
        setSkinDirty();
        setModel(new ListModel<E>() {
            public E get(int i) {
                return (E) ("Dummy item " + i);
            }
            public int size() {
                return 3;
            }
        });
        setSelectedIndex(0);
        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>(){
            public void call(MouseEvent event) {
                processMouse(event);
            }
        });
    }

    private void processMouse(MouseEvent event) {
        
    }

    public void setModel(ListModel<E> model) {
        this.model = model;
    }

    @Override
    protected void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if(pressed) {
            if(popup == null) {
                popup = new PopupMenu(getModel(), new Callback<ChangedEvent>() {
                    public void call(ChangedEvent event) {
                        setSelectedIndex((Integer)event.getValue());
                    }
                });
                popup.setWidth(200);
                popup.setHeight(200);
                popup.setVisible(false);
                Stage stage = getParent().getStage();
                stage.getPopupLayer().add(popup);
            }
            Point2D pt = NodeUtils.convertToScene(this,0,0-this.getSelectedIndex()*25-PopupMenu.spacer);
            popup.setTranslateX(Math.max(pt.getX(),0));
            popup.setTranslateY(Math.max(pt.getY(),0));
            popup.setVisible(true);
        } else {
            popup.setVisible(false);
        }
    }

    @Override
    public void draw(GFX g) {
        if(!isVisible()) return;
        g.setPaint(FlatColor.WHITE);
        g.fillRoundRect(0,0,getWidth(),getHeight(), 10,10);
        g.setPaint(FlatColor.BLACK);
        Object o = getSelectedItem();
        Font.drawCenteredVertically(g, o.toString(), font.getFont(),4, 0, getWidth(), getHeight(), true);

        double[] points = new double[]{0,0, 14,0, 7, 9};
        g.translate(getWidth()-22,6);
        g.fillPolygon(points);
        g.translate(-getWidth()+22,-6);
        g.setPaint(FlatColor.BLACK);
        g.drawRoundRect(0,0,getWidth(),getHeight(), 10,10);
    }

    public E getSelectedItem() {
        return getModel().get(getSelectedIndex());
    }

    @Override
    public void doLayout() {
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        EventBus.getSystem().publish(new SelectionEvent(SelectionEvent.Changed,this));
        setDrawingDirty();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public ListModel<E> getModel() {
        return model;
    }

}