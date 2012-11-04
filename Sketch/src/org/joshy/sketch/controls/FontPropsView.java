package org.joshy.sketch.controls;

import java.io.IOException;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.HFlexBox;
import static org.joshy.gfx.util.localization.Localization.getString;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SText;
import org.joshy.sketch.modes.preso.SwitchTheme;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.property.PropertyManager;

/**
 * Created with IntelliJ IDEA.
 * User: josh
 * Date: 11/3/12
 * Time: 4:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class FontPropsView extends HFlexBox {

    private Togglebutton fontAlignLeft;
    private Togglebutton fontAlignHCenter;
    private Togglebutton fontAlignRight;
    private ToggleGroup fontAlignGroup;
    private Checkbox fontWrap;
    private Label fontSizeLabel;
    private Slider fontSizeSlider;
    private Togglebutton fontBoldButton;
    private Togglebutton fontItalicButton;
    private PopupMenuButton<String> fontPicker;
    private VectorDocContext context;
    private Selection selection;
    private Main manager;
    private PopupMenuButton<String> semanticFontPicker;


    private Callback<SelectionEvent> semanticFontChanged = new Callback<SelectionEvent>() {
        public void call(SelectionEvent event) throws Exception {
            if(!manager.propMan.isClassAvailable(SText.class)) return;
            int index = event.getView().getSelectedIndex();
            String style = SwitchTheme.Reveal.semanticStyles[index];
            SwitchTheme.Reveal sw = new SwitchTheme.Reveal(null, context);
            for(SNode node: selection.items()) {
                if(node instanceof SText) {
                    SText text = (SText) node;
                    text.setStringProperty("semanticstyle",style);
                    sw.styleText(text);
                }
            }
            context.redraw();
        }
    };

    private Callback<SelectionEvent> fontPickerChanged =  new Callback<SelectionEvent>(){
        public void call(SelectionEvent event) {
            if(manager.propMan.isClassAvailable(SText.class)) {
                int index = event.getView().getSelectedIndex();
                String fontname = Main.getDatabase().getAllFonts().get(index).getName();
                manager.propMan.getProperty("fontName").setValue(fontname);
                context.redraw();
            }
        }
    };

    public FontPropsView(final Main manager, final VectorDocContext context) throws IOException {
        this.setBoxAlign(Align.Baseline);
        this.context = context;
        this.manager = manager;

        fontSizeLabel = new Label(getString("toolbar.fontSize")+":");
        add(fontSizeLabel);

        fontSizeSlider = new Slider(false);
        fontSizeSlider.setMin(8);
        fontSizeSlider.setMax(500);
        fontSizeSlider.setValue(24);
        EventBus.getSystem().addListener(fontSizeSlider, ChangedEvent.DoubleChanged, fontSizeCallback);
        add(fontSizeSlider);

        fontPicker = new PopupMenuButton<String>();
        fontPicker.setModel(new ListModel<String>() {
            public String get(int i) {
                return Main.getDatabase().getAllFonts().get(i).getName();
            }

            public int size() {
                return Main.getDatabase().getAllFonts().size();
            }
        });
        EventBus.getSystem().addListener(fontPicker, SelectionEvent.Changed, fontPickerChanged);
        add(fontPicker);


        semanticFontPicker = new PopupMenuButton<String>();
        semanticFontPicker.setModel(new ListModel<String>() {
            public String get(int i) {
                return SwitchTheme.Reveal.semanticStyles[i];
            }
            public int size() {
                return SwitchTheme.Reveal.semanticStyles.length;
            }
        });
        EventBus.getSystem().addListener(semanticFontPicker, SelectionEvent.Changed, semanticFontChanged);
        add(semanticFontPicker);

        fontBoldButton = new Togglebutton("B");
        fontBoldButton.onClicked(fontBoldCallback);
        add(fontBoldButton);

        fontItalicButton = new Togglebutton("I");
        fontItalicButton.onClicked(fontItalicCallback);
        add(fontItalicButton);

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

        add(fontAlignLeft, fontAlignHCenter, fontAlignRight);
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

        fontWrap = new Checkbox("wrap");
        fontWrap.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) throws Exception {
                if (manager.propMan.isClassAvailable(SText.class)) {
                    manager.propMan.getProperty("wrapText").setValue(fontWrap.isSelected());
                    context.redraw();
                }
            }
        });
        add(fontWrap);
    }

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

    private Callback<ChangedEvent> fontSizeCallback = new Callback<ChangedEvent>() {
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

    public void update(PropertyManager propMan, Selection selection) {
        this.selection = selection;
        double dval = propMan.getProperty("fontSize").getDoubleValue();
        if(this.selection.size() == 1) {
            fontSizeSlider.setValue(dval);
        }
        PropertyManager.Property wrapText = propMan.getProperty("wrapText");
        if(wrapText.hasSingleValue()) {
            fontWrap.setSelected(wrapText.getBooleanValue());
        }

        if(context.getDocument().isPresentation()) {
            fontSizeLabel.setText("Style");
            semanticFontPicker.setVisible(true);
            fontPicker.setVisible(false);
            fontSizeSlider.setVisible(false);
            fontBoldButton.setVisible(false);
            fontItalicButton.setVisible(false);
            fontAlignRight.setVisible(false);
            fontAlignLeft.setVisible(false);
            fontAlignHCenter.setVisible(false);
        } else {
            semanticFontPicker.setVisible(false);
            fontPicker.setVisible(true);
            fontSizeSlider.setVisible(true);
            fontBoldButton.setVisible(true);
            fontItalicButton.setVisible(true);
        }
    }

    public static class TIB extends ToolbarButton {
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

}
