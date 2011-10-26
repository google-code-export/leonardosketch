package org.joshy.sketch.controls;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.control.Control;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: 4/26/11
* Time: 7:45 PM
* To change this template use File | Settings | File Templates.
*/
class FreerangeColorPickerPopup extends Control {
    private Image image;
    private BufferedImage img;
    private FlatColor selectedColor = FlatColor.GREEN;
    private FreerangeColorPicker delegate;
    private boolean hideOnSelect;

    public FreerangeColorPickerPopup(FreerangeColorPicker delegate, int width, int height, boolean hideOnSelect) {
        this.hideOnSelect = hideOnSelect;
        this.delegate = delegate;
        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public void call(MouseEvent event) {
                processMouse(event);
            }
        });

        img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        for(int x=0; x<width; x++) {
            for(int y=0; y<height; y++) {
                double hue = (double)x/(double)width*360.0;
                double sat = 1.0;
                double bri = 1.0;
                if (y < height/2) {
                    sat = (double)y/ (height/2.0);
                } else {
                    bri = 1.0-(double)(y-height/2)/(height/2.0);
                }
                FlatColor c =  FlatColor.hsb(hue,sat,bri);
                g.setPaint(new Color(c.getRGBA()));
                g.fillRect(x,y,1,1);
            }
        }
        g.dispose();
        image = Image.create(img);
    }

    private void processMouse(MouseEvent event) {
        if (event.getType() == MouseEvent.MouseDragged || event.getType() == MouseEvent.MousePressed) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if(x < 0 || x > img.getWidth()-1 || y <0 || y > img.getHeight()-1 ) {
                setSelectedColor(FlatColor.RED);
                setDrawingDirty();return;
            }
            setSelectedColor(new FlatColor(img.getRGB(x, y)));
            setDrawingDirty();
        }
        if (event.getType() == MouseEvent.MouseReleased) {
            setDrawingDirty();
            if(hideOnSelect) {
                setVisible(false);
            }
            setFinalColor(getSelectedColor());
        }

    }

    @Override
    public void doPrefLayout() {
        //noop
    }

    @Override
    public void doLayout() {
        setWidth(img.getWidth());
        setHeight(img.getHeight());
    }

    @Override
    public void doSkins() {
    }

    @Override
    public void draw(GFX g) {
        if (!isVisible()) return;
        g.setPaint(FlatColor.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(image,0,0);
        Point2D pt = colorToPoint(getSelectedColor());
        g.setPaint(FlatColor.BLACK);
        g.drawOval(pt.getX()-5,pt.getY()-5,10,10);
        g.setPaint(FlatColor.WHITE);
        g.drawOval(pt.getX()-4,pt.getY()-4,8,8);
    }

    public void setSelectedColor(FlatColor color) {
        this.selectedColor = color;
        if(delegate != null) {
            delegate.setSelectedColor(color);
        }
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.ColorChanged,selectedColor,this,true));
        setDrawingDirty();
    }

    //this fires an extra event where isAdjusting is false
    private void setFinalColor(FlatColor selectedColor) {
        setSelectedColor(selectedColor);
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.ColorChanged,selectedColor,this,false));
    }


    public FlatColor getSelectedColor() {
        return selectedColor;
    }

    public Point2D colorToPoint(FlatColor color) {
        //u.p("hue = " + color.getHue() + " " + getWidth() + " sat = " + color.getSaturation() + " brig = " + color.getBrightness());
        double sat = color.getSaturation();
        double bri = color.getBrightness();
        double dx = color.getHue()/360.0*img.getWidth();

        double dy = 0;
        double hh = img.getHeight()/2.0;
        if(sat < 1.0) {
            dy = sat*hh;
        } else {
            dy = hh + (1.0-bri)*hh;
        }

        return new Point2D.Double(dx,dy);
    }
    public void positionAt(double x, double y, FlatColor color) {
        Point2D pt = colorToPoint(color);
        setTranslateX(x-pt.getX()+5);
        setTranslateY(y-pt.getY()-5);
    }

}
