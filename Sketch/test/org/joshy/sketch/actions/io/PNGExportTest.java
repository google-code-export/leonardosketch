package org.joshy.sketch.actions.io;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.util.u;
import org.joshy.sketch.model.SOval;
import org.joshy.sketch.model.SRect;
import org.joshy.sketch.model.SketchDocument;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Oct 11, 2010
 * Time: 1:51:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class PNGExportTest {
    @Before
    public void setUp() throws Exception {
        try {
            Core.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    @Test
    public void testExporters() {

        try {
            SketchDocument doc = new SketchDocument();

            //test a rectangle
            doc.getCurrentPage().add(new SRect(0, 0, 20, 20).setFillPaint(FlatColor.RED));
            assertTrue(saveAndReadbackPixel(doc,1,1) == 0xffff0000);

            //test an oval
            doc.getCurrentPage().clear();
            doc.getCurrentPage().add(new SOval(0,0,20,20).setFillPaint(new FlatColor(0xFF00FF00)));
            assertTrue(saveAndReadbackPixel(doc,10,10) == 0xFF00FF00);

            //test a polygon
            

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private int saveAndReadbackPixel(SketchDocument doc, int x, int y) throws IOException {
        File file = File.createTempFile("amino.test", ".png");
        u.p("wrote to tempfile: " + file.getAbsolutePath());
        SavePNGAction.export(file,doc);

        BufferedImage image = ImageIO.read(file);
        int rgb = image.getRGB(x,y);
        u.p("read back pixel = " + Integer.toHexString(rgb));
        return rgb;
    }

}
