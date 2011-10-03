package org.joshy.sketch.controls;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.*;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.FlexBox;
import org.joshy.gfx.node.layout.GridBox;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.BooleanGeometry;
import org.joshy.sketch.actions.NodeActions;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.property.PropertyManager;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.DecimalFormat;

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

    private GridBox shadowProperties;
    private Checkbox shadowSet;
    private Checkbox shadowInner;
    private SwatchColorPicker shadowColorButton;
    private SpinBox<Double> shadowXoff;
    private SpinBox<Double> shadowYoff;
    private SpinBox<Integer> shadowBlurRadius;
    private Slider shadowOpacity;

    private FlexBox fontProperties;
    private FlexBox booleanPropsEditor;
    private FillPicker fillButton;
    private Togglebutton fontAlignLeft;
    private Togglebutton fontAlignHCenter;
    private Togglebutton fontAlignRight;
    private ToggleGroup fontAlignGroup;
    private DecimalFormat df;
    private DecimalFormat intFormat;
    private FlexBox defaultProperties;
    private Textbox nameBox;


    public FloatingPropertiesPanel(final Main manager, final VectorDocContext context) throws IOException {
        this.context = context;

        df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);

        intFormat = new DecimalFormat();
        intFormat.setMinimumIntegerDigits(2);


        setFill(FlatColor.RED);
        this.manager = manager;
        selected = false;


        EventBus.getSystem().addListener(Selection.SelectionChangeEvent.Changed, selectionCallback);

        defaultProperties = new HFlexBox().setBoxAlign(Align.Baseline);

        add(defaultProperties);
        defaultProperties.add(new Label("Name:").setFont(Font.name("OpenSans").size(12).resolve()));
        nameBox =  new Textbox("").setHintText("unset");
        nameBox.setPrefWidth(100);
        nameBox.onAction(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                if(manager.propMan.isClassAvailable(SNode.class)) {
                    PropertyManager.Property prop = manager.propMan.getProperty("id");
                    prop.setValue(nameBox.getText());
                }
            }
        });
        defaultProperties.add(nameBox,1);

        shapeProperties = new HFlexBox().setBoxAlign(HFlexBox.Align.Baseline);
        add(shapeProperties);
        shapeProperties.setVisible(false);

        fillButton = new FillPicker(this.manager);
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


        setupShadowProperties();

        strokeWidthSlider = new Slider(false);
        strokeWidthSlider.setMin(0);
        strokeWidthSlider.setMax(20);
        strokeWidthSlider.setValue(3);
        EventBus.getSystem().addListener(strokeWidthSlider, ChangedEvent.DoubleChanged, strokeWidthCallback);
        strokeWidthLabel = new Label(getString("toolbar.stroke")+": " + 3);
        shapeProperties.add(strokeWidthLabel);
        shapeProperties.add(strokeWidthSlider);

        fillOpacitySlider = new Slider(false).setMin(0).setMax(100).setValue(100);
        EventBus.getSystem().addListener(fillOpacitySlider, ChangedEvent.DoubleChanged, fillOpacityCallback);
        fillOpacityLabel = new Label(getString("toolbar.opacity")+": "+df.format(0));
        shapeProperties.add(fillOpacityLabel);
        shapeProperties.add(fillOpacitySlider);





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
                return Main.fontList.get(i);
            }

            public int size() {
                return Main.fontList.size();
            }
        });
        EventBus.getSystem().addListener(fontPicker, SelectionEvent.Changed, new Callback<SelectionEvent>(){
            public void call(SelectionEvent event) {
                if(manager.propMan.isClassAvailable(SText.class)) {
                    int index = event.getView().getSelectedIndex();
                    manager.propMan.getProperty("fontName").setValue(Main.fontList.get(index));
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

        fontAlignLeft = new TIB("cr22-action-text_left.png", new SAction(){
            @Override
            public void execute() throws Exception {
                if(manager.propMan.isClassAvailable(SText.class)) {
                    manager.propMan.getProperty("halign").setValue(SText.HAlign.Left);
                }
            }
        });
        fontAlignHCenter = new TIB("cr22-action-text_center.png", new SAction(){
            @Override
            public void execute() throws Exception {
                if(manager.propMan.isClassAvailable(SText.class)) {
                    manager.propMan.getProperty("halign").setValue(SText.HAlign.Center);
                }
            }
        });
        fontAlignRight = new TIB("cr22-action-text_right.png", new SAction(){
            @Override
            public void execute() throws Exception {
                if(manager.propMan.isClassAvailable(SText.class)) {
                    manager.propMan.getProperty("halign").setValue(SText.HAlign.Right);
                }
            }
        });
        fontProperties.add(fontAlignLeft,fontAlignHCenter,fontAlignRight);
        fontAlignGroup = new ToggleGroup();
        fontAlignGroup.add(fontAlignLeft).add(fontAlignHCenter).add(fontAlignRight);
        fontAlignGroup.setSelectedButton(fontAlignLeft);
        EventBus.getSystem().addListener(fontAlignGroup, ActionEvent.Action, new Callback<ActionEvent>(){
            public void call(ActionEvent actionEvent) throws Exception {
                if(manager.propMan.isClassAvailable(SText.class)) {
                    if(fontAlignGroup.getSelectedButton() == fontAlignLeft) {
                        manager.propMan.getProperty("halign").setValue(SText.HAlign.Left);
                    }
                    if(fontAlignGroup.getSelectedButton() == fontAlignHCenter) {
                        manager.propMan.getProperty("halign").setValue(SText.HAlign.Center);
                    }
                    if(fontAlignGroup.getSelectedButton() == fontAlignRight) {
                        manager.propMan.getProperty("halign").setValue(SText.HAlign.Right);
                    }
                    context.redraw();
                }
            }
        });


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

    private void setupShadowProperties() {
        shadowProperties = new GridBox();
        shadowProperties.debug(false);
        shadowProperties.setPrefWidth(200);
        shadowProperties.setPrefHeight(130);
        shadowProperties.setPadding(1);
        shadowProperties.createColumn(70,GridBox.Align.Right);
        shadowProperties.createColumn(60,GridBox.Align.Left);
        shadowProperties.createColumn(60,GridBox.Align.Left);
        add(shadowProperties);
        shadowProperties.setVisible(false);

        shadowSet = new Checkbox("shadow");
        EventBus.getSystem().addListener(shadowSet, ActionEvent.Action, new Callback<ActionEvent>(){
            public void call(ActionEvent actionEvent) throws Exception {
                if(selection != null) {
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        if(shadowSet.isSelected()) {
                            manager.propMan.getProperty("shadow").setValue(new DropShadow());
                        } else {
                            manager.propMan.getProperty("shadow").setValue(null);
                        }
                        context.redraw();
                    }
                }
            }
        });
        shadowProperties.add(shadowSet);
        shadowInner = new Checkbox("inner");
        EventBus.getSystem().addListener(shadowInner, ActionEvent.Action, new Callback<ActionEvent>(){
            public void call(ActionEvent actionEvent) throws Exception {
                if(selection != null) {
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        DropShadow shad = (DropShadow) manager.propMan.getProperty("shadow").getValue();
                        if(shad !=  null) {
                            shad.setInner(shadowInner.isSelected());
                        }
                        manager.propMan.getProperty("shadow").setValue(shad);
                        context.redraw();
                    }
                }
            }
        });
        shadowProperties.add(shadowInner);
        shadowProperties.nextRow();


        shadowProperties.add(new Label("Offset:"));
        shadowXoff = new SpinBox<Double>()
            .setValue(5.0);
        shadowProperties.add(shadowXoff);
        shadowXoff.onChanged(new Callback<ChangedEvent>(){
            public void call(ChangedEvent event) throws Exception {
                if(manager.propMan.isPropertyNotNull("shadow")) {
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


        shadowYoff = new SpinBox<Double>()
            .setValue(5.0);
        shadowYoff.onChanged(new Callback<ChangedEvent>(){
            public void call(ChangedEvent event) throws Exception {
                if(manager.propMan.isPropertyNotNull("shadow")) {
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
        shadowProperties.add(shadowYoff);
        shadowProperties.nextRow();

        shadowProperties.add(new Label("Blur:"));
        shadowBlurRadius = new SpinBox<Integer>()
            .setMinValue(0)
            .setMaxValue(20)
            .setValue(3);
        shadowBlurRadius.onChanged(new Callback<ChangedEvent>(){
            public void call(ChangedEvent event) throws Exception {
                if(manager.propMan.isPropertyNotNull("shadow")) {
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
        shadowProperties.add(shadowBlurRadius);
        shadowColorButton = new SwatchColorPicker();
        shadowProperties.add(shadowColorButton);
        shadowProperties.nextRow();
        shadowProperties.add(new Label("opacity"));
        shadowOpacity = new Slider(false).setMin(0).setMax(100).setValue(100);
        EventBus.getSystem().addListener(shadowOpacity, ChangedEvent.DoubleChanged, new Callback<ChangedEvent>(){
            public void call(ChangedEvent event) throws Exception {
                if(manager.propMan.isPropertyNotNull("shadow")) {
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
    }

    private static class IB extends ToolbarButton {
        private SAction action;

        private IB(String s, SAction act) throws IOException {
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

    private static class TIB extends ToolbarButton {
        private SAction action;

        private TIB(String s, SAction act) throws IOException {
            super(Main.class.getResource("resources/"+s));
            this.selectable = true;
            this.action = act;
            onClicked(new Callback<ActionEvent>(){
                public void call(ActionEvent actionEvent) throws Exception {
                    action.execute();
                }
            });
        }

        @Override
        public void draw(GFX g) {
            if(this.isSelected()) {
                g.setPaint(FlatColor.BLACK);
            } else {
                g.setPaint(FlatColor.WHITE);
            }
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

        if(manager.propMan.isClassAvailable(SNode.class)) {
            String id = (String) manager.propMan.getProperty("id").getValue();
            if(id == null){
                nameBox.setText("");
            } else {
                nameBox.setText(id);
            }
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
            if(lastNode != null) {
                if(manager.propMan.isPropertyNotNull("shadow")) {
                    shadowSet.setSelected(true);
                    DropShadow shad = (DropShadow) manager.propMan.getProperty("shadow").getValue();
                    shadowXoff.setValue(shad.getXOffset());
                    shadowYoff.setValue(shad.getYOffset());
                    shadowBlurRadius.setValue(shad.getBlurRadius());
                    shadowColorButton.setSelectedColor(shad.getColor());
                    shadowInner.setSelected(shad.isInner());
                    shadowOpacity.setValue(shad.getOpacity()*100);
                } else {
                    shadowSet.setSelected(false);
                }
            }
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
        g.setPaint(new FlatColor(0.8,0.8,0.8,0.8));
        g.fillRoundRect(0,0,getWidth()-1,h-1,10,10);
        g.setPaint(new FlatColor(0.3,0.3,0.3,0.8));
        g.drawRoundRect(0,0,getWidth()-1,h-1,10,10);
    }
    @Override
    public void doLayout() {
        if(selected) {
            Bounds bounds = selection.calculateBounds();
            bounds = context.getSketchCanvas().transformToDrawing(bounds);
            bounds = NodeUtils.convertToScene(context.getSketchCanvas(),bounds);
            Stage stage = this.getStage();

            double x = bounds.getX();
            double y = bounds.getY() + bounds.getHeight()+20;
            double h = stage.getContent().getVisualBounds().getHeight();
            double w = stage.getContent().getVisualBounds().getWidth();
            double bottom = y + this.getHeight();

            //if to low then put above the item
            if(bottom > h) {
                y = bounds.getY()-this.getHeight()-20;
            }
            //if off the top now, then move to the right
            if (y < 0) {
                y = 10;
                x = bounds.getX2() + 20;
            }

            //if off the right then move to the left
            if(x + this.getWidth() > w) {
                x = bounds.getX()-this.getWidth()-20;
            }

            //if too far off the left then just shove on the left edge and accept it will overlap
            if(x < 0) {
                x = 20;
            }


            setTranslateX(x);
            setTranslateY(y);

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
            if(manager.propMan.isClassAvailable(STransformNode.class)) {
                setVisible(false);
            }
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
                    fillOpacityLabel.setText(getString("toolbar.opacity")+": "+df.format(v/100.0));
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
                strokeWidthLabel.setText(getString("toolbar.stroke")+": " + intFormat.format(sw));
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
