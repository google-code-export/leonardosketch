package org.joshy.gfx.test;

import org.joshy.gfx.Core;
import org.joshy.gfx.SkinManager;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Checkbox;
import org.joshy.gfx.node.layout.VBox;
import org.joshy.gfx.stage.Stage;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 23, 2010
 * Time: 8:28:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class Styles implements Runnable {

    public static void main(String ... args) throws Exception {
        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().defer(new Styles());
    }

    public void run() {
        try {
            Stage stage = Stage.createStage();

            VBox panel = new VBox();
            panel.add(new Button());
            Button b = new Button();
            b.setStyle("radiobutton");
            panel.add(b);
            panel.add(new Checkbox());
        stage.setContent(panel);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
