package org.joshy.sketch.actions;

import org.joshy.sketch.Main;
import org.joshy.sketch.modes.pixel.PixelModeHelper;
import org.joshy.sketch.pixel.model.PixelDoc;

/** Action to create a new pixel document
 */
public class NewPixelDocAction extends NewAction {
    int defaultWidth = 1024;
    int defaultHeight = 768;

    public NewPixelDocAction(Main main) {
        super(main);
    }

    @Override
    protected void newDocDialog() {
        //don't do the dialog. instead just create a new doc
        PixelDoc doc = new PixelDoc();//(defaultWidth, defaultHeight);
        try {
            main.setupNewDoc(new PixelModeHelper(main),doc);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
