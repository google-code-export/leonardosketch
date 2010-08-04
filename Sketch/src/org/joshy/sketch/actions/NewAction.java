package org.joshy.sketch.actions;

import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.HBox;
import org.joshy.gfx.node.layout.VBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.sketch.Main;
import org.joshy.sketch.model.CanvasDocument;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorModeHelper;

public class NewAction extends SAction {
    protected Main main;

    public NewAction(Main main) {
        super();
        this.main = main;
    }

    @Override
    public void execute() {
        /*
        if(context.getDocument().isDirty()) {
            StandardDialog.Result result = StandardDialog.showYesNoCancel(
                    "This document hasn't been saved yet. Save?",
                    "Save","Don't Save","Cancel");
            if(result== StandardDialog.Result.Yes) {
                new SaveAction(context,false).execute();
                newDocDialog();
            }
            if(result==StandardDialog.Result.No) {
                newDocDialog();
            }
            if(result==StandardDialog.Result.Cancel) {
                //do nothing
            }
        } else {*/
            newDocDialog();
        //}
    }

    protected void newDocDialog() {
        /*
                let you choose from some presets
                set the size of the doc in pixels
                hit the cancel button
                hit the okay button
                 */

        final Stage dialog = Stage.createStage();
        dialog.setTitle("New Document");

            final Textbox width = new Textbox("800");
            final Textbox height = new Textbox("500");
            Callback<ActionEvent> canceled = new Callback<ActionEvent>() {
                public void call(ActionEvent event) {
                    dialog.hide();
                }
            };
            Callback<ActionEvent> okay = new Callback<ActionEvent>() {
                public void call(ActionEvent event) {
                    dialog.hide();
                    double dwidth = Double.parseDouble(width.getText());
                    double dheight = Double.parseDouble(height.getText());
                    SketchDocument doc = new SketchDocument();
                    doc.setDocBoundsActive(true);
                    doc.setUnits(CanvasDocument.LengthUnits.Pixels);
                    doc.setWidth(dwidth);
                    doc.setHeight(dheight);
                    try {
                        newDocCreated(doc);
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            };


            final PopupMenuButton popup = new PopupMenuButton();
            popup.setModel(ListView.createModel(new String[]{"16x16","1024x1024"}));
            Callback<ActionEvent> clicked = new Callback<ActionEvent>() {
                public void call(ActionEvent event) {
                    switch(popup.getSelectedIndex()) {
                        case 0: width.setText("16"); height.setText("16"); break;
                        case 1: width.setText("1024"); height.setText("768"); break;
                    }
                }
            };
            popup.onClicked(clicked);
            dialog.setContent(new VBox().add(
                new HBox().add(new Label("Preset:"),popup),
                new HBox().add(new Label("Width (px):"), width),
                new HBox().add(new Label("Height (px):"), height),
                new HBox().add(new Button("Cancel").onClicked(canceled), new Button("Okay").onClicked(okay))
            ));
        dialog.setWidth(400);
        dialog.setHeight(400);
    }

    protected void newDocCreated(SketchDocument doc) throws Exception {
        main.setupNewDoc(new VectorModeHelper(main),doc);
        //context.getMain().setupNewDoc(new VectorModeHelper(context.getMain()),doc);
    }

}
