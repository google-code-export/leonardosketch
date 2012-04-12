package org.joshy.sketch.actions.swatches;

import assetmanager.Asset;
import assetmanager.AssetDB;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.util.ArrayListModel;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 4/7/11
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColorSwatchManager {
    final ArrayListModel<Palette> palettes;
    Palette current;
    private AssetDB db;
    private ArrayListModel<FlatColor> swatchModel;

    public ColorSwatchManager(AssetDB db) {
        this.db = db;
        swatchModel = new ArrayListModel<FlatColor>();
        palettes = new ArrayListModel<Palette>();
        if(db.getAllPalettes().size() < 1) {
            Palette pal = db.createPalette();
            pal.add(FlatColor.BLACK);
            pal.add(FlatColor.WHITE);
            pal.add(FlatColor.RED);
            pal.add(FlatColor.GREEN);
            pal.add(FlatColor.BLUE);
            pal.save();
            pal.setName("Standard Palette");
        }
        palettes.addAll(db.getAllPalettes());
        current = palettes.get(0);
        swatchModel.clear();
        swatchModel.addAll(current.getColors());
    }

    public ArrayListModel<FlatColor> getSwatchModel() {
        return swatchModel;
    }

    public void addSwatch(FlatColor color) {
        current.add(color);
        current.save();
        swatchModel.clear();
        swatchModel.addAll(current.getColors());
    }

}
