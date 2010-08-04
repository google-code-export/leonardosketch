package org.joshy.gfx.node.control;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.control.skin.FontSkin;

import java.util.Date;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: May 3, 2010
* Time: 10:46:03 AM
* To change this template use File | Settings | File Templates.
*/
public class PopupMenu extends Control {
    private ListModel model;
    private int hoverRow = -1;
    private Callback<ChangedEvent> callback;
    private long openTime;

    public PopupMenu(ListModel model, Callback<ChangedEvent> callback) {
        setVisible(true);
        this.model = model;
        this.callback = callback;
        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>(){
            public void call(MouseEvent event) {
                processMouse(event);
            }
        });
    }

    public void setModel(ListModel model) {
        this.model = model;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        openTime = new Date().getTime();
    }

    private void processMouse(MouseEvent event) {
        long currentTime = new Date().getTime();
        if(event.getType() == MouseEvent.MouseDragged || event.getType() == MouseEvent.MouseMoved) {
            hoverRow = (int)(event.getY()/rowHeight);
            setDrawingDirty();
        }
        if(event.getType() == MouseEvent.MouseReleased) {
            hoverRow = (int)(event.getY()/rowHeight);
            //if click open, do nothing
//            if(currentTime - openTime < 500) {
//                u.p("did a click open");
//            } else {
//                u.p("closing");
                //else fire selection and hide
                if(hoverRow >= 0 && hoverRow < model.size()) {
                    fireSelection(hoverRow);
                }
                setDrawingDirty();
                setVisible(false);
//            }
        }
    }

    private void fireSelection(int row) {
        this.callback.call(new ChangedEvent(ChangedEvent.IntegerChanged,(Integer)row,this));
    }

    double rowHeight = 25;
    static double spacer = 5;
    double arc = 10;

    @Override
    public void draw(GFX g) {
        if(!isVisible()) return;
        g.setPaint(FlatColor.WHITE);
        g.fillRoundRect(0,0,getWidth(),getHeight(),arc,arc);
        g.setPaint(FlatColor.BLACK);
        g.drawRoundRect(0,0,getWidth(),getHeight(),arc,arc);

        for(int i=0; i<model.size(); i++) {
            Object o = model.get(i);
            double rowy = i*rowHeight;
            Paint bg = FlatColor.WHITE;
            Paint fg = FlatColor.BLACK;
            if(i == hoverRow) {
                bg = FlatColor.BLUE;
                fg = FlatColor.WHITE;
            } else {
                bg = FlatColor.WHITE;
                fg = FlatColor.BLACK;
            }
            g.setPaint(bg);
            g.fillRect(1,rowy+spacer,getWidth()-1,rowHeight);
            g.setPaint(fg);
            Font.drawCenteredVertically(g,o.toString(), FontSkin.DEFAULT.getFont(),
                    3,rowy+spacer,getWidth(),rowHeight,true);
        }
    }

    @Override
    public void doLayout() {
        setHeight( rowHeight * model.size() + spacer*2);
    }

    @Override
    public void doSkins() {
    }

    public ListModel getModel() {
        return model;
    }

    public void setCallback(Callback<ChangedEvent> callback) {
        this.callback = callback;
    }
}
