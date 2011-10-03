package org.joshy.sketch.controls;

import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.layout.GridBox;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.OpenAction;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.modes.DocModeHelper;

import static org.joshy.gfx.util.localization.Localization.getString;

/**
* Created by IntelliJ IDEA.
* User: josh
* Date: 10/3/11
* Time: 2:12 PM
* To change this template use File | Settings | File Templates.
*/
public class NewDocumentChooser extends VFlexBox {

    public NewDocumentChooser(final Main main, final Stage stage) {
        GridBox grid = new GridBox()
                .createColumn(200,GridBox.Align.Fill)
                .createColumn(200,GridBox.Align.Fill);
        grid.setPrefWidth(500);
        grid.setPrefHeight(400);
        grid.debug(false);

        int count = 0;
        for(final DocModeHelper mode : main.getModeHelpers()) {
            String name = getString("misc.new").toString() + " " + mode.getModeName();
            if(mode.isAlpha()) {
                name = "[ALPHA] " + name;
            }
            grid.addControl(new Button(name).onClicked(new Callback<ActionEvent>() {
                public void call(ActionEvent event) throws Exception {
                    SAction action = mode.getNewDocAction(main);
                    action.execute();
                    stage.hide();
                }
            }).addCSSClass("newdocbutton"));
            count++;
            if(count%2==0) {
                grid.nextRow();
            }
        }


        this.setBoxAlign(Align.Stretch);
        this.add(grid,1);

        this.add(new Button("Open Existing Document").onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                new OpenAction(main).execute();
                if (!main.getContexts().isEmpty()) {
                    stage.hide();
                }
            }
        }));

        this.add(new Button("Exit").onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                System.exit(0);
            }
        }));

    }
}
