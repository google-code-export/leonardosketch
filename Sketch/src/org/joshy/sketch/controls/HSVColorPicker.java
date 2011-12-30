package org.joshy.sketch.controls;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.gfx.util.u;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 11/3/11
 * Time: 12:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class HSVColorPicker extends Control implements AbstractColorPickerPopup {
    private BufferedImage img;
    private Image ring;
    private int ringWidth;
    private int initWidth;
    private int initHeight;
    private Point2D dragPoint;
    private double hue;
    private FlatColor selectedColor;
    private GenericColorPickerPopup delegate;
    private Image svImage;
    private BufferedImage svImg;
    private double sat=1;
    private double bri=1;
    int inset = 40;
    private boolean startedDrag;

    public static void main(String ... args) throws Exception {
        Core.init();
        Core.getShared().defer(new Runnable() {
            public void run() {
                Stage stage = Stage.createStage();
                stage.setContent(new HSVColorPicker(null,150,150));
            }
        });

    }


    public HSVColorPicker(GenericColorPickerPopup delegate, int width, int height) {
        this.delegate = delegate;
        this.initWidth = width;
        this.initHeight = height;
        ringWidth = 25;
        img = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Point2D c = new Point(width/2,height/2);
        for(int x=0; x<width; x++) {
            for(int y=0; y<height; y++) {
                Point2D pt = new Point(x,y);
                if(pt.distance(c) > width/2 || pt.distance(c) < width/2-ringWidth) {
                    g.setPaint(new Color(0,0,0,0));
                } else {
                    double angle = GeomUtil.calcAngle(c, pt);
                    int color = Color.HSBtoRGB((float) (angle/360f),1,1);
                    g.setPaint(new Color(color));
                }
                g.drawRect(x,y,1,1);
            }
        }
        ring = Image.create(img);

        svImg = new BufferedImage(initWidth-40*2,initHeight-40*2, BufferedImage.TYPE_INT_ARGB);
        regenSV();
        svImage = Image.create(svImg);


        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public void call(MouseEvent event) {
                processMouse(event);
            }
        });
    }

    private void processMouse(MouseEvent event) {
        if(event.getType() == MouseEvent.MousePressed) {
            startedDrag = true;
        }
        if (event.getType() == MouseEvent.MouseDragged || event.getType() == MouseEvent.MousePressed) {
            Point2D pt = new Point2D.Double(event.getX(),event.getY());
            Point2D c = new Point(initWidth/2,initHeight/2);
            double dist = pt.distance(c);

            //inside the ring
            if(dist < initWidth/2 && dist > initWidth/2-ringWidth) {
                hue = GeomUtil.calcAngle(c, pt);
                dragPoint = GeomUtil.calcPoint(c, hue, initWidth/2-ringWidth/2);
                regenSV();
            }

            //inside the sv rect
            double x = pt.getX()-40;
            double y = pt.getY()-40;
            double w = initWidth-80;
            double h = initHeight-80;
            if(x>0 && x < w && y >0 && y < h) {
                sat = 1-x/w;
                bri = 1-y/h;
            }
            setSelectedColor(FlatColor.hsb(hue, sat, bri));
        }
        if(event.getType() == MouseEvent.MouseReleased && startedDrag) {
            if(delegate != null) {
                delegate.setVisible(false);
            }
            startedDrag = false;
        }
    }

    private void regenSV() {
        Graphics2D g2 = svImg.createGraphics();
        for(int x=0; x<svImg.getWidth(); x++) {
            float sat = ((float)x)/((float)svImg.getWidth());
            sat = 1-sat;
            for(int y=0; y<svImg.getHeight(); y++) {
                float bri = ((float)y)/((float)svImg.getHeight());
                bri = 1-bri;
                int color = Color.HSBtoRGB((float) (hue/360),sat,bri);
                g2.setPaint(new Color(color));
                g2.drawRect(x, y, 1, 1);
            }
        }
        g2.dispose();
    }

    @Override
    public void doLayout() {
        setWidth(ring.getWidth());
        setHeight(ring.getHeight());
    }

    @Override
    public void doPrefLayout() {
    }

    @Override
    public void doSkins() {
    }

    @Override
    public void draw(GFX g) {
        g.drawImage(ring,0,0);
        g.setPaint(FlatColor.BLACK);
        g.setStrokeWidth(2);
        g.drawOval(0-1,0-1,initWidth+2,initHeight+2);
        g.drawOval(0+ringWidth,0+ringWidth,initWidth-ringWidth*2,initHeight-ringWidth*2);
        g.setStrokeWidth(1);

        g.drawImage(svImage,inset,inset);

        g.setPaint(FlatColor.WHITE);
        if(dragPoint != null) {
            g.drawOval(dragPoint.getX()-5,dragPoint.getY()-5,10,10);
        }

        double w = initWidth-inset*2;
        double h = initHeight-inset*2;
        g.drawOval(inset+(1-sat)*w-5,inset+(1-bri)*h-5,10,10);

    }

    public void positionAt(double x, double y, FlatColor color) {
        Point2D pt = new Point(20,20);//colorToPoint(color);
        setTranslateX(x-pt.getX()+5);
        setTranslateY(y - pt.getY() - 5);
    }

    public void setSelectedColor(FlatColor selectedColor) {
        this.selectedColor = selectedColor;
        if(delegate != null) {
            delegate.setSelectedColor(selectedColor);
        }
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.ColorChanged,selectedColor,this,true));
        setDrawingDirty();
    }

    public void startDrag() {
        this.startedDrag = true;
    }
}
