package org.joshy.sketch.actions.swatches;

import assetmanager.AssetDB;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.util.ArrayListModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
            pal.setEditable(true);

            Palette pal2 = db.createPalette();
            generateNESPalette(pal2);
            pal2.save();
            pal2.setName("NES");
            pal2.setEditable(false);
        }
        palettes.addAll(db.getAllPalettes());
        current = palettes.get(1);
        swatchModel.clear();
        swatchModel.addAll(current.getColors());
    }

    public ArrayListModel<FlatColor> getSwatchModel() {
        return swatchModel;
    }

    public ArrayListModel<Palette> getPalettes() {
        return palettes;
    }

    public void addSwatch(FlatColor color) {
        current.add(color);
        current.save();
        swatchModel.clear();
        swatchModel.addAll(current.getColors());
    }

    private void generateNESPalette(Palette pal2) {
        try {
            for(String line : toLines(this.getClass().getResourceAsStream("nes.txt"))){
                pal2.add(new FlatColor(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] toLines(InputStream inputStream) throws IOException {
        InputStreamReader reader = new InputStreamReader(inputStream);
        StringBuffer sb = new StringBuffer();
        char[] buff = new char[1024];
        while(true) {
            int n = reader.read(buff);
            if(n < 0) break;
            sb.append(buff,0,n);
        }

        return sb.toString().split("\n");
    }

    public void setCurrentPalette(Palette palette) {
        current = palette;
        swatchModel.clear();
        swatchModel.addAll(current.getColors());
    }
}
