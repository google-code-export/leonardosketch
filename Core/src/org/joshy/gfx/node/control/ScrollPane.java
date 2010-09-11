package org.joshy.gfx.node.control;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.layout.AbstractPane;
import org.joshy.gfx.node.layout.Container;
import org.joshy.gfx.node.layout.Panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Feb 1, 2010
 * Time: 10:58:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class ScrollPane extends AbstractPane {
    private Scrollbar vscroll;
    private Scrollbar hscroll;
    private Node content;
    private Container contentWrapper;
    private double vscrollValue;
    private double hscrollValue;
    private boolean horizontalScrollVisible = true;

    public ScrollPane() {
        vscroll = new Scrollbar(true);
        vscroll.setProportional(true);
        vscroll.setParent(this);
        hscroll = new Scrollbar(false);
        hscroll.setProportional(true);
        hscroll.setParent(this);
        contentWrapper = new Container() {

            @Override
            public void draw(GFX g) {
                g.setPaint(FlatColor.BLACK);
                g.fillRect(0,0,getWidth(),getHeight());
                for(Node child : children) {
                    g.translate(child.getTranslateX(),child.getTranslateY());
                    child.draw(g);
                    g.translate(-child.getTranslateX(),-child.getTranslateY());
                }
                this.drawingDirty = false;
            }
        };
        contentWrapper.setParent(this);
        EventBus.getSystem().addListener(vscroll,ChangedEvent.DoubleChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                setVScrollValue(-(Double)event.getValue(), false);
            }
        });
        EventBus.getSystem().addListener(hscroll,ChangedEvent.DoubleChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                hscrollValue = -(Double)event.getValue();
                if(content instanceof ScrollingAware) {
                    ((ScrollingAware)content).setScrollX(hscrollValue);
                } else {
                    contentWrapper.setTranslateX(hscrollValue);
                }
                setDrawingDirty();
            }
        });
        setContent(new Panel());
    }

    public ScrollPane(Control content) {
        this();
        setContent(content);
    }

    public Scrollbar getVerticalScrollBar() {
        return vscroll;
    }

    private void setVScrollValue(double vsv, boolean updateScrollbar) {
        vscrollValue = vsv;
        if(content instanceof ScrollingAware) {
            ((ScrollingAware)content).setScrollY(vscrollValue);
        } else {
            contentWrapper.setTranslateY(vscrollValue);
        }
        if(updateScrollbar) {
            vscroll.setValue(-vscrollValue);
        }
        setDrawingDirty();
    }

    private void setHScrollValue(double hsv, boolean updateScrollbar) {
        hscrollValue = hsv;
        if(content instanceof ScrollingAware) {
            ((ScrollingAware)content).setScrollX(hscrollValue);
        } else {
            contentWrapper.setTranslateX(hscrollValue);
        }
        if(updateScrollbar) {
            hscroll.setValue(-hscrollValue);
        }
        setDrawingDirty();
    }

    @Override
    public void doLayout() {
        // the main layout pass
        for(Node n : children()) {
            if(n instanceof Control) {
                ((Control)n).doLayout();
            }
        }
        
        vscroll.setTranslateX(getWidth()-vscroll.getWidth());
        if(horizontalScrollVisible) {
            vscroll.setHeight(getHeight()-hscroll.getHeight());
        } else {
            vscroll.setHeight(getHeight());
        }
        hscroll.setTranslateY(getHeight()-hscroll.getHeight());
        hscroll.setWidth(getWidth()-vscroll.getWidth());


        Bounds cBounds = content.getVisualBounds();
        if(content instanceof ScrollingAware) {
            ScrollingAware sa = (ScrollingAware) content;
            cBounds = new Bounds(0,0,sa.getFullWidth(getWidth(),getHeight()),sa.getFullHeight(getWidth(),getHeight()));
            if(sa instanceof Control) {
                Control control = (Control) sa;
                control.setWidth(getWidth()-vscroll.getWidth());
                if(hscroll.isVisible()) {
                    control.setHeight(getHeight()-hscroll.getHeight());
                } else {
                    control.setHeight(getHeight());
                }
            }
        }
        hscroll.setMin(0);
        double hmax =  cBounds.getWidth()-getWidth()+vscroll.getWidth();
        if(hmax < 0) {
            hscroll.setMax(0);
            hscroll.setSpan(1);
        } else {
            hscroll.setMax(hmax);
            hscroll.setSpan(getWidth()/cBounds.getWidth());
        }

        double vmax = cBounds.getHeight()-getHeight()+hscroll.getHeight();
        vscroll.setMin(0);
        if(vmax < 0) {
            vscroll.setMax(0);
            vscroll.setSpan(1);
        } else {
            vscroll.setMax(vmax);
            vscroll.setSpan(getHeight()/cBounds.getHeight());
        }
        
    }

    public void setContent(Node node) {
        content = node;
        contentWrapper.add(content);
        if(node instanceof ScrollingAware) {
            ((ScrollingAware)node).setScrollParent(this);
        }
    }

    public void setHorizontalScrollVisible(boolean visible) {
        this.horizontalScrollVisible = visible;
        hscroll.setVisible(visible);
    }

    public Iterable<? extends Node> children() {
        List<Node> childs = new ArrayList<Node>();
        childs.add(contentWrapper);
        childs.add(hscroll);
        childs.add(vscroll);
        return childs;
    }

    public Iterable<? extends Node> reverseChildren() {
        List<Node> childs = (List<Node>) children();
        Collections.reverse(childs);
        return childs;
    }

    @Override
    public void draw(GFX g) {
        Bounds oldClip = g.getClipRect();
        g.setClipRect(new Bounds(0,0,getWidth(),getHeight()));
        g.setPaint(FlatColor.WHITE);
        g.fillRect(0,0,getWidth(),getHeight());
        for(Node child : children()) {
            g.translate(child.getTranslateX(),child.getTranslateY());
            child.draw(g);
            g.translate(-child.getTranslateX(),-child.getTranslateY());
        }
        g.setClipRect(oldClip);
        this.drawingDirty = false;
    }

    public void scrollToShow(Bounds bounds) {
        double w = getWidth();
        double h = getHeight();

        //vertical adjustments
        //calc if we need to scroll down to reveal more at the bottom
        // (subtract off the value of the horiz scrollbar width
        double diff = bounds.getY()+bounds.getHeight() - (h-20-vscrollValue);
        //calc if we need to scroll up to reveal more on top
        double diff2 = h + bounds.getY() - (getHeight()-vscrollValue);
        if(diff > 0) {
            setVScrollValue(vscrollValue -diff,true);
        }
        if(diff2 <= 0) {
            setVScrollValue(vscrollValue - diff2,true);
        }
        //horizontal adjustments
        //calc if we need to scroll to reveal more on the right
        //(subtract off the width of the vert scrollbar)
        diff = bounds.getX()+bounds.getWidth()-(w-20-hscrollValue);
        //calc if we need to scroll to reveal more on the left
        diff2 = w + bounds.getX() - (getWidth()-hscrollValue);
        if(diff > 0) {
            setHScrollValue(hscrollValue - diff,true);
        }
        if(diff2 <= 0) {
            setHScrollValue(hscrollValue - diff2, true);
        }
    }

    public Scrollbar getHorizontalScrollBar() {
        return hscroll;
    }

    public interface ScrollingAware {
        public double getFullWidth(double width, double height);
        public double getFullHeight(double width, double height);
        public void setScrollX(double value);
        public void setScrollY(double value);
        public void setScrollParent(ScrollPane scrollPane);
    }
}
