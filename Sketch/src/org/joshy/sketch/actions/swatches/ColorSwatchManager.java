package org.joshy.sketch.actions.swatches;

import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import com.joshondesign.xml.XMLParser;
import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.util.ArrayListModel;
import org.joshy.gfx.util.u;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 4/7/11
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColorSwatchManager {
    final ArrayListModel<FlatColor> swatches;
    private File file;

    public ColorSwatchManager(File file) {
        this.file = file;
        swatches = new ArrayListModel<FlatColor>();
        u.p("loading custom swatches");
        if(file.exists()) {
            loadFile(file);
        } else {
            initDummyData();
        }
    }

    private void initDummyData() {
        u.p("initting default color swatches");
        swatches.add(FlatColor.RED);
        swatches.add(FlatColor.GREEN);
        swatches.add(FlatColor.BLUE);
    }

    private void loadFile(File file) {
        u.p("loading color swatches from xml file");
        try {
            Doc doc = XMLParser.parse(file);
            for(Elem c : doc.xpath("/swatches/color")) {
                swatches.add(new FlatColor(c.attr("rgba")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayListModel<FlatColor> getSwatchModel() {
        return swatches;
    }

    public void addSwatch(FlatColor color) {
        swatches.add(color);
        try {
            save();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void save() throws FileNotFoundException, UnsupportedEncodingException {
        XMLWriter xml = new XMLWriter(file).header();
        xml.start("swatches");
        for(FlatColor c : swatches) {
            xml.start("color","rgba",Integer.toHexString(c.getRGBA())).end();
        }
        xml.end();
        xml.close();
        u.p("wrote out swatches to : " + file.getAbsolutePath());
    }
}
