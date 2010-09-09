package org.joshy.gfx.test.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.node.control.ScrollPane;
import org.joshy.gfx.node.control.TableView;
import org.joshy.gfx.stage.Stage;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 31, 2010
 * Time: 1:32:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class TableTest implements Runnable {
    public static void main(String ... args) throws Exception, InterruptedException {
        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().defer(new TableTest());
    }

    public void run() {
        TableView table = new TableView();
        table.setWidth(500);
        Stage s = Stage.createStage();
        s.setContent(new ScrollPane(table));
    }
}
