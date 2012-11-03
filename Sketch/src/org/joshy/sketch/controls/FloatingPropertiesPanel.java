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
import org.joshy.sketch.actions.UndoManager;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.property.PropertyManager;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import static org.joshy.gfx.util.localization.Localization.getString;

public class FloatingPropertiesPanel extends VFlexBox {
    private boolean selected = false;
    private Selection selection;
    private Slider fillOpacitySlider;
    private SwatchColorPicker strokeColorButton;
    private Slider strokeWidthSlider;
    private Main manager;
    private Label fillOpacityLabel;
    private Label strokeWidthLabel;
    private VectorDocContext context;
    private PopupMenuButton<SArrow.HeadEnd> arrowHeadEnd;
    private Label rgbLabel;
    public static final boolean TRUE_PALLETTE = false;//OSUtil.isMac();
    private boolean locked = false;
    private FlexBox groupPropertiesEditor;
    private FlexBox shapeProperties;

    private GridBox shadowProperties;
    private Checkbox shadowSet;
    //private Checkbox shadowInner;
    private SwatchColorPicker shadowColorButton;
    private SpinBox<Double> shadowXoff;
    private SpinBox<Double> shadowYoff;
    private SpinBox<Integer> shadowBlurRadius;
    private Slider shadowOpacity;

    private FontPropsView fontProperties;
    private FlexBox booleanPropsEditor;
    private FillPicker fillButton;
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
        fillButton.setContext(context);
        EventBus.getSystem().addListener(fillButton,ChangedEvent.ObjectChanged, fillButtonCallback);

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





        fontProperties = new FontPropsView(manager,context);
        add(fontProperties);

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
        shadowProperties.setPrefWidth(350);
        shadowProperties.setPrefHeight(100);
        shadowProperties.setPadding(1);
        shadowProperties.createColumn(70,GridBox.Align.Right);
        shadowProperties.createColumn(60,GridBox.Align.Left);
        shadowProperties.createColumn(60,GridBox.Align.Left);
        shadowProperties.createColumn(30,GridBox.Align.Right);
        shadowProperties.createColumn(60,GridBox.Align.Left);
        shadowProperties.createColumn(30,GridBox.Align.Left);
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
        shadowProperties.addControl(shadowSet);
        /*
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
        */
        //shadowProperties.addControl(shadowInner);
        shadowProperties.nextRow();


        shadowProperties.addControl(new Label("Offset:"));
        shadowXoff = new SpinBox<Double>()
            .setValue(5.0);
        shadowProperties.addControl(shadowXoff);
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
        shadowProperties.addControl(shadowYoff);

