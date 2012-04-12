package org.joshy.sketch.actions.swatches;

import assetmanager.Asset;
import assetmanager.AssetDB;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.util.ArrayListModel;
import org.joshy.gfx.util.u;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: josh
 * Date: 4/12/12
 * Time: 1:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class Palette extends Asset {
    private List<FlatColor> swatches;

    public Palette(AssetDB assetDB, Node asset) {
        super(assetDB, asset);
        this.swatches = new ArrayList<FlatColor>();
    }

    public void add(FlatColor black) {
        this.swatches.add(black);
    }

    public List<FlatColor> getColors() {
        return swatches;
    }

    public void save() {
        Transaction tx = this.db.beginTx();
        try {

            StringBuffer sb = new StringBuffer();
            for(FlatColor color : swatches) {
                sb.append(Integer.toHexString(color.getRGBA()));
                sb.append(",");
            }
            u.p("writing to string: " + sb.toString());
            this.node.setProperty("colors",sb.toString());
            tx.success();
        } finally {
            tx.finish();
        }
    }

    public void load() {
        String stringcolors = (String) this.node.getProperty("colors");
        String[] colors = stringcolors.split(",");
        swatches.clear();
        for(String c : colors) {
            FlatColor color = new FlatColor(c);
            u.p("flat color = " + Integer.toHexString(color.getRGBA()));
            swatches.add(color);
        }
    }
}
