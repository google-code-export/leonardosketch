package org.joshy.sketch.actions.io;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.sketch.actions.OpenAction;
import org.joshy.sketch.model.*;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 12/27/11
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class PathTests {
    @Before
    public void setUp() throws Exception {
        Core.setTesting(true);
        Core.init();
    }

    //create simple path

    @Test
    public void createSimplePath() {
        SPath path1 = new SPath();
        SPath.PathPoint prev1 = path1.moveTo(100, 100);
        path1.curveTo(prev1,  120, 100, 180, 280 ,200,300);
        prev1 = path1.lineTo(100,200);
        path1.close();

        SPath2 path2 = new SPath2();
        SPath2.PathPoint prev2 = path2.moveTo(100, 100);
        path2.curveTo(prev2,  120, 100, 180, 280 ,200,300);
        prev2 = path2.lineTo(100,200);
        path2.closeTo(prev2);
        
        assertTrue(equals(path1,path2));

    }

    //create doughnut path
    @Test
    public void createDonut() {
        SPath path = new SPath();
        path.moveTo(0,0);
        path.lineTo(100,0);
        path.lineTo(100,100);
        path.lineTo(0,100);
        path.close();
        path.newSubPath();
        path.moveTo(20, 20);
        path.lineTo(80,20);
        path.lineTo(80, 80);
        path.lineTo(20, 80);
        path.close();

        assertEquals(path.getSubPaths().size(),2);

        assertEquals(path.getSubPaths().get(0).size(), 4);
        assertTrue(path.getSubPaths().get(0).autoClosed());

        assertEquals(path.getSubPaths().get(1).size(), 4);
        assertTrue(path.getSubPaths().get(1).autoClosed());
    }

    //save and load a new style path with a donut
    @Test
    public void loadSaveDonut() throws Exception {
        SPath path = new SPath();
        path.moveTo(0,0);
        path.lineTo(100,0);
        path.lineTo(100,100);
        path.lineTo(0,100);
        path.close();
        path.newSubPath();
        path.moveTo(20, 20);
        path.lineTo(80,20);
        path.lineTo(80, 80);
        path.lineTo(20, 80);
        path.close();
        
        SketchDocument doc = NativeExportTest.SaveAndLoad(path);
        assertTrue(doc.getPages().get(0).getModel().get(0) instanceof SPath);
        SPath path2 = (SPath) doc.getPages().get(0).getModel().get(0);
        assertEquals(path2.getSubPaths().size(),2);
        assertEquals(path2.getSubPaths().get(0).size(),4);
        assertTrue(path2.getSubPaths().get(0).autoClosed());

    }

    //save an oldstyle path and reload as newstyle
    @Test
    public void testOldFileLoading() throws Exception {
        SketchDocument doc = loadXMLFile("path.xml");
        SNode node = doc.getPages().get(0).getModel().get(0);
        assertTrue(node instanceof SPath);
        SPath path = (SPath) node;
        assertTrue(path.getSubPaths().size() == 1);
        assertTrue(path.getSubPaths().get(0).size() == 6);
    }


    //create crossing path
    //load an SVG file path
    //load an SVG file path with a donut
    //duplicate a path
    //duplicate a path with a subpath
    //add a point then undo it
    //close a subpath, then undo it
    //add a point then delete it
    //create a donut area then convert to a path


    /* util code */
    private static SketchDocument loadXMLFile(String s) throws Exception {
        SketchDocument doc = OpenAction.load(PathTests.class.getResourceAsStream(s), null, s, null);
        return doc;

    }

    private static boolean equals(SPath path1, SPath2 path2) {
        if(path1.getSubPaths().size() == 1) {
            if(path1.getSubPaths().get(0).size() == path2.getPoints().size()) {
                return true;
            }
        }
        return false;
    }
}
