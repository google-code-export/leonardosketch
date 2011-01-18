package org.joshy.sketch.pixel.model;

import org.joshy.gfx.draw.FlatColor;
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

    public PixelDoc() {
        layers = new ArrayList<PixelLayer>();
        PixelLayer layer1 = new PixelLayer();
        add(layer1);
        PixelGraphics g = layer1.getGraphics();
        g.setFill(FlatColor.PURPLE);
        g.fillRect(200,100,100,100); // this should span one tile to the right

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
}
