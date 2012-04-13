package org.joshy.sketch.pixel.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/17/11
 * Time: 5:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class PixelLayer {
    private PixelGraphics graphics;
    private Map<String,PixelTile> tiles;
    private PixelDoc document;

    public PixelLayer(PixelDoc document) {
        this.document = document;
        tiles = new HashMap<String,PixelTile>();
        if(document.isRepeat()) {
            graphics = new RepeatPixelGraphics(this);
        } else {
            graphics = new PixelGraphics(this);
        }
    }

    public PixelGraphics getGraphics() {
        return graphics;
    }

    //tile space
    public PixelTile getTile(int tx, int ty) {
        return tiles.get(genKey(tx,ty));
    }

    //tile space
    public PixelTile createTile(int tx, int ty) {
        PixelTile pt = new PixelTile(tx,ty);
        tiles.put(genKey(tx,ty),pt);
        document.addedTile(tx,ty);
        return pt;
    }

    private String genKey(int tx, int ty) {
        return "key"+tx+","+ty;
    }

    public int getTileCount() {
        return tiles.size();
    }

    public void clearAll() {
        tiles.clear();
    }

    public Collection<PixelTile> getTiles() {
        return tiles.values();
    }

    private static class RepeatPixelGraphics extends PixelGraphics {

        public RepeatPixelGraphics(PixelLayer pixelLayer) {
            super(pixelLayer);
        }

        @Override
        public void fillPixel(int x, int y) {
            super.fillPixel(x%target.document.getRepeatSize(), y%target.document.getRepeatSize());
        }
    }
}

