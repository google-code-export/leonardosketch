package org.joshy.gfx.node.control;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.u;

import java.awt.geom.Point2D;

public class Slider extends Control {
    private boolean vertical = false;
    private double value = 50;
    private double min = 0;
    private double max = 100;
    private boolean thumbPressed = false;
    double thumbWidth = 9;
    private double offPX;
    private Point2D.Double startPX;
    private double smallScroll = 10;
    private double largeScroll = 20;
    private FlatColor thumbFill;
    private Paint trackFill;

    public Slider(boolean vertical) {
        thumbFill = FlatColor.BLACK;
        trackFill = FlatColor.GRAY;
        setWidth(100);
        setHeight(20);
        this.vertical = vertical;
        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public void call(MouseEvent event) {
                processInput(event);
            }
        });
    }

    private void processInput(MouseEvent event) {
        double ex = event.getX();
        double ey = event.getY();
        Bounds thumbBounds = calculateThumbBounds();
        if (event.getType() == MouseEvent.MousePressed) {
            if (thumbBounds.contains(ex, ey)) {
                thumbPressed = true;
                if (isVertical()) {
                    offPX = event.getY() - calculateThumbBounds().getY();
                } else {
                    offPX = event.getX() - calculateThumbBounds().getX();
                }
                startPX = new Point2D.Double(ex, ey);
            } else {
                if (ex < thumbBounds.getX()) {
                    setValue(getValue() - largeScroll);
                }
                if (ex > thumbBounds.getX() + thumbBounds.getWidth()) {
                    setValue(getValue() + largeScroll);
                }
                if (ey < thumbBounds.getY()) {
                    setValue(getValue() - largeScroll);
                }
                if (ey > thumbBounds.getY() + thumbBounds.getHeight()) {
                    setValue(getValue() + largeScroll);
                }
            }
        }
        if (event.getType() == MouseEvent.MouseDragged) {
            if (thumbPressed) {
                Point2D currentPX = new Point2D.Double(event.getX(), event.getY());
                double value = pxToValue(currentPX, offPX);
                setValue(value);
            }
        }
        if (event.getType() == MouseEvent.MouseReleased) {
            thumbPressed = false;
        }

    }

    private double pxToValue(Point2D point, double offPX) {
        double px = point.getX() - offPX;
        if (isVertical()) {
            px = point.getY() - offPX;
        }
        double length = 0;
        if (vertical) {
            length = getHeight();
        } else {
            length = getWidth();
        }
        length -= thumbWidth;
        double fraction = px / ((length) * (1.0));
        return getMin() + (getMax() - getMin()) * fraction;
    }

    private Bounds calculateThumbBounds() {
        double fract = (getValue()-getMin()) / (getMax() - getMin());
        double tx = (getWidth()-thumbWidth)*fract;
        return new Bounds(
                tx,
                0,
                thumbWidth,
                getHeight()
        );
    }

    public void draw(GFX g) {
        if(!isVisible()) return;
        //draw the background
        int rot = 0;
        if (isVertical()) rot = 1;

        //track
        g.setPaint(trackFill);
        double inset = 6;
        g.fillRoundRect(0, inset, getWidth(), getHeight() - inset*2, 7,7);

        //thumb
        g.setPaint(thumbFill);
        Bounds thumb = calculateThumbBounds();
        g.fillRoundRect(thumb.getX(), thumb.getY(), thumb.getWidth(), thumb.getHeight(),7,7);
    }

    @Override
    public void doLayout() {
    }

    @Override
    public void doSkins() {
    }

    public boolean isVertical() {
        return vertical;
    }

    public double getValue() {
        return value;
    }

    public Slider setValue(double value) {
        if (value < getMin()) {
            value = getMin();
        }
        if (value > getMax()) {
            value = getMax();
        }
        this.value = value;
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.DoubleChanged,this.value,this));
        setDrawingDirty();
        return this;
    }

    public double getMin() {
        return min;
    }

    public Slider setMin(double min) {
        this.min = min;
        setDrawingDirty();
        return this;
    }

    public double getMax() {
        return max;
    }

    public Slider setMax(double max) {
        this.max = max;
        setDrawingDirty();
        return this;
    }

    public void setThumbFill(FlatColor thumbFill) {
        this.thumbFill = thumbFill;
    }

    public void setTrackFill(Paint trackFill) {
        this.trackFill = trackFill;
    }
}
