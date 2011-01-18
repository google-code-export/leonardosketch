package org.joshy.sketch.actions.io.svg;

import org.joshy.sketch.actions.ImportAction;
import org.joshy.sketch.model.SketchDocument;
import org.junit.Test;

import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Dec 3, 2010
 * Time: 8:15:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class SVGImportTest {
    @Test
    public void loadTest1() throws Exception {
        URL url = SVGImportTest.class.getResource("import_01.svg");
        SketchDocument doc = ImportAction.importSVG(url);
    }
}
