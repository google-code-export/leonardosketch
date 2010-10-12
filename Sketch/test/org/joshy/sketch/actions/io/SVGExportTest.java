package org.joshy.sketch.actions.io;

import com.joshondesign.xml.Doc;
import com.joshondesign.xml.XMLParser;
import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.util.u;
import org.joshy.sketch.model.*;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Oct 11, 2010
 * Time: 4:36:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class SVGExportTest {
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

    @Test
    public void testRectangle() throws Exception {
        page.clear();
        page.add(new SRect(0, 0, 20, 20).setFillPaint(FlatColor.RED));
        Doc xdoc = saveAndReadback(doc);
        assertTrue("0.0".equals(xdoc.xpathString("/svg/rect/@x")));
        assertTrue("1.0".equals(xdoc.xpathString("/svg/rect/@stroke-width")));
    }

    @Test public void testOval() throws Exception {
        page.clear();
        page.add(new SOval(0, 0, 20, 20).setFillPaint(FlatColor.RED));
        Doc xdoc = saveAndReadback(doc);
        assertTrue("10.0".equals(xdoc.xpathString("/svg/ellipse/@cx")));
        assertTrue("10.0".equals(xdoc.xpathString("/svg/ellipse/@ry")));
        assertTrue("1.0".equals(xdoc.xpathString("/svg/ellipse/@stroke-width")));
    }

    @Test public void testPolygon() throws Exception {
        //test a polygon
        SPoly poly = new SPoly();
        poly.setFillPaint(FlatColor.BLUE);
        poly.addPoint(new Point(0,0));
        poly.addPoint(new Point(10,0));
        poly.addPoint(new Point(0,10));
        poly.setClosed(true);
        page.clear();
        page.add(poly);
        Doc xdoc = saveAndReadback(doc);
        assertTrue("0.0,0.0 10.0,0.0 0.0,10.0 ".equals(xdoc.xpathString("/svg/polygon/@points")));        
    }

    @Test public void testNGon() throws Exception {
        NGon ngon = new NGon();
        ngon.setRadius(20);
        ngon.setFillPaint(FlatColor.RED);
        ngon.setSides(5);
        ngon.setTranslateX(100);
        ngon.setTranslateY(100);
        page.clear();
        page.add(ngon);
        Doc xdoc = saveAndReadback(doc);
        assertTrue("120.0,100.0 106.0,119.0 84.0,112.0 84.0,88.0 106.0,81.0 ".equals(xdoc.xpathString("/svg/polygon/@points")));        
    }

    @Test public void testPath() throws Exception {
        SPath path = new SPath();
        path.setFillPaint(FlatColor.RED);
        path.addPoint(new SPath.PathPoint(0,0, -5,0, 5,0));
        path.addPoint(new SPath.PathPoint(10,10, 10,5, 10,15));
        path.addPoint(new SPath.PathPoint(0,20,  5,20, 0,15));
        path.setClosed(true);
        page.clear();
        page.add(path);
        Doc xdoc = saveAndReadback(doc);
        assertTrue(" M 0.0 0.0  C 5.0 0.0 10.0 5.0 10.0 10.0   C 10.0 15.0 5.0 20.0 0.0 20.0   z".equals(xdoc.xpathString("/svg/path/@d")));
    }
    
    @Test public void testText() throws Exception {
        SText text = new SText();
        text.setText("ABC");
        text.setFontSize(100);
        text.setFillPaint(FlatColor.GREEN);
        page.clear();
        page.add(text);
        Doc xdoc = saveAndReadback(doc);
        assertTrue("ABC".equals(xdoc.xpathString("/svg/text/text()")));
    }


    @Test public void testImage() throws IOException {

    }


    @Test public void testGroup() throws Exception {
        page.clear();

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
        page.add(group);
        
        SRect rect2 = new SRect(0,0,150,150);
        rect2.setFillPaint(FlatColor.GREEN);
        page.add(rect2);
        
        Doc xdoc = saveAndReadback(doc);
        assertTrue("translate(50.0,50.0)".equals(xdoc.xpathString("/svg/g/@transform")));
        assertTrue("10.0".equals(xdoc.xpathString("/svg/g/rect/@width")));
        assertTrue("1.0".equals(xdoc.xpathString("/svg/g/ellipse/@stroke-width")));
        assertTrue("150.0".equals(xdoc.xpathString("/svg/rect/@width")));
    }


    @Test public void testArea() throws IOException {

    }
    @Test public void testArrow() {

    }

    @Test public void testGradientRect() throws IOException {

    }

    private Doc saveAndReadback(SketchDocument doc) throws Exception {
        File file = File.createTempFile("amino.test", ".svg");
        u.p("wrote to tempfile: " + file.getAbsolutePath());
        SaveSVGAction.export(file,doc);
        Doc xdoc = XMLParser.parse(file);
        return xdoc;
    }

}
