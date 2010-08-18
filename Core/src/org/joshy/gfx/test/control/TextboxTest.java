package org.joshy.gfx.test.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.node.control.Textarea;
import org.joshy.gfx.node.control.Textbox;
import org.joshy.gfx.stage.Stage;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 26, 2010
 * Time: 8:46:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class TextboxTest implements Runnable {
    public static void main(String ... args) throws Exception, InterruptedException {
        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().defer(new TextboxTest());
        EventBus.getSystem().addListener(KeyEvent.KeyPressed, new Callback<KeyEvent>() {
            public void call(KeyEvent event) {
//                u.p("key pressed " + event + " on node " + event.getSource());
            }
        });
    }

    public void run() {
        /*
        Panel g = new Panel();
        Textbox text1 = new Textbox();
        text1.setText("abcd");
        //text1.setWidth(100);
//        text1.setHeight(20);
        g.add(text1);

        Textarea ta = new Textarea();
//        ta.setWidth(200);
//        ta.setHeight(200);
        ta.setTranslateX(300);
        g.add(ta);
        
        Textbox t2 = new Textbox();
        t2.setText("abcd");
//        t2.setWidth(100);
//        t2.setHeight(20);
        t2.setTranslateY(100);
        g.add(t2);
          */
        Stage stage = Stage.createStage();
        //stage.setContent(g);
        Textarea ta = new Textarea("hello there\nmister man\nhow are you doing todays?");
        //Textbox ta = new Textbox("hello there");
        //ta.setFont(Font.name("Helvetica").size(50).resolve());
        stage.setContent(ta);

    }
}
