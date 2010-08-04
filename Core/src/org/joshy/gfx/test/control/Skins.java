package org.joshy.gfx.test.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.SkinManager;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Checkbox;
import org.joshy.gfx.node.control.Radiobutton;
import org.joshy.gfx.node.control.Togglebutton;
import org.joshy.gfx.node.control.skin.InsetsSkin;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.node.layout.VBox;
import org.joshy.gfx.stage.Stage;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Feb 4, 2010
 * Time: 1:03:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Skins implements Runnable {
    public static void main(String... args) throws Exception {
        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().defer(new Skins());
    }

    public void run() {
        try {
            SkinManager.getShared().parseStylesheet(new File("assets/style.xml").toURI().toURL());

            SkinManager.getShared().installSkin(Button.class.getName(), "imageOnly", "main", "padding", "normal", new InsetsSkin(0,0,0,0));

            Button prevButton = new Button("");
            prevButton.setVariant("imageOnly");
            prevButton.setNormalIcon(new File("assets/itunes/prevButton.png").toURI().toURL());
            prevButton.setPressedIcon(new File("assets/itunes/prevButton_pressed.png").toURI().toURL());

            Panel panel = new VBox();
            panel.add(prevButton);
            panel.add(new Button("button"));
            panel.add(new Checkbox("Check Box"));
            panel.add(new Radiobutton("Radio button"));
            panel.add(new Togglebutton("Toggle"));
            Stage stage = Stage.createStage();
            stage.setContent(panel);

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
