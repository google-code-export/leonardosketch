package org.joshy.sketch.modes.preso;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.NewAction;
import org.joshy.sketch.model.CanvasDocument;
import org.joshy.sketch.model.SketchDocument;

/**
 * Action to create a new presentation document
 */
public class NewPresentationDocAction extends NewAction {

    public NewPresentationDocAction(Main main) {
        super(main);
    }

    @Override
    protected void newDocDialog() {
        //don't do the dialog. instead just create a new doc
        
        SketchDocument doc = new SketchDocument();
        doc.setBackgroundFill(FlatColor.hsb(25,0.05,1.0));
        doc.setPresentation(true);
        doc.setDocBoundsActive(true);
        doc.setUnits(CanvasDocument.LengthUnits.Pixels);
        doc.setGridActive(false);
        doc.setSnapDocBounds(true);
        doc.setSnapGrid(false);
        doc.setSnapNodeBounds(true);

        doc.setWidth(800);
        doc.setHeight(500);

        SwitchTheme.PresoThemeAction theme = new SwitchTheme.Standard(doc, null);
        doc.getProperties().put("theme", theme);
        try {
            theme.execute();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        //insert title page content on the first page
        SketchDocument.SketchPage page = doc.getCurrentPage();
        new PresoModeHelper.AddTitlePage(null).insertContents(page);

        try {
            newDocCreated(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void newDocCreated(SketchDocument doc) throws Exception {
        main.setupNewDoc(new PresoModeHelper(main),doc);
    }

}
