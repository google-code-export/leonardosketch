package org.joshy.gfx.test.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.event.BackgroundTask;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.SelectionEvent;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.node.layout.SplitPane;
import org.joshy.gfx.node.layout.VBox;
import org.joshy.gfx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GrandTour implements Runnable {
    public static void main(String... args) throws Exception, InterruptedException {
        Core.setUseJOGL(false);
        Core.init();
        //SkinManager.getShared().parseStylesheet(new File("assets/style.xml").toURI().toURL());
        Core.getShared().defer(new GrandTour());
    }

    public void run() {
        List<Example> examples = new ArrayList<Example>();
        examples.add(new Example("Buttons") {
            public Control build() {
                VBox box = new VBox();
                try {
                    box.add(new Button("Regular Button"));
                    box.add(new Togglebutton("Toggle Button"));
                    box.add(new Checkbox("Check box"));
                    box.add(new Radiobutton("Radio button"));
                    box.add(new Linkbutton("a hyperlink"));
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return box;
            }
        });
        examples.add(new Example("Text controls") {
            public Control build() {
                VBox box = new VBox();

                box.add(new Label("A Label"));
                Textbox tb = new Textbox();
                tb.setText("a textbox");
                box.add(tb);

                Passwordbox passbox = new Passwordbox();
                passbox.setText("password");
                box.add(passbox);

                Textarea ta = new Textarea();
                ta.setText("A\nText\nArea");
                box.add(ta);
                return box;
            }
        });
        examples.add(new Example("Sliders and Scrollbars") {
            public Control build() {
                VBox vbox = new VBox();
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
                return vbox;
            }
        });
        examples.add(new Example("Progress Bars and Spinners") {
            public Control build() throws InterruptedException {
                VBox vbox = new VBox();
                ProgressBar pb = new ProgressBar();
                pb.setTranslateX(200);
                vbox.add(pb);

                ProgressSpinner ps = new ProgressSpinner();
                vbox.add(ps);
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
                task.start();
                task2.start();
                return vbox;
            }
        });
        examples.add(new Example("Complex (list, table, dropdowns)") {
            public Control build() throws IOException {
                VBox vbox = new VBox();
                PopupMenuButton popup = new PopupMenuButton();
                popup.setTranslateX(20);
                popup.setTranslateY(250);
                popup.setModel(ListView.createModel(new String[]{"Ethernet","WiFi","Bluetooth","FireWire","USB hack"}));
                vbox.add(popup);

                SwatchColorPicker color1 = new SwatchColorPicker();
                vbox.add(color1);


                vbox.add(new Label("List View"));
                ListView listView = new ListView();
                listView.setModel(new ListModel(){
                    public Object get(int i) {
                        return "item " + i;
                    }
                    public int size() {
                        return 100;
                    }
                });
                ScrollPane sp = new ScrollPane();
                sp.setContent(listView);
                sp.setWidth(300);
                sp.setHeight(200);
                vbox.add(sp);

                vbox.add(new Label("Compounds List View"));
                CompoundListView clistView = new CompoundListView();
                ScrollPane sp3 = new ScrollPane();
                sp3.setContent(clistView);
                sp3.setWidth(300);
                sp3.setHeight(200);
                vbox.add(sp3);

                TableView table = new TableView();
                ScrollPane sp2 = new ScrollPane();
                sp2.setContent(table);
                sp2.setWidth(300);
                sp2.setHeight(200);
                vbox.add(sp2);
                
                return vbox;
            }
        });

        ListView exampleList = new ListView();
        final ListModel<Example> exampleModel = (ListModel<Example>)ListView.createModel(examples);
        exampleList.setModel(exampleModel);
        exampleList.setTextRenderer(new ListView.TextRenderer<Example>(){
            public String toString(ListView view, Example item, int index) {
                if(item != null) {
                    return item.name;
                } else {
                    return null;
                }
            }
        });
        
        ScrollPane exampleListScroll = new ScrollPane();
        exampleListScroll.setContent(exampleList);

        final SplitPane split = new SplitPane(false);
        split.setFirst(exampleListScroll);
        split.setSecond(new Panel());
        split.setPosition(300);
        Stage stage = Stage.createStage();
        stage.setContent(split);

        Callback<SelectionEvent> callback = new Callback<SelectionEvent>() {
            public void call(SelectionEvent event) {
                Example ex = exampleModel.get(event.getView().getSelectedIndex());
                if(ex == null) return;
                try {
                    split.setSecond(ex.build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        EventBus.getSystem().addListener(exampleList, SelectionEvent.Changed, callback);
    }

    private abstract class Example {
        public String name;

        public Example(String name) {
            this.name = name;
        }

        public abstract Control build() throws Exception;
    }
}
