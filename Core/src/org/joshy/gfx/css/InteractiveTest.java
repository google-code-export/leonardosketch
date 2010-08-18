package org.joshy.gfx.css;

import org.joshy.gfx.Core;
import org.joshy.gfx.SkinManager;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.PeriodicTask;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Checkbox;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.Radiobutton;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.stage.swing.SwingCore;
import org.joshy.gfx.util.u;
import org.parboiled.support.ParsingResult;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 29, 2010
 * Time: 5:00:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class InteractiveTest implements Runnable {
    private Thread thread;
    private long lastModified;
    private Panel hbox;

    public static void main(String ... args) throws Exception, InterruptedException {
        Core.init();
        Core.getShared().defer(new InteractiveTest());
    }

    public void run() {
        Stage stage = Stage.createStage();
        SimpleBoxControl node1 = new SimpleBoxControl(this);
        SimpleBoxControl node2 = new SimpleBoxControl(this);
        hbox = new Panel().onDoLayout(new Callback<Panel>() {
            public void call(Panel panel) {
                double x = 0;
                double y = 0;
                for(Control c : panel.controlChildren()) {
                    c.doLayout();
                    c.setTranslateX(x);
                    x+=c.getWidth();
                }
            }
        });

        hbox.add(new Button("button"));
        hbox.add(new Checkbox("checkbox"));
        hbox.add(new Radiobutton("radiobutton"));
        stage.setContent(hbox);

        new PeriodicTask(100).call(new Callback() {
            public void call(Object event) {
                checkFile();
            }
        }).start();

    }

    private void reloadCSS() {
        File file = new File("test.css");
        if(file.exists()) {
            try {
                ParsingResult<?> result = CSSProcessor.parseCSS(new FileInputStream(file));
                CSSRuleSet set = new CSSRuleSet();
                set.setBaseURI(file.toURI());
                SwingCore sc = (SwingCore) Core.getShared();
                SkinManager.getShared().getCSSSkin().setRuleSet(set);
                CSSProcessor.condense(result.parseTreeRoot,set);
                u.p("parsed. reloading skins");
                Core.getShared().reloadSkins();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private boolean checkFile() {
        File file = new File("test.css");
        //u.p("path = " + file.getAbsolutePath());
        if(!file.exists()) return false;
        if(file.lastModified() > lastModified) {
            u.p("updated file!");
            reloadCSS();
        }
        lastModified = file.lastModified();
        return false;
    }

}
