package org.joshy.gfx.node.layout;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.Control;

import java.util.ArrayList;
import java.util.List;

public class SplitPane extends AbstractPane {
    private double position;
    private double dividerWidth = 10;
    private boolean vertical;
    private Control second;
    private Control first;

    public SplitPane(boolean vert) {
        this.vertical = vert;
        position = 100.0;
        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public void call(MouseEvent event) {
                if(event.getType() == MouseEvent.MouseDragged) {
                    if(vertical) {
                        position = event.getY();
                    } else {
                        position = event.getX();
                    }
                    setLayoutDirty();
                }
            }
        });
    }

    public SplitPane setFirst(Control panel) {
        if(first != null) {
            first.setParent(null);
        }
        first = panel;
        first.setParent(this);
        setSkinDirty();
        setLayoutDirty();
        setDrawingDirty();
        return this;
    }

    public SplitPane setSecond(Control panel) {
        if(second != null) {
            second.setParent(null);
        }
        second = panel;
        second.setParent(this);
        setSkinDirty();
        setLayoutDirty();
        setDrawingDirty();
        return this;
    }

    @Override
    public Control setWidth(double width) {
        super.setWidth(width);
        if(!vertical && position > width) {
            position = width;
        }
        return this;
    }

    @Override
    public Control setHeight(double height) {
        super.setHeight(height);
        if(vertical && position > height) {
            position = height;
        }
        return this;
    }

    @Override
    public void draw(GFX g) {
        g.setPaint(FlatColor.BLACK);
        if(vertical) {
            g.fillRect(0,position-dividerWidth/2,getWidth(),dividerWidth);
        } else {
            g.fillRect(position-dividerWidth/2,0,dividerWidth,getHeight());
        }
        for(Node child : children()) {
            g.translate(child.getTranslateX(),child.getTranslateY());
            child.draw(g);
            g.translate(-child.getTranslateX(),-child.getTranslateY());
        }
        this.drawingDirty = false;
    }

    @Override
    public void doLayout() {
        if(first != null) {
            first.setTranslateX(0);
            first.setTranslateY(0);
            if(vertical) {
                first.setHeight(position-dividerWidth/2);
                first.setWidth(getWidth());
            } else {
                first.setWidth(position-dividerWidth/2);
                first.setHeight(getHeight());
            }
        }
        if(second != null) {
            if(vertical) {
                second.setTranslateY(position+dividerWidth/2);
                second.setTranslateX(0);
                second.setHeight(getHeight()-position-dividerWidth/2);
                second.setWidth(getWidth());
            } else {
                second.setTranslateX(position+dividerWidth/2);
                second.setTranslateY(0);
                second.setWidth(getWidth()-position-dividerWidth/2);
                second.setHeight(getHeight());
            }
        }
        
        // the main layout pass
        for(Node n : children()) {
            if(n instanceof Control) {
                ((Control)n).doPrefLayout();
                ((Control)n).doLayout();
            }
        }
    }

    public SplitPane setPosition(double position) {
        this.position = position;
        setLayoutDirty();
        return this;
    }

    public Iterable<? extends Node> children() {
        List<Node> list = new ArrayList<Node>();
        if(first != null) {
            list.add(first);
        }
        if(second != null) {
            list.add(second);
        }
        return list;
    }

    public Iterable<? extends Node> reverseChildren() {
        List<Node> list = new ArrayList<Node>();
        if(second != null) {
            list.add(second);
        }
        if(first != null) {
            list.add(first);
        }
        return list;
    }


}
