package org.joshy.sketch.controls;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.sketch.Main;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.property.PropertyManager;

import java.awt.geom.Point2D;
import java.io.IOException;

public class FloatingPropertiesPanel extends HFlexBox {
    private boolean selected = false;
    private Selection selection;
    private SwatchColorPicker colorButton;
    private Slider fillOpacitySlider;
    private SwatchColorPicker strokeColorButton;
    private Slider strokeWidthSlider;
    private Slider fontSizeSlider;
    private Togglebutton fontBoldButton;
    private Togglebutton fontItalicButton;
    private Togglebutton rectPropsButton;
    private Main manager;
    private PopupMenuButton<String> fontPicker;
    private Label fillOpacityLabel;
    private Label strokeWidthLabel;
    private VectorDocContext context;
    private FlatColor gradient1 = new FlatColor(0,0,0,0);
    private static final GradientFill GRADIENT1 =new GradientFill(FlatColor.GRAY, FlatColor.GREEN,0,true, 50,0,50,100);


    public FloatingPropertiesPanel(final Main manager, final VectorDocContext context) throws IOException {
        this.context = context;
        setFill(FlatColor.RED);
        setWidth(200);
        setHeight(200);
        this.manager = manager;
        selected = false;
        EventBus.getSystem().addListener(Selection.SelectionChangeEvent.Changed, new Callback<Selection.SelectionChangeEvent>(){
            public void call(Selection.SelectionChangeEvent event) {
                //only pay attention to events for our own context doc
                if(event.getSelection().getDocument() != context.getDocument()) return;
                if(!manager.propMan.isClassAvailable(SNode.class)) return;
                selected = !event.getSelection().isEmpty();
                selection = event.getSelection();
                updatePanelContents();
                setDrawingDirty();
                setLayoutDirty();
            }
        });


        colorButton = new SwatchColorPicker();
        colorButton.addCustomSwatch(new SwatchColorPicker.CustomSwatch(){
            public void draw(GFX gfx, double x, double y, double w, double h) {
                gfx.setPaint(GRADIENT1.derive(x,y,x,y+h));
                gfx.fillRect(x,y,w,h);
            }

            public FlatColor getColor() {
                return gradient1;
            }
        });
        colorButton.setOutsideColorCallback(new SwatchColorPicker.ColorCallback(){
            public FlatColor call(MouseEvent event) {
                Point2D point = event.getPointInNodeCoords(context.getCanvas());
                SketchDocument doc = context.getDocument();
                for(SNode node : doc.getCurrentPage().model) {
                    if(node.contains(point) && node instanceof SShape) {
                        return (FlatColor) ((SShape)node).getFillPaint();
                    }
                }
                return FlatColor.BLUE;
            }
        });
        strokeColorButton = new SwatchColorPicker();
        EventBus.getSystem().addListener(ChangedEvent.ColorChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                if(event.getSource() == colorButton) {
                    FlatColor color = (FlatColor) event.getValue();
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        if(color == gradient1) {
                            if(manager.propMan.isClassAvailable(SRect.class)) {
                                PropertyManager.Property prop = manager.propMan.getProperty("fillPaint");
                                if(prop.hasSingleValue() && prop.getValue() instanceof GradientFill) {
                                    //do nothing
                                } else {
                                    prop.setValue(GRADIENT1);
                                }
                            }
                            //don't set the color if its a gradient but this isn't a rect
                        } else {
                            manager.propMan.getProperty("fillPaint").setValue(color);
                        }
                        Selection sel = context.getSelection();
                        if(sel.size() == 1){
                            sel.regenHandles(sel.firstItem());
                        }
                    }
                }
                if(event.getSource() == strokeColorButton) {
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        manager.propMan.getProperty("strokePaint").setValue(event.getValue());
                    }
                }
            }
        });
        add(colorButton);
        add(strokeColorButton);

        fillOpacitySlider = new Slider(false);
        fillOpacitySlider.setMin(0);
        fillOpacitySlider.setMax(100);
        fillOpacitySlider.setValue(100);
        EventBus.getSystem().addListener(fillOpacitySlider, ChangedEvent.DoubleChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                if(selection != null) {
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        double v = (Double)event.getValue();
                        manager.propMan.getProperty("fillOpacity").setValue(v/100.0);
                    }
                }
            }
        });
        fillOpacityLabel = new Label("Opacity:");
        add(fillOpacityLabel);
        add(fillOpacitySlider);

        fontSizeSlider = new Slider(false);
        fontSizeSlider.setMin(8);
        fontSizeSlider.setMax(200);
        fontSizeSlider.setValue(24);
        EventBus.getSystem().addListener(fontSizeSlider, ChangedEvent.DoubleChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
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
                }
                
            }
        });
        add(fontSizeSlider);

        final Font[] fonts = new Font[]{Main.HANDDRAWN_FONT,Main.SERIF_FONT,Main.SANSSERIF_FONT};
        fontPicker = new PopupMenuButton<String>();
        fontPicker.setModel(new ListModel<String>() {
            public String get(int i) {
                return fonts[i].getName();
            }

            public int size() {
                return 3;
            }
        });
        EventBus.getSystem().addListener(fontPicker, SelectionEvent.Changed, new Callback<SelectionEvent>(){
            public void call(SelectionEvent event) {
                if(manager.propMan.isClassAvailable(SText.class)) {
                    int index = event.getView().getSelectedIndex();
                    manager.propMan.getProperty("fontName").setValue(fonts[index].getName());
                }
            }
        });
        add(fontPicker);
        fontPicker.setVisible(false);

        strokeWidthSlider = new Slider(false);
        strokeWidthSlider.setMin(0);
        strokeWidthSlider.setMax(20);
        strokeWidthSlider.setValue(3);
        EventBus.getSystem().addListener(strokeWidthSlider, ChangedEvent.DoubleChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                if(manager.propMan.isClassAvailable(SShape.class)) {
                    int sw = (int)((Double)event.getValue()).doubleValue();
                    manager.propMan.getProperty("strokeWidth").setValue(sw);
                }
            }
        });
        strokeWidthLabel = new Label("Stroke:");
        add(strokeWidthLabel);
        add(strokeWidthSlider);

        fontBoldButton = new Togglebutton("B");
        add(fontBoldButton);
        fontBoldButton.onClicked(new Callback<ActionEvent>() {
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
                }

            }
        });

        fontItalicButton = new Togglebutton("I");
        add(fontItalicButton);
        fontItalicButton.onClicked(new Callback<ActionEvent>() {
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
                }

            }
        });

        rectPropsButton = new Togglebutton("^");
        add(rectPropsButton);
        rectPropsButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) {
                context.propsPalette.setVisible(!context.propsPalette.isVisible());
            }
        });


        this.setBoxAlign(Align.Top);
        setVisible(false);
        //setPadding(new Insets(5,5,5,5));
    }

    private void updatePanelContents() {
        SNode lastNode = null;
        for(SNode node : selection.items()) {
            lastNode = node;
        }

        if(manager.propMan.isClassAvailable(SText.class)) {
            fontSizeSlider.setVisible(true);
            double dval = manager.propMan.getProperty("fontSize").getDoubleValue();
            fontSizeSlider.setValue(dval);
            fontBoldButton.setVisible(true);
            //fontBoldButton.setSelected(firstText.getWeight() == Font.Weight.Bold);
            fontItalicButton.setVisible(true);
            //fontItalicButton.setSelected(firstText.getStyle() == Font.Style.Italic);
            fontPicker.setVisible(true);
        } else {
            fontSizeSlider.setVisible(false);
            fontBoldButton.setVisible(false);
            fontItalicButton.setVisible(false);
            fontPicker.setVisible(false);
        }
        if(manager.propMan.isClassAvailable(SShape.class)) {
            colorButton.setVisible(true);
            fillOpacitySlider.setVisible(true);
            fillOpacityLabel.setVisible(true);
            strokeColorButton.setVisible(true);
            strokeWidthSlider.setVisible(true);
            strokeWidthLabel.setVisible(true);
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

                PropertyManager.Property strokeProp = manager.propMan.getProperty("strokeWidth");
                if(strokeProp.hasSingleValue()) {
                    strokeWidthSlider.setValue(strokeProp.getDoubleValue());
                }
                PropertyManager.Property strokeColorProp = manager.propMan.getProperty("strokePaint");
                if(strokeColorProp.hasSingleValue()) {
                    strokeColorButton.setSelectedColor((FlatColor)strokeColorProp.getValue());
                }
                PropertyManager.Property fillOpacityProp = manager.propMan.getProperty("fillOpacity");
                if(fillOpacityProp.hasSingleValue()) {
                    fillOpacitySlider.setValue(fillOpacityProp.getDoubleValue()*100.0);
                }
            }
        } else {
            colorButton.setVisible(false);
            fillOpacitySlider.setVisible(false);
            fillOpacityLabel.setVisible(false);
            strokeColorButton.setVisible(false);
            strokeWidthSlider.setVisible(false);
            strokeWidthLabel.setVisible(false);
        }

        //show the rect props button only if a single rect is selected
        rectPropsButton.setVisible(manager.propMan.isClassAvailable(SRect.class));

        setLayoutDirty();
    }


    @Override
    protected void drawSelf(GFX g) {
        g.setPaint(new FlatColor(0,0,0,0.2));
        g.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
        g.setPaint(new FlatColor(0,0,0,0.5));
        g.drawRoundRect(0,0,getWidth(),getHeight(),10,10);
    }
    @Override
    public void doLayout() {
        setWidth(500);
        setHeight(40);
        if(selected) {
            Bounds bounds = selection.calculateBounds();
            bounds = context.getSketchCanvas().transformToDrawing(bounds);
            setTranslateX(bounds.getX());
            setTranslateY(bounds.getY() + bounds.getHeight()+20);
            super.doLayout();
        }
    }

    /*
    @Override
    public void doLayout() {
        if(selected) {
            Bounds bounds = selection.calculateBounds();
            bounds = context.getSketchCanvas().transformToDrawing(bounds);
            setTranslateX(bounds.getX());
            setTranslateY(bounds.getY() + bounds.getHeight()+20);
            double x = 10;
            double y = 10;
            double maxHeight = -1;
            for(Control c : controlChildren()) {
                if(!c.isVisible()) continue;
                c.doLayout();
                Bounds layout = c.getLayoutBounds();
                c.setTranslateX(x);
                c.setTranslateY(y);
                x+= layout.getWidth();
                x+=10;
                maxHeight = Math.max(maxHeight,layout.getHeight());
            }
            setHeight(maxHeight+10*2);
            setWidth(x);
        }
    }
      */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        setLayoutDirty();
    }

}
