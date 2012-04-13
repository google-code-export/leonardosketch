package org.joshy.sketch.pixel.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.sketch.model.CanvasDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/17/11
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class PixelDoc extends CanvasDocument {
    private List<PixelLayer> layers;
    private FlatColor foregroundColor = FlatColor.BLACK;
    private FlatColor backgroundColor = FlatColor.WHITE;
    private boolean repeat = false;
    private int repeatSize = 16;
    private int maxTileX = 1;
    private int maxTileY = 1;

    public PixelDoc() {
        layers = new ArrayList<PixelLayer>();
        PixelLayer layer1 = new PixelLayer(this);
        add(layer1);
        this.setWidth(640);
        this.setHeight(480);
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
        this.layers.clear();
        PixelLayer layer1 = new PixelLayer(this);
        add(layer1);
    }

    public void add(PixelLayer layer1) {
        this.layers.add(layer1);
    }

    public Iterable<PixelLayer> getLayers() {
        return layers;
    }

    public PixelLayer getCurrentLayer() {
        return layers.get(0);
    }

    public void setForegroundColor(FlatColor foregroundColor) {
        this.foregroundColor = foregroundColor;
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.ColorChanged,this.foregroundColor,this));
    }

    public FlatColor getForegroundColor() {
        return foregroundColor;
    }

    public FlatColor getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(FlatColor backgroundColor) {
        this.backgroundColor = backgroundColor;
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.ColorChanged,this.backgroundColor,this));
    }

    public int getRepeatSize() {
        return repeatSize;
    }

    public void setRepeatSize(int repeatSize) {
        this.repeatSize = repeatSize;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public int getMaxTileX() {
        return maxTileX;
    }

    public int getMaxTileY() {
        return maxTileY;
    }

    public void addedTile(int tx, int ty) {
        maxTileX = Math.max(maxTileX,tx);
        maxTileY = Math.max(maxTileY,ty);
    }

}
