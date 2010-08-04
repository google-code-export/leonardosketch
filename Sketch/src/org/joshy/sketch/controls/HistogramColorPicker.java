package org.joshy.sketch.controls;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.control.Control;
import org.joshy.sketch.modes.pixel.PixelDocContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: May 25, 2010
 * Time: 5:40:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class HistogramColorPicker extends Control implements Callback<MouseEvent> {
    private Map<FlatColor,String> colorMap;
    private List<FlatColor> colorList;
    private PixelDocContext context;

    public HistogramColorPicker(PixelDocContext context) {
        this.context = context;
        this.colorMap = new HashMap<FlatColor,String>();
        this.colorList = new ArrayList<FlatColor>();
        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, this);
        setWidth(200);
        setHeight(20);
    }

    @Override
    public void draw(GFX g) {
        g.setPaint(FlatColor.BLUE);
        g.drawRect(0,0,200,20);
        double x = 0;
        for(FlatColor color : colorList) {
            g.setPaint(color);
            g.fillRect(x,0,20,20);
            g.setPaint(FlatColor.WHITE);
            g.drawRect(x,0,20,20);
            x+=20;
        }
    }

    @Override
    public void doLayout() {

    }

    @Override
    public void doSkins() {

    }

    public void addColor(FlatColor color) {
        if(!colorMap.containsKey(color)) {
            colorMap.put(color,"");
            colorList.add(color);
            setDrawingDirty();
        }
    }

    public void call(MouseEvent event) {
        if(event.getType() == MouseEvent.MousePressed) {
            if(getInputBounds().contains(event.getX()+getTranslateX(),event.getY()+getTranslateY())) {
                int x = (int)(event.getX()/20);
                FlatColor color = colorList.get(x);
                context.getPixelToolbar().pixelColorPicker.setSelectedColor(color);
            }
        }
    }
}
