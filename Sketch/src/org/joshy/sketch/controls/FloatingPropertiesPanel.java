package org.joshy.sketch.controls;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.*;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.FlexBox;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.BooleanGeometry;
import org.joshy.sketch.actions.NodeActions;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.property.PropertyManager;

import java.awt.geom.Point2D;
import java.io.IOException;

import static org.joshy.gfx.util.localization.Localization.getString;

public class FloatingPropertiesPanel extends VFlexBox {
    private boolean selected = false;
    private Selection selection;
    private Slider fillOpacitySlider;
    private SwatchColorPicker strokeColorButton;
    private Slider strokeWidthSlider;
    private Slider fontSizeSlider;
    private Togglebutton fontBoldButton;
    private Togglebutton fontItalicButton;
    private Main manager;
    private PopupMenuButton<String> fontPicker;
    private Label fillOpacityLabel;
    private Label strokeWidthLabel;
    private VectorDocContext context;
    private PopupMenuButton<SArrow.HeadEnd> arrowHeadEnd;
    private Label rgbLabel;
    private Label fontSizeLabel;
    public static final boolean TRUE_PALLETTE = false;//OSUtil.isMac();
    private boolean locked = false;
    private FlexBox groupPropertiesEditor;
    private FlexBox shapeProperties;
    private FlexBox shadowProperties;
    private FlexBox fontProperties;
    private FlexBox booleanPropsEditor;
    private FillPicker fillButton;
    private SwatchColorPicker shadowColorButton;


