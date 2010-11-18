package org.joshy.sketch.modes.preso;

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
        doc.setPresentation(true);
        doc.setDocBoundsActive(true);
        doc.setUnits(CanvasDocument.LengthUnits.Pixels);
        doc.setWidth(800);
        doc.setHeight(500);
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
