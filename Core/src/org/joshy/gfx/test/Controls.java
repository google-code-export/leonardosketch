package org.joshy.gfx.test;

import org.joshy.gfx.Core;
import org.joshy.gfx.SkinManager;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.event.BackgroundTask;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.ProgressUpdate;
import org.joshy.gfx.node.layout.VBox;
import org.joshy.gfx.node.shape.Rectangle;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.u;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 21, 2010
 * Time: 9:14:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class Controls implements Runnable {
    
    public static void main(String... args) throws Exception {
        Core.setUseJOGL(false);
        Core.init();
        Core.getShared().defer(new Controls());
    }

    public void run() {
        try {
            SkinManager.getShared().parseStylesheet(new File("assets/style.xml").toURI().toURL());
            Panel panel = new Panel();


            VBox vbox = new VBox();
            panel.add(vbox);

            Button b = new Button();
            //b.setWidth(250);
            //b.setHeight(30);
            b.setText("ABC def QRS xyz");
            vbox.add(b);

            Button tog = new Togglebutton("Toggle");
            vbox.add(tog);

            vbox.add(new Radiobutton("Radio"));

            Checkbox cb = new Checkbox();
            cb.setText("Checkbox");
            vbox.add(cb);

            Scrollbar sb = new Scrollbar();
            sb.setWidth(200);
            sb.setMin(0); sb.setMax(200); sb.setValue(100);
            vbox.add(sb);

            Scrollbar psb = new Scrollbar();
            psb.setProportional(true);
            psb.setWidth(200);
            psb.setMin(0); psb.setMax(200); psb.setValue(100); psb.setSpan(0.5);
            vbox.add(psb);

            Slider slider = new Slider(false);
            slider.setWidth(200);
            slider.setMin(0);
            slider.setMax(100);
            slider.setValue(50);
            vbox.add(slider);


            Textbox tb = new Textbox();
            tb.setText("a textbox");
            vbox.add(tb);

            Passwordbox passbox = new Passwordbox();
            passbox.setText("password");
            vbox.add(passbox);

            ProgressBar pb = new ProgressBar();
            pb.setTranslateX(200);
            vbox.add(pb);

            ProgressSpinner ps = new ProgressSpinner();
            vbox.add(ps);



            EventBus.getSystem().addListener(ProgressUpdate.TYPE, new Callback<ProgressUpdate>() {
                public void call(ProgressUpdate event) {
//                    u.p("progress = " + event.getPercentage());
                }
            });
            EventBus.getSystem().addListener(ChangedEvent.StringChanged, new Callback<ChangedEvent>() {
                public void call(ChangedEvent event) {
//                    u.p("text changed to " + event.getValue() + " by control " + event.getSource());
                }
            });

            BackgroundTask task = new BackgroundTask<String, String>() {
                @Override
                protected String onWork(String data) {
                    String result = "bar";
                    for(int i=0; i<100; i++) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        this.updateGUI(data,result,i/100.0);
                    }
                    updateGUI(data,result,1);
                    return result;
                }
            };
            task.setData("foo");
            pb.setTask(task);

            BackgroundTask task2 = new BackgroundTask<String,String>() {
                @Override
                protected String onWork(String data) {
                    String result = "blah";
                    this.updateGUI(data,result,-1);
                    try {
                        Thread.sleep(100*100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    this.updateGUI(data,result,1);
                    return result;
                }
            };

            ps.setTask(task2);
            //ps.setTask(task);
            task.start();
            task2.start();


            PopupMenuButton popup = new PopupMenuButton();
            popup.setTranslateX(20);
            popup.setTranslateY(250);
            popup.setModel(ListView.createModel(new String[]{"Ethernet","WiFi","Bluetooth","FireWire","USB hack"}));
            vbox.add(popup);

            SwatchColorPicker color1 = new SwatchColorPicker();
            vbox.add(color1);
            
            Stage stage = Stage.createStage();
            stage.setContent(panel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
