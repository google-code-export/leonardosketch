package org.joshy.gfx.node.layout;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.Control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 5, 2010
 * Time: 6:27:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class TabPanel extends Panel {
    private TabTop tabtop = new TabTop();
    private List<Control> tabs = new ArrayList<Control>();
    private Map<Control,String> titleMap = new HashMap<Control,String>();

    public TabPanel() {
        add(tabtop);
    }

    public void add(String title, Control control) {
        add(control);
        tabs.add(control);
        titleMap.put(control,title);
    }

    @Override
    public void doLayout() {
        for(Control c : controlChildren()) {
            c.doPrefLayout();
            if(c == tabtop) {
                tabtop.setWidth(getWidth());
                tabtop.setHeight(30);
                tabtop.setTranslateX(0);
                tabtop.setTranslateY(0);
            } else {
                c.setWidth(getWidth());
                c.setHeight(getHeight()-30);
                c.setTranslateX(0);
                c.setTranslateY(30);
            }
            c.doLayout();
        }
        setDrawingDirty();
    }

    public void setSelected(Node node) {
        for(Control c : tabs){
            c.setVisible(false);
        }
        node.setVisible(true);
        setDrawingDirty();
    }

    private class TabTop extends Control {
        private TabTop() {
            EventBus.getSystem().addListener(this, MouseEvent.MousePressed, new Callback<MouseEvent>(){
                public void call(MouseEvent event) {
                    int x = (int) (event.getX()/getWidth()*tabs.size());
                    setSelected(tabs.get(x));
                }
            });
        }

        @Override
        public void doLayout() {
        }

        @Override
        public void doSkins() {
        }

        @Override
        public void draw(GFX g) {
            g.setPaint(FlatColor.GRAY);
            g.fillRect(0,0,getWidth(),getHeight());


            double size = getWidth()/tabs.size();
            double x = 0;
            for(Control c : tabs) {

                //background
                g.setPaint(FlatColor.GRAY);
                if(c.isVisible()) {
                    g.setPaint(FlatColor.WHITE);
                }
                g.fillRect(x,0,size,30);

                //border
                g.setPaint(FlatColor.BLACK);
                g.drawRect(x,0,size,30);

                //title
                String title = titleMap.get(c);
                g.drawText(title, Font.name("Arial").size(12).resolve(),x+5,12);
                x+=size;
            }
        }
    }
}
