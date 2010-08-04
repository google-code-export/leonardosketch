package org.joshy.gfx.test.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.SkinManager;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.stage.Stage;

import java.io.File;

public class ListTest implements Runnable {
    
    public static void main(String ... args) throws Exception {
        Core.setUseJOGL(false);
        Core.init();
        SkinManager.getShared().parseStylesheet(new File("assets/style.xml").toURI().toURL());
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
