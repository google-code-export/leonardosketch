package org.joshy.sketch.actions.io;

import org.joshy.gfx.Core;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.OpenAction;
import org.joshy.sketch.canvas.SketchCanvas;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.modes.vector.VectorModeHelper;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

/** A test for the OpenAction
 */
public class OpenActionTest {

    @Before public void doSetup() throws InvocationTargetException, InterruptedException {
        Core.setTesting(true);
        Core.init();
        Core.getShared();
    }

    //do a basic test of loading a page
    @Test public void simpleTest() throws Exception {
        VectorModeHelper helper = new VectorModeHelper(null);
        VectorDocContext context = helper.createDocContext(null);
        SketchCanvas canvas = new SketchCanvas(context);
        OpenAction action = new OpenAction(context);
        u.p("Url = " + this.getClass().getResource("simple.xml"));
        action.load(this.getClass().getResourceAsStream("simple.xml"),null,"simple.xml");
        SketchDocument doc = canvas.getDocument();
        u.p("canvas doc = " + doc);
        for(SketchDocument.SketchPage page : doc.getPages()) {
            u.p("page");
            for(SNode shape : page.getNodes()) {
                u.p("  shape = " + shape);
            }
        }
    }

    //test loading version -1 and auto-upgrading it to version 0
    @Test public void versionN1to0Test() throws Exception {
        u.p("running a version upgrade test");
        VectorModeHelper helper = new VectorModeHelper(null);
        VectorDocContext context = helper.createDocContext(null);
        SketchCanvas canvas = new SketchCanvas(context);
        OpenAction action = new OpenAction(context);
        u.p("Url = " + this.getClass().getResource("oldVersion_-1.xml"));
        action.load(this.getClass().getResourceAsStream("oldVersion_-1.xml"),null,"oldVersion_-1.xml");
        SketchDocument doc = canvas.getDocument();
        u.p("canvas doc = " + doc);
        for(SNode shape : doc.getCurrentPage().getNodes()) {
            u.p("shape = " + shape);
        }
    }
}