    public FloatingPropertiesPanel(final Main manager, final VectorDocContext context) throws IOException {
        this.context = context;

        setFill(FlatColor.RED);
        this.manager = manager;
        selected = false;


        EventBus.getSystem().addListener(Selection.SelectionChangeEvent.Changed, selectionCallback);


        shapeProperties = new HFlexBox().setBoxAlign(HFlexBox.Align.Baseline);
        add(shapeProperties);
        shapeProperties.setVisible(false);

        fillButton = new FillPicker();
        EventBus.getSystem().addListener(fillButton,ChangedEvent.ObjectChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) throws Exception {
                if(locked) return;
                if(event.getSource() == fillButton) {
                    Paint color = (Paint) event.getValue();
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        setFillStuff(color);
                        Selection sel = context.getSelection();
                        if(sel.size() == 1){
                            sel.regenHandles(sel.firstItem());
                        }
                    }
                    context.redraw();
                }

            }
        });

        strokeColorButton = new SwatchColorPicker();
        EventBus.getSystem().addListener(ChangedEvent.ColorChanged, colorChangeCallback);
        shapeProperties.add(fillButton);
        shapeProperties.add(strokeColorButton);



        shadowProperties = new HFlexBox().setBoxAlign(HFlexBox.Align.Baseline);
        add(shadowProperties);
        shadowProperties.setVisible(false);
        final Checkbox dropShadow = new Checkbox("shadow");
        EventBus.getSystem().addListener(dropShadow, ActionEvent.Action, new Callback<ActionEvent>(){
            public void call(ActionEvent actionEvent) throws Exception {
                if(selection != null) {
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        if(dropShadow.isSelected()) {
                            manager.propMan.getProperty("shadow").setValue(new DropShadow());
                        } else {
                            manager.propMan.getProperty("shadow").setValue(null);
                        }
                        context.redraw();
                    }
                }
            }
        });
        shadowProperties.add(dropShadow);
        shadowProperties.add(new Label("offset"));
        final SpinBox<Double> xoffBox = new SpinBox<Double>();
        xoffBox.setValue(5.0);
        shadowProperties.add(xoffBox);
        xoffBox.onChanged(new Callback<ChangedEvent>(){
            public void call(ChangedEvent event) throws Exception {
                if(selection != null) {
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        double v = (Double)event.getValue();
                        DropShadow shadow = (DropShadow) manager.propMan.getProperty("shadow").getValue();
                        shadow = shadow.setXOffset(v);
                        manager.propMan.getProperty("shadow").setValue(shadow);
                        context.redraw();
                    }
                }
            }
        });


        final SpinBox<Double> yoffBox = new SpinBox<Double>();
        yoffBox.setValue(5.0);
        yoffBox.onChanged(new Callback<ChangedEvent>(){
            public void call(ChangedEvent event) throws Exception {
                if(selection != null) {
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        double v = (Double)event.getValue();
                        DropShadow shadow = (DropShadow) manager.propMan.getProperty("shadow").getValue();
                        shadow = shadow.setYOffset(v);
                        manager.propMan.getProperty("shadow").setValue(shadow);
                        context.redraw();
                    }
                }
            }
        });
        shadowProperties.add(yoffBox);

        shadowProperties.add(new Label("radius"));
        final SpinBox<Integer> blurRadius = new SpinBox<Integer>();
        blurRadius.setValue(3);
        blurRadius.onChanged(new Callback<ChangedEvent>(){
            public void call(ChangedEvent event) throws Exception {
                if(selection != null) {
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        int v = (Integer)event.getValue();
                        DropShadow shadow = (DropShadow) manager.propMan.getProperty("shadow").getValue();
                        shadow = shadow.setBlurRadius(v);
                        manager.propMan.getProperty("shadow").setValue(shadow);
                        context.redraw();
                    }
                }
            }
        });
        shadowProperties.add(blurRadius);
        shadowColorButton = new SwatchColorPicker();
        shadowProperties.add(shadowColorButton);
        shadowProperties.add(new Label("opacity"));
        final Slider shadowOpacity = new Slider(false).setMin(0).setMax(100).setValue(100);
        EventBus.getSystem().addListener(shadowOpacity, ChangedEvent.DoubleChanged, new Callback<ChangedEvent>(){
            public void call(ChangedEvent event) throws Exception {
                if(selection != null) {
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        double v = (Double)event.getValue();
                        DropShadow shadow = (DropShadow) manager.propMan.getProperty("shadow").getValue();
                        shadow = shadow.setOpacity(v/100);
                        manager.propMan.getProperty("shadow").setValue(shadow);
                        context.redraw();
                    }
                }
            }
        });
        shadowProperties.add(shadowOpacity);





        fillOpacitySlider = new Slider(false).setMin(0).setMax(100).setValue(100);
        EventBus.getSystem().addListener(fillOpacitySlider, ChangedEvent.DoubleChanged, fillOpacityCallback);
        fillOpacityLabel = new Label(getString("toolbar.opacity")+":");
        shapeProperties.add(fillOpacityLabel);
        shapeProperties.add(fillOpacitySlider);

        strokeWidthSlider = new Slider(false);
        strokeWidthSlider.setMin(0);
        strokeWidthSlider.setMax(20);
        strokeWidthSlider.setValue(3);
        EventBus.getSystem().addListener(strokeWidthSlider, ChangedEvent.DoubleChanged, strokeWidthCallback);
        strokeWidthLabel = new Label(getString("toolbar.stroke")+":");
        shapeProperties.add(strokeWidthLabel);
        shapeProperties.add(strokeWidthSlider);




        fontProperties = new HFlexBox().setBoxAlign(HFlexBox.Align.Baseline);
        add(fontProperties);

        fontSizeLabel = new Label(getString("toolbar.fontSize")+":");
        fontProperties.add(fontSizeLabel);

        fontSizeSlider = new Slider(false);
        fontSizeSlider.setMin(8);
        fontSizeSlider.setMax(200);
        fontSizeSlider.setValue(24);
        EventBus.getSystem().addListener(fontSizeSlider, ChangedEvent.DoubleChanged, fontSizeCallback);
        fontProperties.add(fontSizeSlider);

        fontPicker = new PopupMenuButton<String>();
        fontPicker.setModel(new ListModel<String>() {
            public String get(int i) {
                return Main.fonts[i].getName();
            }

            public int size() {
                return Main.fonts.length;
            }
        });
        EventBus.getSystem().addListener(fontPicker, SelectionEvent.Changed, new Callback<SelectionEvent>(){
            public void call(SelectionEvent event) {
                if(manager.propMan.isClassAvailable(SText.class)) {
                    int index = event.getView().getSelectedIndex();
                    manager.propMan.getProperty("fontName").setValue(Main.fonts[index].getName());
                    context.redraw();
                }
            }
        });
        fontProperties.add(fontPicker);

        fontBoldButton = new Togglebutton("B");
        fontBoldButton.onClicked(fontBoldCallback);
        fontProperties.add(fontBoldButton);

        fontItalicButton = new Togglebutton("I");
        fontItalicButton.onClicked(fontItalicCallback);
        fontProperties.add(fontItalicButton);



        arrowHeadEnd = new PopupMenuButton<SArrow.HeadEnd>();
        arrowHeadEnd.setModel(ListView.createModel(SArrow.HeadEnd.values()));
        add(arrowHeadEnd);
        arrowHeadEnd.setVisible(false);
        EventBus.getSystem().addListener(arrowHeadEnd, SelectionEvent.Changed, arrowHeadCallback);


        groupPropertiesEditor = new HFlexBox()
                .add(new IB("align-horizontal-left.png", new NodeActions.AlignLeft(context)))
                .add(new IB("align-horizontal-center.png", new NodeActions.AlignCenterH(context)))
                .add(new IB("align-horizontal-right.png", new NodeActions.AlignRight(context)))
                .add(new IB("align-vertical-bottom.png", new NodeActions.AlignBottom(context)))
                .add(new IB("align-vertical-center.png", new NodeActions.AlignCenterV(context)))
                .add(new IB("align-vertical-top.png", new NodeActions.AlignTop(context)))
                ;
        add(groupPropertiesEditor);
        groupPropertiesEditor.setVisible(false);
        booleanPropsEditor = new HFlexBox()
                .add(new Button(getString("menus.add")).onClicked(new Callback<ActionEvent>() {
                    public void call(ActionEvent actionEvent) throws Exception {
                        new BooleanGeometry.Union(context).execute();
                    }
                }))
                .add(new Button(getString("menus.subtract")).onClicked(new Callback<ActionEvent>() {
                    public void call(ActionEvent actionEvent) throws Exception {
                        new BooleanGeometry.Subtract(context).execute();
                    }
                }))
                .add(new Button(getString("menus.intersection")).onClicked(new Callback<ActionEvent>() {
                    public void call(ActionEvent actionEvent) throws Exception {
                        new BooleanGeometry.Intersection(context).execute();
                    }
                }))
        ;
        add(booleanPropsEditor);
        booleanPropsEditor.setVisible(false);

        this.setBoxAlign(Align.Left);
        setVisible(false);

    }

    private static class IB extends ToolbarButton {
        private NodeActions.MultiNodeAction action;

        private IB(String s, NodeActions.MultiNodeAction act) throws IOException {
            super(Main.class.getResource("resources/"+s));
            this.selectable = false;
            this.action = act;
            onClicked(new Callback<ActionEvent>(){
                public void call(ActionEvent actionEvent) throws Exception {
                    action.execute();
                }
            });
        }

        @Override
        public void draw(GFX g) {
            g.setPaint(FlatColor.WHITE);
            g.fillRect(0,0,getWidth(),getHeight());
            //g.setPaint(FlatColor.BLACK);
            //g.drawRect(0,0,getWidth(),getHeight());

            g.drawImage(icon,0,0);
        }
    }

    private void updatePanelContents() {
        locked = true;
        SNode lastNode = null;
        for(SNode node : selection.items()) {
            lastNode = node;
        }

        if(manager.propMan.isClassAvailable(SText.class)) {
            fontProperties.setVisible(true);
            double dval = manager.propMan.getProperty("fontSize").getDoubleValue();
            if(selection.size() == 1) {
                fontSizeSlider.setValue(dval);
            }
        } else {
            fontProperties.setVisible(false);
        }

        if(manager.propMan.isClassAvailable(SArrow.class)) {
            arrowHeadEnd.setVisible(true);
        } else {
            arrowHeadEnd.setVisible(false);
        }

        if(manager.propMan.isClassAvailable(SShape.class)) {
            shapeProperties.setVisible(true);
            shadowProperties.setVisible(true);
            /*
            if(lastNode != null) {
                PropertyManager.Property fillColorProp = manager.propMan.getProperty("fillPaint");
                if(fillColorProp.hasSingleValue()) {
                    Object val = fillColorProp.getValue();
                    if(val instanceof FlatColor) {
                        colorButton.setSelectedColor((FlatColor)val);
                    } else {
                        colorButton.setSelectedColor(gradient1);
                    }
                }

                PropertyManager.Property fillOpacityProp = manager.propMan.getProperty("fillOpacity");
                if(fillOpacityProp.hasSingleValue()) {
                    fillOpacitySlider.setValue(fillOpacityProp.getDoubleValue()*100.0);
                }
            }*/
        } else {
            shapeProperties.setVisible(false);
            shadowProperties.setVisible(false);
        }
        if(manager.propMan.isClassAvailable(SShape.class) ||
            manager.propMan.isClassAvailable(SImage.class)) {
            strokeColorButton.setVisible(true);
            strokeWidthSlider.setVisible(true);
            strokeWidthLabel.setVisible(true);
            if(lastNode != null) {
                PropertyManager.Property strokeProp = manager.propMan.getProperty("strokeWidth");
                if(strokeProp.hasSingleValue()) {
                    strokeWidthSlider.setValue(strokeProp.getDoubleValue());
                }
                PropertyManager.Property strokeColorProp = manager.propMan.getProperty("strokePaint");
                if(strokeColorProp.hasSingleValue()) {
                    strokeColorButton.setSelectedColor((FlatColor)strokeColorProp.getValue());
                }
            }
        } else {
            strokeColorButton.setVisible(false);
            strokeWidthSlider.setVisible(false);
            strokeWidthLabel.setVisible(false);
        }

        groupPropertiesEditor.setVisible(selection.size()>1);
        booleanPropsEditor.setVisible(selection.size()>1 && manager.propMan.isClassAvailable(SShape.class));

        setLayoutDirty();
        Core.getShared().defer(new Runnable() {
            public void run() {
                locked = false;
            }
        });
    }


    @Override
    protected void drawSelf(GFX g) {
        double h = getHeight();
        g.setPaint(new FlatColor(0.4,0.4,0.4,0.5));
        g.fillRoundRect(0,0,getWidth()-1,h-1,10,10);
        g.setPaint(new FlatColor(0,0,0,0.8));
        g.drawRoundRect(0,0,getWidth()-1,h-1,10,10);
    }
    @Override
    public void doLayout() {
        if(selected) {
            Bounds bounds = selection.calculateBounds();
            bounds = context.getSketchCanvas().transformToDrawing(bounds);
            Point2D pt = NodeUtils.convertToScene(context.getSketchCanvas(), bounds.getX(), bounds.getY());
            if(TRUE_PALLETTE) {
                Stage s = getParent().getStage();
                Stage cs = context.getStage();
                s.setWidth(400);
                s.setHeight(230);
                pt = new Point2D.Double(pt.getX()+cs.getX(),pt.getY()+cs.getY());
                s.setX(pt.getX());
                s.setY(pt.getY()+bounds.getHeight()+40);
            } else {
                setTranslateX((int)pt.getX());
                setTranslateY((int)(pt.getY() + bounds.getHeight()+20));
            }
            super.doLayout();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        setLayoutDirty();
    }

    Callback<Selection.SelectionChangeEvent> selectionCallback = new Callback<Selection.SelectionChangeEvent>() {
        public void call(Selection.SelectionChangeEvent event) throws Exception {
            //only pay attention to events for our own context doc
            if(event.getSelection().getDocument() != context.getDocument()) return;
            if(!manager.propMan.isClassAvailable(SNode.class)) return;
            selected = !event.getSelection().isEmpty();
            selection = event.getSelection();
            updatePanelContents();
            setDrawingDirty();
            setLayoutDirty();
        }
    };

    SwatchColorPicker.ColorCallback colorCallback = new SwatchColorPicker.ColorCallback() {
        public FlatColor call(MouseEvent event) {
            SketchDocument doc = context.getDocument();
            Stage stage = context.getSketchCanvas().getParent().getStage();
            Point2D point = event.getPointInScreenCoords();
            point = new Point2D.Double(point.getX()-stage.getX(),point.getY()-stage.getY());
            point = NodeUtils.convertFromScene(context.getCanvas(), point);
            if(rgbLabel == null) {
                rgbLabel = new Label("RGB");
                rgbLabel.setId("rgblabel");
                stage.getPopupLayer().add(rgbLabel);
                rgbLabel.setTranslateX(100);
                rgbLabel.setTranslateY(100);
            }
            rgbLabel.setVisible(true);
            Point2D dx = event.getPointInScreenCoords();

            rgbLabel.setTranslateX(dx.getX()-stage.getX()+70);
            rgbLabel.setTranslateY(dx.getY()-stage.getY());
            for(SNode node : doc.getCurrentPage().model) {
                if(node.contains(point) && node instanceof SShape) {
                    FlatColor color =  (FlatColor) ((SShape)node).getFillPaint();
                    int val = color.getRGBA();
                    val = val & 0x00FFFFFF;
                    rgbLabel.setText("RGB: " + Integer.toHexString(val).toUpperCase());
                    return color;
                }
            }
            rgbLabel.setText("RGB: ------");
            return FlatColor.BLUE;
        }
    };


    private Callback<? extends Event> colorChangeCallback = new Callback<ChangedEvent>() {
        public void call(ChangedEvent event) {
            if(locked) return;
            if(event.getSource() == strokeColorButton) {
                if(manager.propMan.isClassAvailable(SShape.class) ||
                        manager.propMan.isClassAvailable(SImage.class)) {
                    manager.propMan.getProperty("strokePaint").setValue(event.getValue());
                    context.redraw();
                }
            }
            if(event.getSource() == shadowColorButton) {
                if(manager.propMan.isClassAvailable(SShape.class)) {
                    DropShadow shadow = (DropShadow) manager.propMan.getProperty("shadow").getValue();
                    shadow = shadow.setColor(shadowColorButton.getSelectedColor());
                    manager.propMan.getProperty("shadow").setValue(shadow);
                    context.redraw();
                }
            }
        }
    };

    private void setFillStuff(Paint paint) {
        //handle the new form from the fill selector
        PropertyManager.Property prop = manager.propMan.getProperty("fillPaint");
        if(paint instanceof GradientFill) {
            GradientFill grad = (GradientFill) paint;
            Bounds std = new Bounds(0,0,40,40);
            SShape shape = (SShape) context.getSelection().firstItem();
            prop.setValue(grad.resize(std, shape.getBounds()));
            return;
        }
        //if just a normal color
        paint = paint.duplicate();
        prop.setValue(paint);
    }

    private Callback<ChangedEvent> fillOpacityCallback = new Callback<ChangedEvent>() {
        public void call(ChangedEvent event) throws Exception {
            if(selection != null) {
                if(manager.propMan.isClassAvailable(SShape.class)) {
                    double v = (Double)event.getValue();
                    manager.propMan.getProperty("fillOpacity").setValue(v/100.0);
                    context.redraw();
                }
            }
        }
    };

    private Callback<ChangedEvent>  fontSizeCallback = new Callback<ChangedEvent>() {
        public void call(ChangedEvent event) throws Exception {
            if(selection != null) {
                for(SNode node: selection.items()) {
                    if(node instanceof SText) {
                        SText text = (SText) node;
                        text.setFontSize(((Double)event.getValue()));
                        if(fontBoldButton.isSelected()) {
                            text.setWeight(Font.Weight.Bold);
                        } else {
                            text.setWeight(Font.Weight.Regular);
                        }
                    }
                }
                context.redraw();
            }
        }
    };

    private Callback<? extends Event> strokeWidthCallback = new Callback<ChangedEvent>() {
        public void call(ChangedEvent event) {
            if(manager.propMan.isClassAvailable(SShape.class) ||
                    manager.propMan.isClassAvailable(SImage.class)) {
                int sw = (int)((Double)event.getValue()).doubleValue();
                manager.propMan.getProperty("strokeWidth").setValue(sw);
                context.redraw();
            }
        }
    };

    private Callback<ActionEvent> fontBoldCallback = new Callback<ActionEvent>() {
        public void call(ActionEvent event) {
            if(selection != null) {
                for(SNode node: selection.items()) {
                    if(node instanceof SText) {
                        SText text = (SText) node;
                        if(fontBoldButton.isSelected()) {
                            text.setWeight(Font.Weight.Bold);
                        } else {
                            text.setWeight(Font.Weight.Regular);
                        }
                    }
                }
                context.redraw();
            }

        }
    };

    private Callback<ActionEvent> fontItalicCallback = new Callback<ActionEvent>() {
        public void call(ActionEvent event) {
            if(selection != null) {
                for(SNode node: selection.items()) {
                    if(node instanceof SText) {
                        SText text = (SText) node;
                        if(fontItalicButton.isSelected()) {
                            text.setStyle(Font.Style.Italic);
                        } else {
                            text.setStyle(Font.Style.Regular);
                        }
                    }
                }
                context.redraw();
            }
        }
    };

    private Callback<? extends Event> arrowHeadCallback = new Callback<SelectionEvent>() {
        public void call(SelectionEvent event) {
            if(manager.propMan.isClassAvailable(SArrow.class)) {
                int index = event.getView().getSelectedIndex();
                manager.propMan.getProperty("headEnd").setValue(SArrow.HeadEnd.values()[index]);
            }
        }
    };

}
