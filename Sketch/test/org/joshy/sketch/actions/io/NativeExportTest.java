package org.joshy.sketch.actions.io;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.OpenAction;
import org.joshy.sketch.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

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

    @After
    public void tearDown() throws Exception {
    }
}
