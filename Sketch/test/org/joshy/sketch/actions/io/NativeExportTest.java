package org.joshy.sketch.actions.io;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.Core;
import org.joshy.gfx.draw.*;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.OpenAction;
import org.joshy.sketch.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 7, 2010
 * Time: 2:37:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class NativeExportTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void exportOldFormat() {
        SketchDocument doc = new SketchDocument();
        doc.getCurrentPage().model.add(new SRect(0,0,100,50));

        NGon ngon = new NGon(5);
        ngon.setRadius(20);
        ngon.setFillPaint(FlatColor.BLUE);
        ngon.setStringProperty("foo","bar");
        
        SGroup group = new SGroup();
        group.addAll(new SOval(),ngon);
        doc.getCurrentPage().model.add(group);
        
        XMLWriter out = new XMLWriter(new PrintWriter(System.out),null);
        ExportProcessor.process(new NativeExport(), out, doc);
        out.close();
    }
    @Test
    public void exportLeozFormat() throws Exception {
        SketchDocument doc = new SketchDocument();
        doc.getCurrentPage().model.add(new SRect(0,0,100,50));
        NGon ngon = new NGon(5);
        ngon.setRadius(20);
        ngon.setFillPaint(FlatColor.BLUE);
        ngon.setStringProperty("foo","bar");
        SGroup group = new SGroup();
        group.addAll(new SOval(),ngon);
        doc.getCurrentPage().model.add(group);

        File file = File.createTempFile("nativeExportTest",".leoz");
        u.p("writing test to file: " + file.getAbsolutePath());
        SaveAction.saveAsZip(
                new FileOutputStream(file)
                ,file.getName()
                ,file.toURI()
                ,doc
        );

        SketchDocument doc2 = OpenAction.loadZip(file);
        assertTrue(doc2.getPages().get(0).model.get(0) instanceof SRect);
    }
    @Test
    public void exportLeozWithImage() throws Exception {
        Core.setTesting(true);
        Core.init();
        SketchDocument doc = new SketchDocument();
        doc.getCurrentPage().model.add(new SRect(0,0,100,50));
        SImage image = new SImage(NativeExportTest.class.getResource("redrect.png"),"redrect.png");
        doc.getCurrentPage().model.add(image);
        File file = File.createTempFile("nativeExportTest",".leoz");
        u.p("writing test to file: " + file.getAbsolutePath());
        SaveAction.saveAsZip(
                new FileOutputStream(file)
                ,file.getName()
                ,file.toURI()
                ,doc
        );
        SketchDocument doc2 = OpenAction.loadZip(file);
        assertTrue(doc2.getPages().get(0).model.get(0) instanceof SRect);
        SNode node = doc2.getPages().get(0).model.get(1);
        assertTrue(node instanceof SImage);
        SImage img = (SImage) node;
        u.p("relative url = " + img.getRelativeURL());
        assertTrue(img.getRelativeURL().equals("redrect.png"));
        assertTrue(img.getBufferedImage().getWidth() == 101);
    }

    @Test
    public void exportWithGradients() throws Exception {
        Core.setTesting(true);
        Core.init();
        SketchDocument doc = new SketchDocument();


        MultiGradientFill grad1 = new LinearGradientFill()
                .setStartX(0).setEndX(100).setStartY(0).setEndY(0)
                .addStop(0, FlatColor.RED)
                .addStop(0.75, FlatColor.PURPLE)
                .addStop(1.0, FlatColor.WHITE);
        doc.getCurrentPage().model.add(new SRect(0,0,100,50).setFillPaint(grad1));

        MultiGradientFill grad2 = new RadialGradientFill()
                .setCenterX(50).setCenterY(50).setRadius(100)
                .addStop(0, FlatColor.RED)
                .addStop(0.75, FlatColor.YELLOW)
                .addStop(1.0, FlatColor.WHITE);
        doc.getCurrentPage().model.add(new NGon(6).setRadius(80).setFillPaint(grad2));

        File file = File.createTempFile("nativeExportTest",".leoz");
        u.p("writing test to file: " + file.getAbsolutePath());
        SaveAction.saveAsZip(
                new FileOutputStream(file)
                ,file.getName()
                ,file.toURI()
                ,doc
        );
        SketchDocument doc2 = OpenAction.loadZip(file);

        assertTrue(doc2.getPages().get(0).model.get(0) instanceof SRect);
        SRect loadedRect = (SRect) doc2.getPages().get(0).model.get(0);
        assertTrue(loadedRect.getFillPaint() instanceof LinearGradientFill);
        LinearGradientFill loadedLinGrad = (LinearGradientFill) loadedRect.getFillPaint();
        assertTrue(loadedLinGrad.getEndX() == 100);
        assertTrue(loadedLinGrad.getStops().size() == 3);
        assertTrue(loadedLinGrad.getStops().get(1).getColor().getRed() == FlatColor.PURPLE.getRed());

        assertTrue(doc2.getPages().get(0).model.get(1) instanceof NGon);
        NGon loadedNGon = (NGon) doc2.getPages().get(0).model.get(1);
        assertTrue(loadedNGon.getFillPaint() instanceof RadialGradientFill);
        RadialGradientFill loadedRadGrad = (RadialGradientFill) loadedNGon.getFillPaint();
        assertTrue(loadedRadGrad.getCenterX() == 50);
        assertTrue(loadedRadGrad.getRadius() == 100);
        assertTrue(loadedRadGrad.getStops().size() == 3);
        assertTrue(loadedRadGrad.getStops().get(1).getColor().getRed() == FlatColor.YELLOW.getRed());
    }

    @Test
    public void exportWithEffects() throws Exception {
        Core.setTesting(true);
        Core.init();
        SketchDocument doc = new SketchDocument();

        doc.getCurrentPage().model.add(new SRect(0,0,100,50)
                .setShadow(new DropShadow()
                        .setBlurRadius(8)
                        .setOpacity(0.6)
                        .setColor(FlatColor.RED)
                        .setXOffset(12.6)
                        .setYOffset(-8.9)
                ));

        File file = File.createTempFile("nativeExportTest",".leoz");
        u.p("writing test to file: " + file.getAbsolutePath());
        SaveAction.saveAsZip(
                new FileOutputStream(file)
                ,file.getName()
                ,file.toURI()
                ,doc
        );
        SketchDocument doc2 = OpenAction.loadZip(file);

        assertTrue(doc2.getPages().get(0).model.get(0) instanceof SRect);
        SRect loadedRect = (SRect) doc2.getPages().get(0).model.get(0);
        DropShadow shadow = loadedRect.getShadow();
        assertTrue(shadow != null);
        assertTrue(shadow.getBlurRadius() == 8);
        assertTrue(shadow.getColor().getRGBA() == FlatColor.RED.getRGBA());
        assertTrue(shadow.getOpacity() == 0.6);
        assertTrue(shadow.getXOffset() == 12.6);
        assertTrue(shadow.getYOffset() == -8.9);
    }

    @Test
    public void exportTexture() throws Exception {
        Core.setTesting(true);
        Core.init();
        SketchDocument doc = new SketchDocument();

        URL url = NativeExportTest.class.getResource("redrect.png");
        doc.getCurrentPage().model.add(new SRect(0, 0, 100, 50)
                .setFillPaint(
                        PatternPaint.create(url,"redrect.png").deriveNewEnd(new Point2D.Double(100, 100))
                )
        );

        File file = File.createTempFile("nativeExportTest",".leoz");
        u.p("writing test to file: " + file.getAbsolutePath());
        SaveAction.saveAsZip(
                new FileOutputStream(file)
                ,file.getName()
                ,file.toURI()
                ,doc
        );
        SketchDocument doc2 = OpenAction.loadZip(file);
        assertTrue(doc2.getPages().get(0).model.get(0) instanceof SRect);
        SRect loadedRect = (SRect) doc2.getPages().get(0).model.get(0);
        assertTrue(loadedRect.getFillPaint() instanceof PatternPaint);
        PatternPaint pp = (PatternPaint) loadedRect.getFillPaint();
        assertTrue(pp.getEnd().getY() == 100);
        assertTrue(pp.getImage().getWidth() == 101);

    }
    @After
    public void tearDown() throws Exception {
    }
}
