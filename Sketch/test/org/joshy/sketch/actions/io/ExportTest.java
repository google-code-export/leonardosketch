package org.joshy.sketch.actions.io;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.sketch.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Sep 6, 2010
 * Time: 12:21:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExportTest {
    @Before
    public void setUp() throws Exception {
    }
    @Test
    public void testExporters() {
        try {
            Core.init();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        SketchDocument doc = new SketchDocument();
        SketchDocument.SketchPage page = doc.getCurrentPage();
        
        page.add(new SRect(0,0,100,50));
        page.add(new SRect(200,0,50,100));

        SOval o = new SOval(0,0,100,50);
        o.setStrokeWidth(10);
        o.setStrokePaint(FlatColor.RED);
        o.setFillPaint(FlatColor.BLUE);
        page.add(o);

        SPoly poly = new SPoly();
        poly.setTranslateX(50);
        poly.setTranslateY(150);
        poly.addPoint(new Point2D.Double(0,0));
        poly.addPoint(new Point2D.Double(30,30));
        poly.addPoint(new Point2D.Double(0,60));
        poly.addPoint(new Point2D.Double(10,30));
        poly.setFillPaint(FlatColor.GRAY);
        poly.setStrokePaint(FlatColor.BLUE);
        poly.setStrokeWidth(3);
        poly.setClosed(true);
        page.add(poly);


        SText text = new SText();
        text.setFillPaint(FlatColor.PURPLE);
        text.setFontSize(40);
        text.setText("The quick brown fox jumped over the lazy dog!");
        text.setTranslateX(40);
        text.setTranslateY(250);
        page.add(text);

        SPath path = new SPath();
        path.setTranslateX(200);
        path.setTranslateY(200);
        path.addPoint(new SPath.PathPoint(0,0));
        path.addPoint(new SPath.PathPoint(50,0,  30,0,  80,0));
        path.addPoint(new SPath.PathPoint(50,50, 80,50, 30,50));
        path.addPoint(new SPath.PathPoint(0,50));
        path.setClosed(false);
        path.recalcPath();
        path.setStrokePaint(FlatColor.BLACK);
        path.setFillPaint(FlatColor.YELLOW);
        page.add(path);

        SRect rect = new SRect();
        rect.setTranslateX(50);
        rect.setTranslateY(200);
        rect.setWidth(100);
        rect.setHeight(100);
        rect.setFillPaint(FlatColor.WHITE);
        rect.setFillOpacity(0.5);
        page.add(rect);

        SGroup group = new SGroup();
        SRect r1 = new SRect(20,0,20,80);
        SRect r2 = new SRect(0,20,80,20);
        group.addAll(r1,r2);
        group.setTranslateX(300);
        group.setTranslateY(150);
        page.add(group);

        File outdir = new File("testout");
        outdir.mkdirs();
        SavePNGAction.export(new File(outdir,"export.png"),doc);
        SaveSVGAction.export(new File(outdir,"export.svg"),doc);
        SaveHTMLAction.export(new File(outdir,"export_html"),doc);
    }

    @After
    public void tearDown() throws Exception {
    }
}
