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
import org.joshy.gfx.node.Insets;
import org.joshy.gfx.util.u;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

public class Button extends Control {
    protected String text;
    protected boolean pressed = false;
    protected boolean hovered = false;
    protected boolean selected;
    protected boolean selectable = false;
    protected String style;
    protected Insets insets;
    protected double baseline = 0;
    private Image normalIcon;
    private Image pressedIcon;
    private Callback<ActionEvent> callback;
    protected Font font;

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

    protected CSSSkin.BoxState size;

    @Override
    public void doSkins() {
        cssSkin = SkinManager.getShared().getCSSSkin();
        insets = cssSkin.getInsets(this);
        font = cssSkin.getDefaultFont();
        setLayoutDirty();
    }

    @Override
    public void doPrefLayout() {
        if(cssSkin != null) {
            size = cssSkin.getSize(this,text);
            if(prefWidth != CALCULATED) {
                setWidth(prefWidth);
                size.width = prefWidth;
            } else {
                setWidth(size.width);
            }
            setHeight(size.height);
        }
    }

    @Override
    public void doLayout() {
        if(size != null) {
            size.width = getWidth();
            size.height = getHeight();
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
            if(size == null) {
                doPrefLayout();
            }
            cssSkin.draw(g, this, text, size, buttonStateToCssState(state));
            return;
        }




        g.setPaint(new FlatColor(0,0,0,1.0));
        g.drawRect(0,0,width,height);
        g.drawLine(0,0,width,height);
        g.drawLine(width,0,0,height);

        double x = insets.getLeft();
        double y = insets.getTop();
        if(pressed) {
            if(pressedIcon != null) {
                g.drawImage(pressedIcon,x,y);
                x+= pressedIcon.getWidth();
            }
        } else {
            if(normalIcon != null) {
                g.drawImage(normalIcon,x,y);
                x+= normalIcon.getWidth();
            }
        }
        g.setPaint(FlatColor.BLACK);
        Font.drawCenteredVertically(g, text, font,x,0,getWidth(),getHeight(),false);

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
        return new Bounds(getTranslateX(), getTranslateY(), getWidth(), getHeight());
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
