package org.joshy.gfx.test.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.event.BackgroundTask;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.SelectionEvent;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.FlexBox;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.node.layout.SplitPane;
import org.joshy.gfx.node.layout.VFlexBox;
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
                FlexBox box = new VFlexBox()
                        .setBoxAlign(FlexBox.Align.Left)
                        .add(new Button("Regular Button"))
                        .add(new Togglebutton("Toggle Button"))
                        .add(new Checkbox("Check box"))
                        .add(new Radiobutton("Radio button"))
                        .add(new Linkbutton("a hyperlink"));
                return box;
            }
        });
        examples.add(new Example("Text controls") {
            public Control build() {
                Textbox tb = new Textbox();
                tb.setText("a textbox");
                Passwordbox passbox = new Passwordbox();
                passbox.setText("password");
                Textarea ta = new Textarea();
                ta.setText("A\nText\nArea");
                FlexBox box = new VFlexBox().setBoxAlign(FlexBox.Align.Left)
                        .add(new Label("A Label"))
                        .add(tb)
                        .add(passbox)
                        .add(ta);
                return box;
            }
        });
        examples.add(new Example("Sliders and Scrollbars") {
            public Control build() {
                Scrollbar sb = new Scrollbar();
                sb.setWidth(200);
                sb.setMin(0); sb.setMax(200); sb.setValue(100);
                Scrollbar psb = new Scrollbar();
                psb.setProportional(true);
                psb.setWidth(200);
                psb.setMin(0); psb.setMax(200); psb.setValue(100); psb.setSpan(0.5);
                Slider slider = new Slider(false);
                //slider.setWidth(200);
                slider.setMin(0);
                slider.setMax(100);
                slider.setValue(50);
                FlexBox box = new VFlexBox().setBoxAlign(FlexBox.Align.Left)
                        .add(sb)
                        .add(psb)
                        .add(slider);
                return box;
            }
        });
        examples.add(new Example("Progress Bars and Spinners") {
            public Control build() throws InterruptedException {
                ProgressBar pb = new ProgressBar();
                ProgressSpinner ps = new ProgressSpinner();
                FlexBox box = new VFlexBox().setBoxAlign(FlexBox.Align.Left)
                        .add(pb)
                        .add(ps);
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
                return box;
            }
        });
        examples.add(new Example("Complex (list, table, dropdowns)") {
            public Control build() throws IOException {
                PopupMenuButton popup = new PopupMenuButton();
                SwatchColorPicker color1 = new SwatchColorPicker();
                popup.setModel(ListView.createModel(new String[]{"Ethernet","WiFi","Bluetooth","FireWire","USB hack"}));

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

                CompoundListView clistView = new CompoundListView();
                ScrollPane sp3 = new ScrollPane();
                sp3.setContent(clistView);
                sp3.setWidth(300);
                sp3.setHeight(200);

                TableView table = new TableView();
                ScrollPane sp2 = new ScrollPane();
                sp2.setContent(table);
                sp2.setWidth(300);
                sp2.setHeight(200);

                FlexBox box = new VFlexBox()
                        .add(popup)
                        .add(color1)
                        .add(new Label("List View"))
                        .add(sp)
                        .add(new Label("Compounds List View"))
                        .add(sp3)
                        .add(sp2);
                return box;

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
