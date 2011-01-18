package org.joshy.sketch.pixel.model;

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

    public PixelLayer() {
        tiles = new HashMap<String,PixelTile>();
        graphics = new PixelGraphics(this);
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
        PixelTile pt = new PixelTile();
        tiles.put(genKey(tx,ty),pt);
        return pt;
    }

    private String genKey(int tx, int ty) {
        return "key"+tx+","+ty;
    }
}
