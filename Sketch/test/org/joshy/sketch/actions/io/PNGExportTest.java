package org.joshy.sketch.actions.io;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.LinearGradientFill;
import org.joshy.gfx.draw.MultiGradientFill;
import org.joshy.gfx.util.u;
import org.joshy.sketch.model.*;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Oct 11, 2010
 * Time: 1:51:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class PNGExportTest {
    private SketchDocument doc;
    private SketchDocument.SketchPage page;

    @Before
    public void setUp() throws Exception {
        try {
            Core.init();
            doc = new SketchDocument();
            page = doc.getCurrentPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test public void testRectangle() throws IOException {
        page.clear();
        page.add(new SRect(0, 0, 20, 20).setFillPaint(FlatColor.RED));
        assertTrue(saveAndReadbackPixel(doc,1,1) == 0xffff0000);        
    }

    @Test public void testOval() throws IOException {
        page.clear();
        page.add(new SOval(0,0,20,20).setFillPaint(FlatColor.GREEN));
        assertTrue(saveAndReadbackPixel(doc,10,10) == 0xFF00FF00);
    }

    @Test public void testPolygon() throws IOException {
        //test a polygon
        SPoly poly = new SPoly();
        poly.setFillPaint(FlatColor.BLUE);
        poly.addPoint(new Point(0,0));
        poly.addPoint(new Point(10,0));
        poly.addPoint(new Point(0,10));
        poly.setClosed(true);
        page.clear();
        page.add(poly);
        assertTrue(saveAndReadbackPixel(doc,5,3) == 0xFF0000FF);
    }

    @Test public void testNGon() throws IOException {
        NGon ngon = new NGon();
        ngon.setRadius(20);
        ngon.setFillPaint(FlatColor.RED);
        ngon.setSides(5);
        page.clear();
        page.add(ngon);
        assertTrue(saveAndReadbackPixel(doc,20,20) == 0xFFFF0000);
    }


    @Test public void testPath() throws IOException {
        SPath path = new SPath();
        path.setFillPaint(FlatColor.RED);
        path.addPoint(new SPath.PathPoint(0,0, -5,0, 5,0));
        path.addPoint(new SPath.PathPoint(10,10, 10,5, 10,15));
        path.addPoint(new SPath.PathPoint(0,20,  5,20, 0,15));
        path.setClosed(true);
        page.clear();
        page.add(path);
        assertTrue(saveAndReadbackPixel(doc,5,10) == 0xFFFF0000);
    }

    @Test public void testText() throws IOException {
        SText text = new SText();
        text.setText("ABC");
        text.setFontSize(100);
        text.setFillPaint(FlatColor.GREEN);
        page.clear();
        page.add(text);
        assertTrue(saveAndReadbackPixel(doc, 110,27) == 0xff00ff00);
    }

    @Test public void testImage() throws IOException {
        URL url = this.getClass().getResource("redrect.png");
        SImage image = new SImage(url,"redrect.png");
        page.clear();
        page.add(image);
        assertTrue(saveAndReadbackPixel(doc,10,10) == 0xffff0000);
    }

    @Test public void testGroup() throws IOException {
        SRect rect = new SRect(0,0,10,10);
        rect.setFillPaint(FlatColor.RED);
        SOval oval = new SOval(0,0,10,10);
        oval.setFillPaint(FlatColor.BLUE);
        rect.setTranslateX(100);
        oval.setTranslateY(100);
        SGroup group = new SGroup();
        group.addAll(rect,oval);
        group.setTranslateX(50);
        group.setTranslateY(50);
        page.clear();
        page.add(group);
        SRect rect2 = new SRect(0,0,10,10);
        rect2.setFillPaint(FlatColor.GREEN);
        page.add(rect2);
        assertTrue(saveAndReadbackPixel(doc,5,5) == 0xff00ff00);
        assertTrue(saveAndReadbackPixel(doc,155,55) == 0xffff0000);
        assertTrue(saveAndReadbackPixel(doc,55,155) == 0xff0000ff);
    }
    
    @Test public void testArea() throws IOException {
        SRect rect = new SRect(0,0,10,10);
        rect.setFillPaint(FlatColor.RED);
        SOval oval = new SOval(5,5,10,10);
        oval.setFillPaint(FlatColor.BLUE);
        Area area = new Area();
        area.add(rect.toArea());
        area.add(oval.toArea());
        SArea sarea = new SArea(area);
        sarea.setTranslateX(10);
        sarea.setTranslateY(10);
        sarea.setFillPaint(FlatColor.GREEN);
        page.clear();
        page.add(sarea);
        assertTrue(saveAndReadbackPixel(doc,7,7) == 0xFF00FF00);
    }

    @Test public void testArrow() {

    }
    
    @Test public void testGradientRect() throws IOException {
        MultiGradientFill grad = new LinearGradientFill()
                .setStartX(3).setStartY(0).setEndX(97).setEndY(0)
                .addStop(0, FlatColor.BLUE)
                .addStop(1, FlatColor.RED);
        SRect rect = new SRect(0,0,100,100);
        rect.setFillPaint(grad);
        rect.setStrokeWidth(0);
        page.clear();
        page.add(rect);
        assertTrue(saveAndReadbackPixel(doc,0,0) == 0xFF0000FF);
        assertTrue(saveAndReadbackPixel(doc,99,0) == 0xFFFF0000);
    }


    private int saveAndReadbackPixel(SketchDocument doc, int x, int y) throws IOException {
        File file = File.createTempFile("amino.test", ".png");
        u.p("wrote to tempfile: " + file.getAbsolutePath());
        SavePNGAction.exportStatic(file, (CanvasDocument) doc);

        BufferedImage image = ImageIO.read(file);
        int rgb = image.getRGB(x,y);
        u.p("read back pixel = " + Integer.toHexString(rgb));
        return rgb;
    }

}
