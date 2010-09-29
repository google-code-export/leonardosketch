package org.joshy.gfx.node.control;

import org.joshy.gfx.SkinManager;
import org.joshy.gfx.css.BoxPainter;
import org.joshy.gfx.css.CSSSkin;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;

public class Button extends Control {
    protected String text;
    protected boolean pressed = false;
    protected boolean hovered = false;
    protected boolean selected;
    protected boolean selectable = false;
    protected String style;
    private Callback<ActionEvent> callback;
    protected Font font;
    private BoxPainter boxPainter;

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
                    setSkinDirty();
                    setDrawingDirty();
                }
                if(event.getType() == MouseEvent.MouseReleased) {
                    setPressed(false);
                    setSkinDirty();
                    setDrawingDirty();
                    fireAction();
                }
                if(event.getType() == MouseEvent.MouseEntered) {
                    setHovered(true);
                    setSkinDirty();
                    setDrawingDirty();
                }
                if(event.getType() == MouseEvent.MouseExited) {
                    setHovered(false);
                    setSkinDirty();
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
            State state = calculateState();
            boxPainter = cssSkin.createBoxPainter(this,size,text,buttonStateToCssState(state));
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


        if(cssSkin != null) {
            if(size == null) {
                doPrefLayout();
            }

            boxPainter.draw(g, size, this, text);
            //cssSkin.draw(g, size, this, text, buttonStateToCssState(state));
            //debugging
            if(false) {
                g.setPaint(FlatColor.WHITE);
                g.fillRect(0,0,50,50);
                g.setPaint(FlatColor.BLUE);
                g.fillRoundRect(10,10,20,20,10,10);
                g.setPaint(FlatColor.BLACK);
                g.drawRoundRect(10,10,20,20,10,10);
            }
            return;
        }

    }

    private State calculateState() {
        State state = State.Normal;
        if(hovered && !pressed) state = State.Hovered;
        if(selected && !pressed) state = State.Selected;
        if(!selected && pressed) state = State.Pressed;
        if(selected && pressed) state = State.SelectedPressed;
        return state;        
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

    @Override
    public double getBaseline() {
        if(size == null) {
            doPrefLayout();
        }
        return size.margin.getTop() + size.borderWidth.getTop() + size.padding.getTop() + size.contentBaseline;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        setLayoutDirty();
        setDrawingDirty();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setDrawingDirty();
    }

}
