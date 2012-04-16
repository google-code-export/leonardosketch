package org.joshy.sketch.actions.swatches;

import assetmanager.Asset;
import assetmanager.AssetDB;
import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import com.joshondesign.xml.XMLParser;
import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.PatternPaint;
import org.joshy.gfx.node.control.ListModel;
import org.joshy.gfx.util.ArrayListModel;
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;

import javax.imageio.ImageIO;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 4/7/11
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatternManager {
    private ArrayListModel<PatternPaint> patternModel = new ArrayListModel<PatternPaint>();
    private AssetDB db;

    public PatternManager(AssetDB db) {
        this.db = db;
        if(db.getAllPatterns().size() < 1) {
            String[] pats = new String[]{"brown_noise.png","checkerboard.png","flurdelis.png","tess01.png","wallpaper1.png"};
            for(String pat : pats) {
                u.p("adding prefab pattern: " + pat);
                InputStream stream = Main.class.getResourceAsStream("resources/textures/" + pat);
                try {
                    db.copyAndAddPattern(stream,pat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for(Asset pat : db.getAllPatterns()) {
            try {
                patternModel.add(PatternPaint.create(pat.getFile()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public ListModel getModel() {
        return (ListModel)this.patternModel;
    }

    public void addPattern(PatternPaint pat) {
        patternModel.add(pat);
        try {
            File file = File.createTempFile("foo", "png");
            ImageIO.write(pat.getImage(),"png",file);
            Asset asset = db.copyAndAddPattern(file);
            asset.setName("generated pattern");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
