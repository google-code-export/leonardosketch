package org.joshy.gfx.test.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.node.control.ListModel;
import org.joshy.gfx.node.control.ListView;
import org.joshy.gfx.node.control.ScrollPane;
import org.joshy.gfx.stage.Stage;

public class ListTest implements Runnable {
    
    public static void main(String ... args) throws Exception {
        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().defer(new ListTest());
    }

    public void run() {
        //final CompoundListView listView = new CompoundListView();
        final ListView<String> listView = new ListView<String>();
        listView.setModel(new ListModel<String>() {
            public String get(int i) {
                if(i > size()-1) {
                    return null;
                }
                return "blah " + i;
            }
            public int size() {
                return 50;
            }
        });
//        listView.setWidth(300);
//        listView.setHeight(300);
        ScrollPane scroll = new ScrollPane();
        scroll.setContent(listView);
        Stage stage = Stage.createStage();
        stage.setContent(scroll);
    }
}
