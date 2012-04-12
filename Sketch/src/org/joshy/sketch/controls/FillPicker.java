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
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;
import org.joshy.gfx.draw.LinearGradientFill.Snap;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.*;

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
    private Main manager;
    private FreerangeColorPickerPopup freerangeColorPickerPopup;
    private boolean locked = false;
    private boolean popupadded;
    private ColorPickerPanel rgbhsvpicker;
    private VectorDocContext context;

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

    public FillPicker(Main manager) {
        super("X");
        this.manager = manager;
        setPrefWidth(25);
        setPrefHeight(25);
        selectedFill = FlatColor.RED;
        try {
            popup = buildPanel();
            popup.setVisible(false);
            popupadded = false;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    protected void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (pressed) {
            if (!popupadded) {
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

            if(paint instanceof PatternPaint) {
                PatternPaint pp = (PatternPaint) paint;
                double pw = pp.getImage().getWidth();
                double ph = pp.getImage().getHeight();
                double sx = w/pw;
                double sy = h/ph;
                gfx.scale(sx,sy);
                gfx.setPaint(pp);
                gfx.fillRect(0,0,pw,ph);
                gfx.scale(1/sx,1/sy);
            } else {
                gfx.setPaint(paint);
                gfx.fillRect(0,0,w,h);
            }

            gfx.setPaint(FlatColor.BLACK);
            gfx.drawRect(0,0,w,h);
            gfx.translate(-x,-y);
        }
    };

    private TabPanel buildPanel() throws IOException {
        final TabPanel panel = new TabPanel();
        panel.setPrefWidth(300);
        panel.setPrefHeight(250);

        setupColorTab(panel);
        setupSwatchTab(panel);
        setupRGBTab(panel);
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



        final ListView<Paint> patternList = new ListView<Paint>()
                .setModel(manager.patternManager.getModel())
                .setColumnWidth(size)
                .setRowHeight(size)
                .setOrientation(ListView.Orientation.HorizontalWrap)
                .setRenderer(paintItemRenderer)
                ;
        EventBus.getSystem().addListener(patternList, SelectionEvent.Changed, new Callback<SelectionEvent>() {
            public void call(SelectionEvent e) throws Exception {
                int n = e.getView().getSelectedIndex();
                setSelectedFill(patternList.getModel().get(n));
                popup.setVisible(false);
            }
        });
        Button addButton = new Button("add image");
        addButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                FileDialog fd = new FileDialog((Frame) null);
                fd.setMode(FileDialog.LOAD);
                fd.setTitle("Open Pattern Image");
                fd.setVisible(true);
                if (fd.getFile() != null) {
                    File file = new File(fd.getDirectory(), fd.getFile());
                    u.p("opening a file" + file);
                    try {
                        PatternPaint pat = PatternPaint.create(file);
                        manager.patternManager.addPattern(pat);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        Button createButton = new Button("create new");
        createButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) throws Exception {
                final PatternBuilder builder = new PatternBuilder();
                final Stage stage = Stage.createStage();
                Callback<ActionEvent> closeAction = new Callback<ActionEvent>() {
                    public void call(ActionEvent event) throws Exception {
                        PatternPaint pattern = builder.getPattern();
                        manager.patternManager.addPattern(pattern);
                        stage.hide();
                    }
                };
                Callback<ActionEvent> cancelAction = new Callback<ActionEvent>() {
                    public void call(ActionEvent event) throws Exception {
                        stage.hide();
                    }
                };
                stage.setContent(new VFlexBox()
                        .add(builder, 1)
                        .add(new HFlexBox()
                                .add(new Button("cancel").onClicked(cancelAction), 0)
                                .add(new Button("save").onClicked(closeAction), 0)
                                ,0)
                );
                stage.setWidth(600);
                stage.setHeight(350);
                stage.centerOnScreen();
            }
        });
        VFlexBox vbox = new VFlexBox();
        vbox.setBoxAlign(FlexBox.Align.Stretch);
        vbox.setFill(FlatColor.GRAY);
        vbox.add(patternList, 1);
        vbox.add(new HFlexBox().add(addButton).add(createButton));
        panel.add("Patterns", vbox);
    }

    private ListView<Paint> setupGradientTab(TabPanel panel) {
        double size = 40;
        //linears
        Paint gf1 = new LinearGradientFill()
                .setStartX(0)
                .setStartXSnapped(Snap.Start)
                .setEndX(size)
                .setEndXSnapped(Snap.End)
                .setStartY(size/2)
                .setStartYSnapped(Snap.Middle)
                .setEndY(size/2)
                .setEndYSnapped(Snap.Middle)
                .addStop(0,FlatColor.BLACK)
                .addStop(1,FlatColor.WHITE);

        Paint gf2 = new LinearGradientFill()
                .setStartX(size/2).setStartXSnapped(Snap.Middle)
                .setEndX(size/2).setEndXSnapped(Snap.Middle)
                .setStartY(0).setStartYSnapped(Snap.Start)
                .setEndY(size).setEndYSnapped(Snap.End)
                .addStop(0,FlatColor.BLACK)
                .addStop(1,FlatColor.WHITE);

        Paint gf3 = new LinearGradientFill()
                .setStartX(0).setStartXSnapped(Snap.Start)
                .setEndX(size).setEndXSnapped(Snap.End)
                .setStartY(0).setStartYSnapped(Snap.Start)
                .setEndY(size).setEndYSnapped(Snap.End)
                .addStop(0,FlatColor.BLACK)
                .addStop(1,FlatColor.WHITE);

        //linears 2
        Paint gf6 = new LinearGradientFill()
                .setStartX(0).setStartXSnapped(Snap.Start)
                .setStartY(size / 2).setStartYSnapped(Snap.Middle)
                .setEndX(size).setEndXSnapped(Snap.End)
                .setEndY(size / 2).setEndYSnapped(Snap.Middle)
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

    private void setupColorTab(TabPanel panel) {
        freerangeColorPickerPopup = new FreerangeColorPickerPopup(null,300,170,false);
        freerangeColorPickerPopup.setOutsideColorProvider(new FreerangeColorPickerPopup.OutsideColorProvider(){
            @Override
            public FlatColor getColorAt(MouseEvent event) {
                if(context == null) return super.getColorAt(event);
                Point2D pt = event.getPointInNodeCoords(context.getSketchCanvas());
                pt = context.getSketchCanvas().transformToCanvas(pt.getX(), pt.getY());
                java.util.List<SNode> underCursor = new ArrayList<SNode>();
                for(SNode node : context.getDocument().getCurrentPage().getNodes()) {
                    if(node.getTransformedBounds().contains(pt)) {
                        underCursor.add(node);
                    }
                }
                if(underCursor.isEmpty()) {
                } else {
                    SNode node = underCursor.get(underCursor.size() - 1);
                    if(node instanceof SShape) {
                        SShape shape = ((SShape)node);
                        if(shape.getFillPaint() instanceof FlatColor) {
                            return (FlatColor) shape.getFillPaint();
                        }
                    }
                }

                return super.getColorAt(event);    //To change body of overridden methods use File | Settings | File Templates.
            }
        });
        EventBus.getSystem().addListener(freerangeColorPickerPopup, ChangedEvent.ColorChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) throws Exception {
                locked = true;
                setSelectedFill((FlatColor)event.getValue());
                locked = false;
                if(!event.isAdjusting()) {
                    popup.setVisible(false);
                }
            }
        });
        panel.add("Color",freerangeColorPickerPopup);
    }


    private void setupRGBTab(TabPanel panel) {
        rgbhsvpicker = new ColorPickerPanel(280,250);
        panel.add("RGB/HSV", rgbhsvpicker);
        EventBus.getSystem().addListener(rgbhsvpicker, ChangedEvent.ColorChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent changedEvent) throws Exception {
                locked = true;
                setSelectedFill((FlatColor)changedEvent.getValue());
                if(!changedEvent.isAdjusting()) {
                    popup.setVisible(false);
                }
                locked = false;
            }
        });

    }

    private void setupSwatchTab(TabPanel panel) {
        final ListView<FlatColor> colorList = new ListView<FlatColor>();
        colorList.setModel(manager.colorManager.getSwatchModel());
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
                        manager.colorManager.addSwatch(color);
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
        if(!locked) {
            if(paint instanceof FlatColor) {
                freerangeColorPickerPopup.setSelectedColor((FlatColor)paint);
                rgbhsvpicker.setSelectedColor((FlatColor)paint);
            }
        }
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.ObjectChanged, selectedFill, this));
        setDrawingDirty();
    }

    public void hidePopups() {
        if(popup != null && popup.isVisible()) {
            popup.setVisible(false);
        }
    }

    public void setContext(VectorDocContext context) {
        this.context = context;
    }
}

