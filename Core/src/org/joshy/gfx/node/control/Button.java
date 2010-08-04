package org.joshy.gfx.node.control;

import org.joshy.gfx.SkinManager;
import org.joshy.gfx.css.CSSSkin;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Skin;
import org.joshy.gfx.node.control.skin.FillSkin;
import org.joshy.gfx.node.control.skin.FontSkin;
import org.joshy.gfx.node.control.skin.ImageSkin;
import org.joshy.gfx.node.control.skin.InsetsSkin;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

public class Button extends Control {
    protected String text;
    protected FontSkin font;
    protected boolean pressed = false;
    protected boolean hovered = false;

    protected static final String BACKGROUND = "background";
    protected static final String TEXT = "text";
    private Skin bg_normal;
    private Skin bg_pressed;
    protected boolean selected;
    protected boolean selectable = false;
    protected String style;
    protected InsetsSkin insets;
    protected Skin textSkin;
    private static final String ICON = "icon";
    private ImageSkin iconSkin;
    protected double baseline = 0;
    private Image normalIcon;
    private Image pressedIcon;
    private static final String PART_ICON = "icon";
    private Callback<ActionEvent> callback;
    private CSSSkin cssSkin;
    private CSSSkin.BoxState size;

    public boolean isSelected() {
        return selected;
    }


    public static enum State {
        Pressed("pressed"),
        Normal("normal"),
        Selected("selected"), SelectedPressed("selected-pressed"), Hovered("hovered");

        private String key;

        State(String s) {
            this.key = s;
        }

        @Override
        public String toString() {
            return this.key;
        }
    };


