package org.joshy.gfx.test.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.node.control.ListModel;
import org.joshy.gfx.node.control.ListView;
import org.joshy.gfx.node.control.ScrollPane;
import org.joshy.gfx.node.shape.Oval;
import org.joshy.gfx.stage.Stage;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Feb 1, 2010
 * Time: 10:55:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class ScrollPaneTest implements Runnable {
    
    public static void main(String ... args) throws Exception {
        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().defer(new ScrollPaneTest());
    }

    public void run() {
        Stage stage = Stage.createStage();
        ScrollPane scroll = new ScrollPane();
        scroll.setHorizontalScrollVisible(true);
        Oval oval = new Oval();
        oval.setWidth(700);
        oval.setHeight(1000);
        scroll.setContent(oval);

        ListView list = new ListView();
        list.setModel(new ListModel<String>(){

            public String get(int i) {
                return "Value " + i;
            }

            public int size() {
                return 100;
            }
        });
        //scroll.setContent(list);

        stage.setContent(scroll);
    }
}
