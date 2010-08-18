package org.joshy.gfx.test;

import org.joshy.gfx.Core;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Checkbox;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.control.Textbox;
import org.joshy.gfx.node.layout.HAlign;
import org.joshy.gfx.node.layout.HBox;
import org.joshy.gfx.node.shape.Rectangle;
import org.joshy.gfx.stage.Stage;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 22, 2010
 * Time: 9:46:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class Layout implements Runnable {
    public static void main(String ... args) throws Exception {
        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().defer(new Layout());
    }

    public void run() {
        Stage stage = Stage.createStage();
        HBox hBox = new HBox();
        hBox.setHAlign(HAlign.BASELINE);
        stage.setContent(hBox);

            Rectangle r = new Rectangle();
            r.setWidth(30);
            r.setHeight(10);
            hBox.add(
                new Button("Button"),
                new Button("buttony"),
                new Checkbox("Checkbox"),
                new Label("Label"),
                new Textbox("TextBoxy"),
                r
            );


        //stage.setVisible(true);
    }
}
