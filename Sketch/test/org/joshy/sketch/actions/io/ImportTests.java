package org.joshy.sketch.actions.io;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ImportAction;
import org.joshy.sketch.model.SGroup;
import org.joshy.sketch.model.SPoly;
import org.joshy.sketch.model.SketchDocument;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Nov 16, 2010
 * Time: 5:48:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImportTests {


    @Test
    public void importSVG() throws Exception {
        URL url = ImportTests.class.getResource("test1.svg");
        SketchDocument doc = ImportAction.importSVG(url);
        //check that the first node is a group
        assertTrue(doc.getPages().get(0).model.get(0) instanceof SGroup);
        //go down the tree to the polygon
        SGroup g1 = (SGroup) doc.getPages().get(0).model.get(0);
        SGroup g2 = (SGroup) g1.getNodes().get(1);
        SPoly p1 = (SPoly) g2.getNodes().get(0);
        //test the color of the polygon
        assertTrue(p1.getFillPaint() instanceof FlatColor);
        FlatColor c1 = (FlatColor) p1.getFillPaint();
        assertTrue(c1.getRGBA() == 0xFF231F20);
    }
}
