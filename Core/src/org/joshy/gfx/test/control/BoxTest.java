package org.joshy.gfx.test.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.node.Insets;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.layout.HAlign;
import org.joshy.gfx.node.layout.HBox;
import org.joshy.gfx.node.layout.VBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.u;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Apr 8, 2010
 * Time: 7:08:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class BoxTest implements Runnable {
    public static void main(String ... args) throws Exception {
        Core.init();
        Core.getShared().defer(new BoxTest());
    }

    public void run() {

        /*

        new box design
        boxes shrink to fit their contents *unless* they are given explicit sizes with setPreferredWidth/Height
        boxes have border (1px always for now) and padding
        background fills the area inside the border
        visual bounds should reflect the w/h after layout is done

        default align for vbox is center
        default align for hbox is center
         */
        try {
        VBox vBox = new VBox();
        vBox.setFill(FlatColor.YELLOW);
        HBox hbox = new HBox();
        hbox.setHAlign(HAlign.TOP);
        hbox.setBorderWidth(2);
        hbox.setPadding(new Insets(5));
        hbox.setSpacing(10);
        hbox.setFill(FlatColor.GRAY);
        hbox.add(
                new Label("label 1"),
                new Label("label 2"),
                new Label("label 3"));
        vBox.add(hbox);
        
        vBox.add(
                new Label("v label 1"),
                new Label("v label 2"),
                new Label("v label 3")
                );
        vBox.add(new HBox()
                .setPreferredWidth(300)
                .setPreferredHeight(60)
                .setBorderWidth(2)
                .setHAlign(HAlign.CENTER)
                .setPadding(new Insets(5))
                .setFill(FlatColor.GRAY).
                add(
                        new Label("h centered"),
                        new Button("Button")
                )
        );
        vBox.add(new HBox()
                .setPreferredWidth(300)
                .setPreferredHeight(60)
                .setBorderWidth(2)
                .setHAlign(HAlign.TOP)
                .setPadding(new Insets(5))
                .setFill(FlatColor.GRAY).
                add(
                        new Label("h top"),
                        new Button("Button")
                )
        );
        vBox.add(new HBox()
                .setPreferredWidth(300)
                .setPreferredHeight(60)
                .setBorderWidth(2)
                .setHAlign(HAlign.BOTTOM)
                .setPadding(new Insets(5))
                .setFill(FlatColor.GRAY).
                add(
                        new Label("h bottom"),
                        new Button("Button")
                )
        );
        vBox.add(new HBox()
                .setPreferredWidth(300)
                .setPreferredHeight(60)
                .setBorderWidth(2)
                .setHAlign(HAlign.BASELINE)
                .setPadding(new Insets(5))
                .setFill(FlatColor.GRAY).
                add(
                        new Label("h baseline"),
                        new Button("Button")
                )
        );



        Stage stage = Stage.createStage();
        stage.setContent(vBox);

        } catch (Exception ex) {
            u.p(ex);
        }
    }
}