    public Button() {
        text = "Button text";
        setSkinDirty();
        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>(){
            public void call(MouseEvent event) {
                if(event.getType() == MouseEvent.MousePressed) {
                    setPressed(true);
                    if(selectable) {
                        selected = !selected;
                    }
                    setDrawingDirty();
                }
                if(event.getType() == MouseEvent.MouseReleased) {
                    setPressed(false);
                    setDrawingDirty();
                    fireAction();
                }
                if(event.getType() == MouseEvent.MouseEntered) {
                    setHovered(true);
                    setDrawingDirty();
                }
                if(event.getType() == MouseEvent.MouseExited) {
                    setHovered(false);
                    setDrawingDirty();
                }
            }
        });
    }
    
    public Button onClicked(Callback<ActionEvent> callback) {
        this.callback = callback;
        return this;
    }

    private void fireAction() {
        ActionEvent action = new ActionEvent(ActionEvent.Action, Button.this);
        EventBus.getSystem().publish(action);
        if(callback != null) {
            callback.call(action);
        }
    }

    protected void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    protected void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public Button(String text) {
        this();
        this.text = text;        
    }

    @Override
    public void doSkins() {
        cssSkin = (CSSSkin) SkinManager.getShared().getSkin(this,PART_CSS,PROP_CSS);
        //u.p("button got a css skin: " + cssSkin);
        setLayoutDirty();
        
        font = (FontSkin)SkinManager.getShared().getSkin(this, style, PART_MAIN, "jogltext.font", null, FontSkin.DEFAULT);
        insets = (InsetsSkin) SkinManager.getShared().getSkin(this, style, PART_MAIN, "padding",State.Normal.toString(), InsetsSkin.DEFAULT);
        bg_normal = SkinManager.getShared().getSkin(this,style,PART_MAIN, BACKGROUND, State.Normal.toString());
        bg_pressed = SkinManager.getShared().getSkin(this,style,PART_MAIN, BACKGROUND, State.Pressed.toString());
        textSkin = SkinManager.getShared().getSkin(this,style,PART_MAIN, TEXT, State.Normal.toString());
        iconSkin = (ImageSkin)SkinManager.getShared().getSkin(this,style,PART_ICON, ICON, State.Normal.toString());
    }

    @Override
    public void doLayout() {
        double textWidth = 0;
        double textHeight = 0;
        double iconWidth = 0;
        double iconHeight = 0;

        if(text != null && !text.trim().equals("")) {
            textWidth = font.getFont().getWidth(text);
            textHeight = font.getFont().getAscender() + font.getFont().getDescender();
            baseline = Math.max(font.getFont().getAscender(),0);
        }
        if(iconSkin != null) {
            iconWidth = iconSkin.getWidth();
            iconHeight = iconSkin.getHeight();
            baseline = Math.max(iconHeight, baseline);
        }
        if(normalIcon != null) {
            iconWidth = normalIcon.getWidth();
            iconHeight = normalIcon.getHeight();
            baseline = Math.max(iconHeight,baseline);
        }

        double width = 0;

        //eventually switch to support all orientations and alignments
        if(true) {
            width = insets.getLeft() + iconWidth + textWidth + insets.getRight();
            height = insets.getTop() + Math.max(iconHeight, textHeight) + insets.getBottom();
            baseline += insets.getTop();
        }


        setWidth(width);
        setHeight(height);

        //new css skin stuff
        if(cssSkin != null) {
            size = cssSkin.getSize(this,text);
            setWidth(size.width);
            setHeight(size.height);
            //u.p("did layout. w/h set to: " + this.getWidth() + " " + this.getHeight());
        }
    }
    
    private CSSSkin.State buttonStateToCssState(State state) {
        switch(state) {
            case Selected: return CSSSkin.State.Selected;
            case SelectedPressed: return CSSSkin.State.Selected;
            case Normal: return CSSSkin.State.None;
            case Pressed: return CSSSkin.State.Pressed;
            case Hovered: return CSSSkin.State.Hover;
        }
        return CSSSkin.State.None;
    }
    @Override
    public void draw(GFX g) {
        if(!isVisible()) return;
        g.setPaint(new FlatColor(1,0,0,1));

        State state = State.Normal;
        if(hovered && !pressed) state = State.Hovered;
        if(selected && !pressed) state = State.Selected;
        if(!selected && pressed) state = State.Pressed;
        if(selected && pressed) state = State.SelectedPressed;

        if(cssSkin != null) {
            cssSkin.draw(g,this,text,size,buttonStateToCssState(state));
            return;
        }



        FillSkin skin = (FillSkin) SkinManager.getShared().getSkin(this,style,PART_MAIN, BACKGROUND, state.toString());
        if(skin != null) {
            skin.paint(g,0,0,width,height);
        } else {
            g.setPaint(new FlatColor(0,0,0,1.0));
            g.drawRect(0,0,width,height);
            g.drawLine(0,0,width,height);
            g.drawLine(width,0,0,height);
        }

        double x = insets.getLeft();
        double y = insets.getTop();
        if(pressed) {
            if(pressedIcon != null) {
                g.drawImage(pressedIcon,x,y);
                x+= pressedIcon.getWidth();
            }
            ImageSkin iskin = (ImageSkin) SkinManager.getShared().getSkin(this, style, PART_ICON, ICON, state.toString());
            if(iskin != null) {
                g.drawImage(iskin.getImage(),x,y);
                x+= iskin.getWidth();
            }
        } else {
            if(normalIcon != null) {
                g.drawImage(normalIcon,x,y);
                x+= normalIcon.getWidth();
            } else {
                ImageSkin iskin = (ImageSkin) SkinManager.getShared().getSkin(this, style, PART_ICON, ICON, state.toString());
                if(iskin != null) {
                    g.drawImage(iskin.getImage(),x,y);
                    x+= iskin.getWidth();
                }
            }
        }
        g.setPaint(FlatColor.BLACK);
        Font.drawCenteredVertically(g, text, font.getFont(),x,0,getWidth(),getHeight(),false);

    }

    protected Skin getSkin(String property, String state) {
        if(property == BACKGROUND && state == "normal") {
            return bg_normal;
        }
        if(property == BACKGROUND && state == "pressed") {
            return bg_pressed;
        }
        
        return null;
    }

    @Override
    public Bounds getVisualBounds() {
        return new Bounds(getTranslateX(),getTranslateY(),getWidth(),getHeight());
    }

    @Override
    public Bounds getInputBounds() {
        return getVisualBounds();
    }

    @Override
    public Bounds getLayoutBounds() {
        return new Bounds(getTranslateX(), getTranslateY(), getWidth(), baseline);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        setLayoutDirty();
        setDrawingDirty();
    }

    public void setStyle(String style) {
        this.style = style;
        setDrawingDirty();
    }
    public String getStyle() {
        return style;
    }

    public Button setVariant(String variant) {
        this.style = variant;
        setSkinDirty();
        setLayoutDirty();
        setDrawingDirty();
        return this;
    }

    public void setNormalIcon(URL url) throws IOException {
        this.normalIcon = Image.create(ImageIO.read(url));
    }

    public void setPressedIcon(URL url) throws IOException {
        this.pressedIcon = Image.create(ImageIO.read(url));
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setDrawingDirty();
    }

}
