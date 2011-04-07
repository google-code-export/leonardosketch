package org.joshy.sketch.controls;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.event.*;
import org.joshy.gfx.event.Event;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.ListModel;
import org.joshy.gfx.node.control.ListView;
import org.joshy.gfx.node.layout.FlexBox;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.TabPanel;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.ArrayListModel;
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/8/11
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class FillPicker extends Button {
    Paint selectedFill;
    private TabPanel popup;
    private double inset = 2;

    public static void main(String ... args) throws Exception {
        Core.init();
        Core.getShared().defer(new Runnable() {
            public void run() {
                Stage stage = Stage.createStage();
                stage.setContent(new ColorPickerPanel());
                EventBus.getSystem().addListener(SystemMenuEvent.Quit,new Callback<Event>() {
                    public void call(Event event) throws Exception {
                        System.exit(0);
                    }
                });
            }
        });
    }

    public FillPicker() {
        super("X");
        setPrefWidth(25);
        setPrefHeight(25);
        selectedFill = FlatColor.RED;
    }

    @Override
    protected void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (pressed) {
            if (popup == null) {
                try {
                    popup = buildPanel();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                popup.setVisible(false);
                Stage stage = getParent().getStage();
                stage.getPopupLayer().add(popup);
            }
            Point2D pt = NodeUtils.convertToScene(this, 0, getHeight());
            popup.setTranslateX(Math.round(Math.max(pt.getX(), 0)));
            popup.setTranslateY(Math.round(Math.max(pt.getY(), 0)));
            popup.setVisible(true);
            EventBus.getSystem().setPressedNode(popup);
        } else {
            //popup.setVisible(false);
        }
    }

    @Override
    public void draw(GFX g) {
        if(!isVisible())return;
        g.setPaint(FlatColor.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setPaint(FlatColor.WHITE);
        g.fillRect(0+1, 0+1, getWidth()-2, getHeight()-2);
        g.setPaint(selectedFill);
        g.fillRect(inset, inset, getWidth() - inset*2, getHeight() - inset*2);
    }

    ListView.ItemRenderer<Paint> paintItemRenderer = new ListView.ItemRenderer<Paint>() {
        public void draw(GFX gfx, ListView listView, Paint paint, int index, double x, double y, double w, double h) {
            gfx.translate(x,y);
            gfx.setPaint(paint);
            gfx.fillRect(0,0,w,h);
            gfx.setPaint(FlatColor.BLACK);
            gfx.drawRect(0,0,w,h);
            gfx.translate(-x,-y);
        }
    };

    private TabPanel buildPanel() throws IOException {
        final TabPanel panel = new TabPanel();
        panel.setPrefWidth(300);
        panel.setPrefHeight(200);

        setupSwatchTab(panel);
        setupGradientTab(panel);
        setupPatternTab(panel);

        //TODO: is this popup event really working?
        EventBus.getSystem().addListener(panel, MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public void call(MouseEvent event) {
                if(event.getType() == MouseEvent.MouseDragged) {
                    if(!popup.isVisible()) return;
                    Control control = panel.getSelected();
                    if(control instanceof ListView) {
                        ListView lv = (ListView) control;
                        Object item = lv.getItemAt(event.getPointInNodeCoords(lv));
                        if(item instanceof Paint) {
                            setSelectedFill((Paint) item);
                        }
                    }
                }
                if(event.getType() == MouseEvent.MouseReleased) {
                    Point2D pt = event.getPointInNodeCoords(panel);
                    pt = new Point2D.Double(pt.getX()+panel.getTranslateX(),pt.getY()+panel.getTranslateY());
                    if(panel.getVisualBounds().contains(pt)) {
                        popup.setVisible(false);
                    }
                }
            }
        });

        return panel;
    }

    private void setupPatternTab(TabPanel panel) throws IOException {
        double size = 40;

        //PatternPaint pt1 = PatternPaint.create(SRect.class.getResource("resources/button1.png"));
        PatternPaint pt1 = PatternPaint.create(Main.class.getResource("resources/textures/webtreats-paper-pattern-1-grey.jpg"),"t1");
        pt1 = pt1.deriveNewStart(new Point(40,40));
        PatternPaint pt2 = PatternPaint.create(Main.class.getResource("resources/textures/webtreats-paper-pattern-2-grey.jpg"),"t2");
        PatternPaint pt3 = PatternPaint.create(Main.class.getResource("resources/textures/webtreats-paper-pattern-3-grey.jpg"),"t3");
        PatternPaint pt4 = PatternPaint.create(Main.class.getResource("resources/textures/webtreats-paper-pattern-4-grey.jpg"),"t4");
        PatternPaint pt5 = PatternPaint.create(Main.class.getResource("resources/textures/webtreats-paper-pattern-5-grey.jpg"),"t4");
        PatternPaint pt6 = PatternPaint.create(Main.class.getResource("resources/textures/webtreats-paper-pattern-6-grey.jpg"),"t6");

        final ArrayListModel<PatternPaint> patternModel = new ArrayListModel<PatternPaint>();
        patternModel.add(pt1);
        patternModel.add(pt2);
        patternModel.add(pt3);
        patternModel.add(pt4);
        patternModel.add(pt5);
        patternModel.add(pt6);
        //ListModel<PatternPaint> patternModel = ListView.createModel(pt1,pt2,pt3,pt4,pt5,pt6);


        final ListView<Paint> patternList = new ListView<Paint>()
                .setModel((ListModel) patternModel)
                .setColumnWidth(size)
                .setRowHeight(size)
                .setOrientation(ListView.Orientation.HorizontalWrap)
                .setRenderer(paintItemRenderer)
                ;
        EventBus.getSystem().addListener(patternList, SelectionEvent.Changed, new Callback<SelectionEvent>(){
            public void call(SelectionEvent e) throws Exception {
                int n = e.getView().getSelectedIndex();
                setSelectedFill(patternList.getModel().get(n));
                popup.setVisible(false);
            }
        });
        Button addButton = new Button("+");
        addButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                FileDialog fd = new FileDialog((Frame)null);
                fd.setMode(FileDialog.LOAD);
                fd.setTitle("Open Pattern Image");
                fd.setVisible(true);
                if(fd.getFile() != null) {
                    File file = new File(fd.getDirectory(),fd.getFile());
                    u.p("opening a file" + file);
                    try {
                        PatternPaint pat = PatternPaint.create(file);
                        patternModel.add(pat);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        VFlexBox vbox = new VFlexBox();
        vbox.setBoxAlign(FlexBox.Align.Stretch);
        vbox.setFill(FlatColor.GRAY);
        vbox.add(patternList, 1);
        vbox.add(new HFlexBox().add(addButton));
        panel.add("Patterns", vbox);
    }

    private ListView<Paint> setupGradientTab(TabPanel panel) {
        double size = 40;
        //linears
        Paint gf1 = new LinearGradientFill()
                .setStartX(0).setEndX(size)
                .setStartY(size/2).setEndY(size/2)
                .addStop(0,FlatColor.BLACK)
                .addStop(1,FlatColor.WHITE);

        Paint gf2 = new LinearGradientFill()
                .setStartX(size/2).setEndX(size/2)
                .setStartY(0).setEndY(size)
                .addStop(0,FlatColor.BLACK)
                .addStop(1,FlatColor.WHITE);

        Paint gf3 = new LinearGradientFill()
                .setStartX(0).setEndX(size)
                .setStartY(0).setEndY(size)
                .addStop(0,FlatColor.BLACK)
                .addStop(1,FlatColor.WHITE);

        //linears 2
        Paint gf6 = new LinearGradientFill()
                .setStartX(0).setStartY(size / 2)
                .setEndX(size).setEndY(size / 2)
                .addStop(0.0, FlatColor.BLACK)
                .addStop(0.5, FlatColor.WHITE)
                .addStop(1.0, FlatColor.BLACK);

        //radials
        Paint gf4 = new RadialGradientFill()
                .setCenterX(size / 2).setCenterY(size / 2)
                .setRadius(size / 2)
                .addStop(0, FlatColor.BLACK)
                .addStop(1, FlatColor.WHITE);

        Paint gf5 = new RadialGradientFill()
                .setCenterX(size / 2).setCenterY(size / 2)
                .setRadius(size / 2)
                .addStop(0.0, FlatColor.BLACK)
                .addStop(0.5, FlatColor.WHITE)
                .addStop(1.0, FlatColor.BLACK);

        ListModel<Paint> gradientModel = ListView.createModel(gf1, gf2, gf3, gf6, gf4, gf5);
        final ListView<Paint> gradientList = new ListView<Paint>()
                .setModel(gradientModel)
                .setColumnWidth(size)
                .setRowHeight(size)
                .setOrientation(ListView.Orientation.HorizontalWrap)
                .setRenderer(paintItemRenderer)
                ;
        panel.add("gradients", gradientList);
        EventBus.getSystem().addListener(gradientList, SelectionEvent.Changed, new Callback<SelectionEvent>(){
            public void call(SelectionEvent e) throws Exception {
                int n = e.getView().getSelectedIndex();
                setSelectedFill(gradientList.getModel().get(n));
                popup.setVisible(false);
            }
        });

        return gradientList;
    }

    private void setupSwatchTab(TabPanel panel) {
        final ArrayListModel<FlatColor> colorModel = new ArrayListModel<FlatColor>();
        colorModel.add(FlatColor.RED);
        colorModel.add(FlatColor.GREEN);
        colorModel.add(FlatColor.BLUE);
        final ListView<FlatColor> colorList = new ListView<FlatColor>();
        colorList.setModel(colorModel);
        colorList.setColumnWidth(20);
        colorList.setRowHeight(20);
        colorList.setOrientation(ListView.Orientation.HorizontalWrap);
        colorList.setRenderer(new ListView.ItemRenderer<FlatColor>() {
            public void draw(GFX gfx, ListView listView, FlatColor flatColor, int i, double x, double y, double w, double h) {
                gfx.setPaint(flatColor);
                gfx.fillRect(x, y, w, h);
            }
        });
        EventBus.getSystem().addListener(colorList, SelectionEvent.Changed, new Callback<SelectionEvent>(){
            public void call(SelectionEvent e) throws Exception {
                int n = e.getView().getSelectedIndex();
                setSelectedFill(colorList.getModel().get(n));
                popup.setVisible(false);
            }
        });

        Button addButton = new Button("+");
        addButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                final Stage dialog = Stage.createStage();
                dialog.setTitle("Color");

                final ColorPickerPanel picker = new ColorPickerPanel();

                Callback<ActionEvent> okay = new Callback<ActionEvent>() {
                    public void call(ActionEvent event) {
                        FlatColor color = picker.getColor();
                        colorModel.add(color);
                        dialog.hide();
                    }
                };
                Callback<ActionEvent> canceled = new Callback<ActionEvent>() {
                    public void call(ActionEvent event) {
                        dialog.hide();
                    }
                };
                dialog.setContent(new VFlexBox()
                        .add(picker)
                        .add(new HFlexBox()
                                .add(new Button("okay").onClicked(okay))
                                .add(new Button("cancel").onClicked(canceled))
                        )
                );
                dialog.setWidth(400);
                dialog.setHeight(370);
                dialog.centerOnScreen();
            }
        });

        VFlexBox vbox = new VFlexBox();
        vbox.setFill(FlatColor.GRAY);
        vbox.add(colorList, 1);
        vbox.add(new HFlexBox().add(addButton));
        vbox.setBoxAlign(FlexBox.Align.Stretch);
        panel.add("Swatches", vbox);
    }

    public void setSelectedFill(Paint paint) {
        this.selectedFill = paint;
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.ObjectChanged, selectedFill, this));
        setDrawingDirty();
    }
}

