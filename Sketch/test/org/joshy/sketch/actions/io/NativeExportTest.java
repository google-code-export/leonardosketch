package org.joshy.sketch.actions.io;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.*;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.BooleanGeometry;
import org.joshy.sketch.actions.OpenAction;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import static org.junit.Assert.assertFalse;
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
    /*
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
    */
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
    public void exportNGon() throws Exception {
        Core.setTesting(true);
        Core.init();
        SketchDocument doc = new SketchDocument();
        VectorDocContext ctx = new VectorDocContext(null,null);
        ctx.setDocument(doc);

        SShape a = new NGon(6).setRadius(100).setInnerRadius(50).setStar(true);
        doc.getCurrentPage().model.add(a);
        SShape b = new NGon(6).setRadius(100).setInnerRadius(50).setStar(false);
        doc.getCurrentPage().model.add(b);

        File file = File.createTempFile("nativeExportTest",".leoz");
        u.p("writing test to file: " + file.getAbsolutePath());
        SaveAction.saveAsZip(file, doc);
        SketchDocument doc2 = OpenAction.loadZip(file);
        assertTrue(doc2.getPages().get(0).model.get(0) instanceof NGon);
        assertTrue(doc2.getPages().get(0).model.get(1) instanceof NGon);
        NGon na = (NGon) doc2.getPages().get(0).model.get(0);
        assertTrue(na.isStar());
        assertTrue(na.getInnerRadius() == 50);
        NGon nb = (NGon) doc2.getPages().get(0).model.get(1);
        assertFalse(nb.isStar());
    }

    @Test
    public void exportArea() throws Exception {
        Core.setTesting(true);
        Core.init();
        SketchDocument doc = new SketchDocument();
        VectorDocContext ctx = new VectorDocContext(null,null);
        ctx.setDocument(doc);

        SShape a = new NGon(6).setRadius(100).setFillPaint(FlatColor.RED);
        doc.getCurrentPage().model.add(a);

        SShape b = new SOval(20,20,100,100).setFillPaint(FlatColor.BLUE);
        doc.getCurrentPage().model.add(b);

        ctx.getSelection().addSelectedNode(a);
        ctx.getSelection().addSelectedNode(b);
        BooleanGeometry.Union union = new BooleanGeometry.Union(ctx);
        union.execute();

        //make sure we have just an sarea
        assertTrue(doc.getPages().get(0).model.get(0) instanceof SArea);

        //save
        File file = File.createTempFile("nativeExportTest",".leoz");
        u.p("writing test to file: " + file.getAbsolutePath());
        SaveAction.saveAsZip(file, doc);

        //reopen
        SketchDocument doc2 = OpenAction.loadZip(file);

        //make sure we still have just an sarea
        assertTrue(doc2.getPages().get(0).model.get(0) instanceof SArea);
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
    public void exportText() throws Exception {
        Core.setTesting(true);
        Core.init();
        SketchDocument doc = new SketchDocument();
        doc.setGridActive(false);
        SText text = new SText();
        text.setText("foo");
        text.setHalign(SText.HAlign.Center);
        text.setFontSize(36);
        text.setAutoSize(false);
        text.setWidth(300);
        text.setBulleted(true);
        doc.getCurrentPage().model.add(text);
        File file = File.createTempFile("nativeExportTest",".leoz");
        u.p("writing test to file: " + file.getAbsolutePath());
        SaveAction.saveAsZip(
                new FileOutputStream(file)
                , file.getName()
                , file.toURI()
                , doc
        );
        SketchDocument doc2 = OpenAction.loadZip(file);
        assertTrue(doc2.getPages().get(0).model.get(0) instanceof SText);
        assertTrue(doc2.isGridActive() == false);
        SText text2 = (SText) doc2.getPages().get(0).model.get(0);
        assertTrue(text2.getText().equals("foo"));
        assertTrue(text2.getHalign() == SText.HAlign.Center);
        assertTrue(text2.getFontSize() == 36);
        assertTrue(text2.getWidth() == 300);
        assertTrue(text2.isAutoSize() == false);
        assertTrue(text2.isBulleted() == true);

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

    @Test
    public void testRectangleOpacity() throws Exception {
        Core.setTesting(true);
        Core.init();
        SketchDocument doc = new SketchDocument();
        SRect sRect = new SRect(0,0,100,50);
        sRect.setFillPaint(FlatColor.BLUE);
        sRect.setFillOpacity(0.5);
        doc.getCurrentPage().model.add(sRect);
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
        u.p("opacity = " + loadedRect.getFillOpacity());
        assertTrue("fill opacity wrong for loaded rect", loadedRect.getFillOpacity() == 0.5);

    }

    @Test
    public void testText() throws Exception {
        Core.setTesting(true);
        Core.init();
        SketchDocument doc = new SketchDocument();
        SText text = new SText();
        text.setFontName("Chunk Five");
        text.setText("this is some\n long text");
        doc.getCurrentPage().add(text);
        File file = File.createTempFile("nativeExportTest",".leoz");
        u.p("writing to test file: " + file.getAbsolutePath());
        SaveAction.saveAsZip(file,doc);
        SketchDocument doc2 = OpenAction.loadZip(file);
        assertTrue(doc2.getPages().get(0).model.get(0) instanceof SText);
        SText text2 = (SText) doc2.getPages().get(0).model.get(0);
        u.p("text is really: " + text2.getText());
        assertTrue(text2.getText().equals("this is some\n long text"));
        assertTrue(text2.getFontName().equals("Chunk Five"));

    }

    @Test
    public void testPathSaving() throws Exception {
        Core.setTesting(true);
        Core.init();
        SPath path = new SPath();
        SPath.PathPoint first = path.moveTo(0,100);
        path.lineTo(100,0);
        path.lineTo(130,100);
        SPath.PathPoint last = path.closeTo(first);
        assertTrue(path.isClosed());
        assertTrue(last.closePath);

        SketchDocument doc = SaveAndLoad(path);

        assertTrue(doc.getPages().get(0).model.get(0) instanceof SPath);
        SPath path2 = (SPath) doc.getPages().get(0).model.get(0);
        assertTrue(path2.getPoints().size()==3);
        SPath.PathPoint last2 = path2.getPoints().get(2);
        assertTrue(path2.isClosed());
        assertTrue(last2.closePath);
    }

    @Test
    public void testTransformSaving() throws Exception {
        Core.setTesting(true);
        Core.init();
        SOval o1 = new SOval();
        o1.setX(10);
        o1.setY(20);
        o1.setWidth(100);
        o1.setHeight(50);
        o1.setTranslateX(1);
        o1.setTranslateY(2);
        o1.setAnchorX(3);
        o1.setAnchorY(4);
        o1.setRotate(5);
        o1.setScaleX(6);
        o1.setScaleY(7);

        SketchDocument doc = SaveAndLoad(o1);
        assertTrue(doc.getPages().get(0).model.get(0) instanceof SOval);
        SOval o2 = (SOval) doc.getPages().get(0).model.get(0);
        assertTrue(o2.getX() == o1.getX());
        assertTrue(o2.getY() == o1.getY());
        assertTrue(o2.getWidth() == o1.getWidth());
        assertTrue(o2.getHeight() == o1.getHeight());
        assertTrue(o2.getTranslateX() == o1.getTranslateX());
        assertTrue(o2.getTranslateY() == o1.getTranslateY());
        assertTrue(o2.getAnchorX() == o1.getAnchorX());
        assertTrue(o2.getAnchorY() == o1.getAnchorY());
        assertTrue(o2.getRotate() == o1.getRotate());
        assertTrue(o2.getScaleX() == o1.getScaleX());
        assertTrue(o2.getScaleY() == o1.getScaleY());

    }

    private SketchDocument SaveAndLoad(SNode path) throws Exception {
        SketchDocument doc = new SketchDocument();
        doc.getCurrentPage().add(path);
        File file = File.createTempFile("nativeExportTest",".leoz");
        u.p("writing to test file: " + file.getAbsolutePath());
        SaveAction.saveAsZip(file,doc);
        SketchDocument doc2 = OpenAction.loadZip(file);
        return doc2;
    }

    @Test
    public void testDocumentBackground() throws Exception {
        Core.setTesting(true);
        Core.init();
        SketchDocument doc = new SketchDocument();
        PatternPaint patternPaint = PatternPaint.create(NativeExportTest.class.getResource("redrect.png"),"foo");
        doc.setBackgroundFill(patternPaint);
        File file = File.createTempFile("nativeExportTest",".leoz");
        u.p("writing test to file: " + file.getAbsolutePath());
        SaveAction.saveAsZip(
                new FileOutputStream(file)
                , file.getName()
                , file.toURI()
                , doc
        );
        SketchDocument doc2 = OpenAction.loadZip(file);
    }


    @After
    public void tearDown() throws Exception {
    }
}
