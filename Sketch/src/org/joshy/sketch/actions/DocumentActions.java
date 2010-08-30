package org.joshy.sketch.actions;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.ArrayListModel;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.modes.vector.VectorDocContext;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 6, 2010
 * Time: 5:53:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentActions {

    public static class SetBackground extends SAction {
        private DocContext context;

        public SetBackground(DocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            final Stage dialog = Stage.createStage();
            dialog.setTitle("Set Background");
            dialog.setWidth(400);
            dialog.setHeight(300);

            Callback<ActionEvent> okayed = new Callback<ActionEvent>() {
                public void call(ActionEvent event) {
                    dialog.hide();
                }
            };

            SwatchColorPicker picker = new SwatchColorPicker();
            EventBus.getSystem().addListener(picker, ChangedEvent.ColorChanged, new Callback<ChangedEvent>() {
                public void call(ChangedEvent event) {
                    SketchDocument doc = (SketchDocument) context.getDocument();
                    doc.setBackgroundFill((FlatColor)event.getValue());
                    context.redraw();
                }
            });
            picker.setTranslateX(10);
            picker.setTranslateY(10);
            Panel panel = new Panel();
            panel.add(picker);
            Button okay = new Button("Close");
            okay.setTranslateX(200);
            okay.setTranslateY(200);
            okay.onClicked(okayed);
            panel.add(okay);
            dialog.setContent(panel);


        }
    }

    public static class SetDocumentSize extends SAction {
        private VectorDocContext context;

        public SetDocumentSize(VectorDocContext context) {
            this.context = context;
        }

        @Override
        public void execute() {
            final Stage dialog = Stage.createStage();
            dialog.setTitle("Document Size");
                final Textbox width = new Textbox(""+context.getDocument().getWidth());
                final Textbox height = new Textbox(""+context.getDocument().getHeight());
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
                        SketchDocument doc = context.getDocument();
                        doc.setWidth(dwidth);
                        doc.setHeight(dheight);
                        context.redraw();
                    }
                };

                final PopupMenuButton popup = new PopupMenuButton();
                popup.setModel(new ArrayListModel<String>("16x16","1024x768"));
                Callback<ActionEvent> clicked = new Callback<ActionEvent>() {
                    public void call(ActionEvent event) {
                        switch(popup.getSelectedIndex()) {
                            case 0: width.setText("16"); height.setText("16"); break;
                            case 1: width.setText("1024"); height.setText("768"); break;
                        }
                    }
                };
                popup.onClicked(clicked);
                dialog.setContent(new VFlexBox().add(
                        new HFlexBox().add(new Label("Preset:"),popup),
                        new HFlexBox().add(new Label("Width (px):"), width),
                        new HFlexBox().add(new Label("Height (px):"), height),
                        new HFlexBox().add(
                                new Button("Cancel")
                                    .onClicked(canceled), 
                                new Button("Okay")
                                        .onClicked(okay))
                ));
            dialog.setWidth(400);
            dialog.setHeight(400);
        }
    }

}
