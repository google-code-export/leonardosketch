package org.joshy.sketch.actions;

import org.joshy.gfx.draw.LinearGradientFill;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.control.PopupMenuButton;
import org.joshy.gfx.node.control.Textbox;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.ArrayListModel;
import org.joshy.sketch.Main;
import org.joshy.sketch.controls.FillPicker;
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
        private Main manager;

        public SetBackground(DocContext context, Main main) {
            super();
            this.context = context;
            this.manager = main;
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

            FillPicker picker = new FillPicker(this.manager);
            EventBus.getSystem().addListener(picker,ChangedEvent.ObjectChanged, new Callback<ChangedEvent>() {
                public void call(ChangedEvent event) throws Exception {
                    Paint paint = (Paint) event.getValue();
                    SketchDocument doc = (SketchDocument) context.getDocument();
                    setBackgroundFill(paint, doc);
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

        private void setBackgroundFill(Paint paint, SketchDocument doc) {
            if(paint instanceof LinearGradientFill) {
                LinearGradientFill grad = (LinearGradientFill) paint;
                grad = resizeTo(grad, new Bounds(0, 0, doc.getWidth(), doc.getHeight()));
                doc.setBackgroundFill(grad);
                return;
            }

            doc.setBackgroundFill(paint);
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

    public static LinearGradientFill resizeTo(LinearGradientFill grad, Bounds bounds) {
        LinearGradientFill g2 = (LinearGradientFill) grad.duplicate();
        switch(grad.getStartXSnapped()) {
            case Start: g2.setStartX(bounds.getX()); break;
            case Middle: g2.setStartX(bounds.getCenterX()); break;
            case End: g2.setStartX(bounds.getX2()); break;
        }
        switch(grad.getEndXSnapped()) {
            case Start: g2.setEndX(bounds.getX()); break;
            case Middle: g2.setEndX(bounds.getCenterX()); break;
            case End: g2.setEndX(bounds.getX2()); break;
        }
        switch(grad.getStartYSnapped()) {
            case Start: g2.setStartY(bounds.getY()); break;
            case Middle: g2.setStartY(bounds.getCenterY()); break;
            case End: g2.setStartY(bounds.getY2()); break;
        }
        switch(grad.getEndYSnapped()) {
            case Start: g2.setEndY(bounds.getY()); break;
            case Middle: g2.setEndY(bounds.getCenterY()); break;
            case End: g2.setEndY(bounds.getY2()); break;
        }
        return g2;
    }
}
