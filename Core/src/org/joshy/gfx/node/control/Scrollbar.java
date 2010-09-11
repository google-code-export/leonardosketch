package org.joshy.gfx.node.control;

import org.joshy.gfx.SkinManager;
import org.joshy.gfx.css.CSSSkin;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.GraphicsUtil;

import java.awt.geom.Point2D;


/**
 * A scrollbar that can be either vertical or horizontal. It can optionally draw the thumb
 * with a proportional size.  Styling can be done with CSS.
 *
 */
public class Scrollbar extends Control {
    private double min = 0;
    private double max = 100;
    private double value = 0;
    private double smallScroll = 10;
    private double largeScroll = 20;

    protected boolean vertical = false;
    double arrowLength = 20;
    double thumbLength = 20;
    private boolean thumbPressed = false;
    private boolean isProportional = false;
    private double span = 0;
    private Point2D.Double startPX;
    private double offPX;
    private CSSSkin.BoxState size;

    public Scrollbar() {
        this(false);
    }

    public Scrollbar(boolean vertical) {
        this.vertical = vertical;
        if(vertical) {
            setHeight(100);
            setWidth(20);
        } else {
            setHeight(20);
            setWidth(100);
        }
        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>(){
            public void call(MouseEvent event) {
                processInput(event);
            }
        });
    }

    private void processInput(MouseEvent event) {
        double ex = event.getX();
        double ey = event.getY();

        if(event.getType() == MouseEvent.MousePressed) {
            Bounds startArrowBounds = getStartArrowBounds();
            if(startArrowBounds.contains(event.getX(),event.getY())) {
                setValue(getValue()-smallScroll);
                return;
            }
            Bounds endArrowBounds = getEndArrowBounds();
            if(endArrowBounds.contains(event.getX(),event.getY())) {
                setValue(getValue()+smallScroll);
                return;
            }

            Bounds trackBounds = getTrackBounds();
            Bounds thumbBounds = calculateThumbBounds();
            if(thumbBounds.contains(ex,ey)) {
                thumbPressed = true;
                if(isVertical()) {
                    offPX = event.getY() - calculateThumbBounds().getY();
                } else {
                    offPX = event.getX() - calculateThumbBounds().getX();
                }
                startPX = new Point2D.Double(ex,ey);
            } else {
                if(trackBounds.contains(ex,ey)) {
                    if(ex < thumbBounds.getX()) {
                        setValue(getValue()-largeScroll);
                    }
                    if(ex > thumbBounds.getX()+thumbBounds.getWidth()) {
                        setValue(getValue()+largeScroll);
                    }
                    if(ey < thumbBounds.getY()) {
                        setValue(getValue()-largeScroll);
                    }
                    if(ey > thumbBounds.getY()+thumbBounds.getHeight()) {
                        setValue(getValue()+largeScroll);
                    }
                }
            }
        }

        if(event.getType() == MouseEvent.MouseDragged) {
            if(thumbPressed) {
                Point2D currentPX = new Point2D.Double(event.getX(),event.getY());
                double value = pxToValue(currentPX,offPX);
                setValue(value);
            }
        }
        if(event.getType() == MouseEvent.MouseReleased) {
            thumbPressed = false;
        }
        
    }

    private double pxToValue(Point2D point, double offPX) {
        double px = point.getX() - offPX;
        if(isVertical()) {
            px = point.getY() - offPX;
        }
        px = px - arrowLength;
        double length = 0;
        if(vertical) {
            length = getHeight();
        } else {
            length = getWidth();
        }
        double fraction = px / ((length-arrowLength*2)*(1.0-span));
        if(span >= 1.0) {
            fraction = 0;
        }
        return getMin() + (getMax()-getMin())*fraction;
    }

    private Bounds getTrackBounds() {
        if(vertical) {
            return new Bounds(0,arrowLength,getWidth(),getHeight()-arrowLength*2);
        } else {
            return new Bounds(arrowLength,0,getWidth()-arrowLength*2,getHeight());
        }
    }

    private Bounds getEndArrowBounds() {
        if(vertical) {
            return new Bounds(0, getHeight()-arrowLength, getWidth(), arrowLength);
        } else {
            return new Bounds(getWidth()-arrowLength, 0, arrowLength, height);
        }
    }

    private Bounds getStartArrowBounds() {
        if(vertical) {
            return new Bounds(0,0, getWidth(), arrowLength);
        } else {
            return new Bounds(0,0, arrowLength, height);
        }
    }

    @Override
    public void doSkins() {
        cssSkin = SkinManager.getShared().getCSSSkin();
        setLayoutDirty();
    }

    @Override
    public void doLayout() {
        if(cssSkin != null) {
            size = cssSkin.getSize(this);
            setWidth(size.width);
            setHeight(size.height);
        }
    }
    
    @Override
    public void draw(GFX g) {
        if(!isVisible()) return;
        //draw the background

        Bounds thumbBounds = calculateThumbBounds();
        Bounds leftArrowBounds = new Bounds(0,0,arrowLength,getHeight());
        Bounds rightArrowBounds = new Bounds(getWidth()-arrowLength,0,arrowLength,getHeight());
        if(isVertical()) {
            leftArrowBounds = new Bounds(0,0,getWidth(),arrowLength);
            rightArrowBounds = new Bounds(0, getHeight()-arrowLength,getWidth(),arrowLength);           
        }

        if(cssSkin != null) {
            cssSkin.draw(g,this,size,thumbBounds, leftArrowBounds, rightArrowBounds);
            return;
        }

        g.setPaint(FlatColor.GRAY);
        g.fillRoundRect(0,0,getWidth(),getHeight(),10,10);

        //draw the arrows
        g.setPaint(FlatColor.BLACK);
        if(vertical) {
            GraphicsUtil.fillUpArrow(g,3,3,14);
            GraphicsUtil.fillDownArrow(g,3,getHeight()-3-14,14);
        } else {
            GraphicsUtil.fillLeftArrow(g,2,3,14);
            GraphicsUtil.fillRightArrow(g,getWidth()-2-14,3,14);
        }

        //draw the thumb
        g.setPaint(FlatColor.BLACK);
        double arc = thumbBounds.getHeight();
        if(isVertical()) {
            arc = thumbBounds.getWidth();
        }
        g.fillRoundRect(thumbBounds.getX(),thumbBounds.getY(),thumbBounds.getWidth(),thumbBounds.getHeight(),
                arc,arc);
    }

    Bounds calculateThumbBounds() {
        double diff = getMax()-getMin();
        double valueFraction = 0;
        if(diff > 0) {
            valueFraction = getValue() / diff;
        }
        double tl = thumbLength;
        if(isProportional()) {
            if(isVertical()){
                tl = span * (getHeight() - arrowLength - arrowLength);
            } else {
                tl = span * (getWidth()  - arrowLength - arrowLength);
            }
        }
        if(tl < thumbLength) {
            tl = thumbLength;
        }
        if(vertical) {
            double thumbY = (getHeight() -arrowLength-arrowLength-tl)*valueFraction + arrowLength;
            return new Bounds(0,thumbY,getWidth(), tl);
        } else {
            double thumbX = (getWidth() -arrowLength-arrowLength-tl)*valueFraction + arrowLength;
            return new Bounds(thumbX,0,tl,getHeight());
        }
    }

    @Override
    public Bounds getVisualBounds() {
        return new Bounds(getTranslateX(),getTranslateY(), getWidth(), getHeight());
    }

    @Override
    public Bounds getInputBounds() {
        return getVisualBounds();
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
        setDrawingDirty();
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
        setValue(getValue());
        setDrawingDirty();
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        if(value < getMin()) value = getMin();
        if(value > getMax()) value = getMax();
        if(this.value != value) {
            this.value = value;
            EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.DoubleChanged, this.value, this));
            setDrawingDirty();
        }
    }

    public void setSpan(double value) {
        if(value > 1) {
            value = 1;
        }
        this.span = value;
        setDrawingDirty();
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
        setDrawingDirty();
    }


    public boolean isProportional() {
        return isProportional;
    }

    public void setProportional(boolean proportional) {
        isProportional = proportional;
    }

    public double getSpan() {
        return span;
    }
}