        shadowProperties.addControl(new Label("Blur:"));
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
        shadowProperties.addControl(shadowBlurRadius);
        shadowColorButton = new SwatchColorPicker();
        shadowProperties.addControl(shadowColorButton);
        shadowProperties.nextRow();
        shadowProperties.addControl(new Label("opacity"));
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
        shadowProperties.addControl(shadowOpacity);
    }

    public void hidePopups() {
        fillButton.hidePopups();
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
            fontProperties.update(manager.propMan,selection);
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

            //shadow properties
            shadowProperties.setVisible(true);
            if(lastNode != null) {
                if(manager.propMan.isPropertyNotNull("shadow")) {
                    shadowSet.setSelected(true);
                    DropShadow shad = (DropShadow) manager.propMan.getProperty("shadow").getValue();
                    shadowXoff.setValue(shad.getXOffset());
                    shadowYoff.setValue(shad.getYOffset());
                    shadowBlurRadius.setValue(shad.getBlurRadius());
                    shadowColorButton.setSelectedColor(shad.getColor());
                    //shadowInner.setSelected(shad.isInner());
                    shadowOpacity.setValue(shad.getOpacity()*100);
                } else {
                    shadowSet.setSelected(false);
                }
            }
        } else {
            shapeProperties.setVisible(false);
            shadowProperties.setVisible(false);
        }
        //fill property
        if(manager.propMan.isClassAvailable(SShape.class)) {
            if(manager.propMan.getProperty("fillPaint").hasSingleValue()) {
                Paint fill = (Paint) manager.propMan.getProperty("fillPaint").getValue();
                fillButton.setSelectedFill(fill);
            }
        }

        //stroke properties
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
            if(bottom > h-140) {
                //the 130 above is to account for the height of the fill picker
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

    Callback<ChangedEvent> fillButtonCallback = new Callback<ChangedEvent>() {
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
            for(SNode node : doc.getCurrentPage().getNodes()) {
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
                    setPropertyWithUndo("strokePaint", event.getValue());
                    context.redraw();
                }
            }
            if(event.getSource() == shadowColorButton) {
                if(manager.propMan.isClassAvailable(SShape.class)) {
                    DropShadow shadow = (DropShadow) manager.propMan.getProperty("shadow").getValue();
                    if(shadow != null) {
                        shadow = shadow.setColor(shadowColorButton.getSelectedColor());
                        manager.propMan.getProperty("shadow").setValue(shadow);
                        context.redraw();
                    }
                }
            }
        }
    };

    private void setFillStuff(Paint paint) {
        if(paint instanceof RadialGradientFill) {
            RadialGradientFill grad = (RadialGradientFill) paint.duplicate();
            SShape shape = (SShape) context.getSelection().firstItem();
            Bounds bounds = shape.getTransformedBounds();
            grad.setCenterX(bounds.getWidth()/2);
            grad.setCenterY(bounds.getWidth()/2);
            grad.setRadius(Math.min(bounds.getWidth()/2,bounds.getHeight()/2));
            setPropertyWithUndo("fillPaint",grad);
            return;
        }
        if(paint instanceof LinearGradientFill) {
            LinearGradientFill grad = (LinearGradientFill) paint.duplicate();
            SShape shape = (SShape) context.getSelection().firstItem();
            Bounds bounds = shape.getTransformedBounds();
            switch(grad.getStartXSnapped()) {
                case Start:  grad.setStartX(0); break;
                case Middle: grad.setStartX(bounds.getWidth()/2); break;
                case End:    grad.setStartX(bounds.getWidth()); break;
            }
            switch(grad.getEndXSnapped()) {
                case Start:  grad.setEndX(0); break;
                case Middle: grad.setEndX(bounds.getWidth()/2); break;
                case End:    grad.setEndX(bounds.getWidth()); break;
            }
            switch(grad.getStartYSnapped()) {
                case Start:  grad.setStartY(0); break;
                case Middle: grad.setStartY(bounds.getHeight()/2); break;
                case End:    grad.setStartY(bounds.getHeight()); break;
            }
            switch(grad.getEndYSnapped()) {
                case Start:  grad.setEndY(0); break;
                case Middle: grad.setEndY(bounds.getHeight()/2); break;
                case End:    grad.setEndY(bounds.getHeight()); break;
            }
            setPropertyWithUndo("fillPaint",grad);
            return;
        }
        //if just a normal color
        paint = paint.duplicate();
        setPropertyWithUndo("fillPaint", paint);
    }

    private Callback<ChangedEvent> fillOpacityCallback = new Callback<ChangedEvent>() {
        public void call(ChangedEvent event) throws Exception {
            if(selection != null) {
                if(manager.propMan.isClassAvailable(SShape.class)) {
                    double v = (Double)event.getValue();
                    setPropertyWithUndo("fillOpacity", v / 100.0);
                    manager.propMan.getProperty("fillOpacity").setValue(v/100.0);
                    fillOpacityLabel.setText(getString("toolbar.opacity")+": "+df.format(v/100.0));
                    context.redraw();
                }
            }
        }
    };


    private Callback<? extends Event> strokeWidthCallback = new Callback<ChangedEvent>() {
        public void call(ChangedEvent event) {
            if(manager.propMan.isClassAvailable(SShape.class) ||
                    manager.propMan.isClassAvailable(SImage.class)) {
                int sw = (int)((Double)event.getValue()).doubleValue();
                setPropertyWithUndo("strokeWidth", sw);
                strokeWidthLabel.setText(getString("toolbar.stroke")+": " + intFormat.format(sw));
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

    private void setPropertyWithUndo(final String propName, final Object newValue) {
        //u.p("maybe appending undo action: " + propName + " " + newValue);
        final PropertyManager.Property prop = manager.propMan.getProperty(propName);
        if(prop.hasSingleValue()) {
            //u.p("prop = " + prop.getValue());
            //u.p("new value = " + newValue);

            Object curValue = prop.getValue();
            if(curValue != null && newValue != null) {
                //u.p("setting a new value that is the same as the old value");
                if(curValue instanceof Number && newValue instanceof Number) {
                    Number n1 = (Number) curValue;
                    Number n2 = (Number) newValue;
                    if(Math.abs(n1.doubleValue() - n2.doubleValue()) < 0.01) {
                        //u.p("close enough. not adding to the undo");
                        return;
                    }
                }
            }

            //undo single value
            UndoManager.UndoableAction previousUndo = context.getUndoManager().getLastAction();
            if(previousUndo != null &&
                    previousUndo instanceof SingleValuePropertyUndo &&
                    ((SingleValuePropertyUndo)previousUndo).propName.equals(propName)) {
                //u.p("we can coallate");
                SingleValuePropertyUndo svpu = (SingleValuePropertyUndo) previousUndo;
                svpu.newValue = newValue;
            } else {
                final Object oldValue = prop.getValue();
                SingleValuePropertyUndo undo = new SingleValuePropertyUndo(prop, oldValue, propName, newValue);
                context.getUndoManager().pushAction(undo);
            }

        } else {
            //undo differing values
            //u.p("appending undo for differing values");
            final Map<SNode,Object> oldValues = prop.getValues();
            context.getUndoManager().pushAction(new UndoManager.UndoableAction() {
                public void executeUndo() {
                    for(SNode node : oldValues.keySet()) {
                        //u.p("setting " + propName + " value to : " + oldValues.get(node));
                        prop.setValue(node, oldValues.get(node));
                    }
                    context.redraw();
                }

                public void executeRedo() {
                    context.redraw();
                }

                public String getName() {
                    return "changed " + propName;
                }
            });
        }
        prop.setValue(newValue);
    }

    private class SingleValuePropertyUndo implements UndoManager.UndoableAction {
        private final PropertyManager.Property prop;
        private final Object oldValue;
        private final String propName;
        private Object newValue;

        public SingleValuePropertyUndo(PropertyManager.Property prop, Object oldValue, String propName, Object newValue) {
            this.prop = prop;
            this.oldValue = oldValue;
            this.propName = propName;
            this.newValue = newValue;
        }

        public void executeUndo() {
            prop.setValue(oldValue);
            //u.p("undoing a single value set of : " + propName + " to " + oldValue);
            context.redraw();
        }

        public void executeRedo() {
            //u.p("redoing a single value set of " + propName + " to " + newValue);
            prop.setValue(newValue);
            context.redraw();
        }

        public String getName() {
            return "changed " + propName;
        }
    }
}
