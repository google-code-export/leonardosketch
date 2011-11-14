package org.joshy.sketch.controls;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.PatternPaint;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.Event;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.SystemMenuEvent;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.control.Checkbox;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.GraphicsUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 11/10/11
 * Time: 7:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatternBuilder extends VFlexBox {
    private FreerangeColorPicker color1;
    private FreerangeColorPicker color2;
    private PreviewControl preview;
    private Slider width1;
    private Slider width2;
    private Slider stripeAngle;
    private Checkbox showPatternGrid;
    private PatternPaint pat;
    private PatternPaint noisePat;
    private Slider noiseAmount;

    public static void main(String ... args) throws Exception {
        Core.init();
        Core.getShared().defer(new Runnable() {
            public void run() {
                final Stage stage = Stage.createStage();
                stage.setContent(new PatternBuilder());
                stage.setWidth(800);
                stage.setHeight(600);

                EventBus.getSystem().addListener(SystemMenuEvent.Quit, new Callback<Event>() {
                    public void call(Event event) throws Exception {
                        stage.hide();
                        System.exit(0);
                    }
                });

            }
        });
    }


    public PatternPaint getPattern() {
        return pat;
    };

    public PatternBuilder() {

        color1 = new FreerangeColorPicker();
        color1.setPrefWidth(30);
        color1.setPrefHeight(30);
        color1.setSelectedColor(FlatColor.BLUE);
        color1.setRecenterOnSelect(false);
        this.add(color1, 0);

        color2 = new FreerangeColorPicker();
        color2.setPrefWidth(30);
        color2.setPrefHeight(30);
        color2.setSelectedColor(FlatColor.WHITE);
        color2.setRecenterOnSelect(false);
        this.add(color2,0);

        //stripes vs glyph vs plain
        //stripe width vs glyph picker
        width1 = new Slider(false);
        width1.setMin(1).setMax(64).setValue(32);
        add(new HFlexBox()
                .add(new Label("stripe 1 width"), 0)
                .add(width1, 0)
                ,0);

        width2 = new Slider(false);
        width2.setMin(1).setMax(64).setValue(32);
        add(new HFlexBox()
                .add(new Label("stripe 2 width"),0)
                .add(width2,0)
                ,0);

        stripeAngle = new Slider(false);
        stripeAngle.setMin(0).setMax(90).setValue(0);
        add(new HFlexBox()
                .add(new Label("stripe angle"), 0)
                .add(stripeAngle, 0)
                , 0);

        noiseAmount = new Slider(false);
        noiseAmount.setMin(0).setMax(100).setValue(0);
        add(new HFlexBox()
                .add(new Label("noise"),0)
                .add(noiseAmount,0)
                ,0);

        showPatternGrid = new org.joshy.gfx.node.control.Checkbox("Show pattern grid");
        add(showPatternGrid,0);

        preview = new PreviewControl();
        this.add(preview,0);


        BufferedImage noise = NoiseGen.render(200,200);
        try {
            noisePat = PatternPaint.create(noise, noise.getWidth(), noise.getHeight());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private class PreviewControl extends Control {

        private PreviewControl() {
            this.setPrefWidth(200);
            this.setPrefHeight(200);
        }

        @Override
        public void doLayout() {
            this.setWidth(this.getPrefWidth());
            this.setHeight(this.getPrefHeight());
        }

        @Override
        public void doPrefLayout() {  }

        @Override
        public void doSkins() {  }

        @Override
        public void draw(GFX g2) {
            g2.setPaint(FlatColor.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            //int angle = (int) GeomUtil.snapTo45(stripeAngle.getValue());
            int angle = (int)((Math.floor(stripeAngle.getValue()/15))*15);
            double sw = width1.getValue()+width2.getValue();
            double ih = sw;
            double iw = sw;
            if(angle > 0 && angle < 90) {
                double theta = Math.toRadians(angle);
                iw = sw/Math.cos(theta);
                ih = sw/Math.sin(theta);
            }

            g2.setPaint(noisePat);
            g2.fillRect(0,0,getWidth(),getHeight());


            BufferedImage img = new BufferedImage(
                    (int) iw,
                    (int) ih,
                    BufferedImage.TYPE_INT_ARGB);
            try {
                pat = PatternPaint.create(img,img.getWidth(),img.getHeight());
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            Graphics2D gfx = img.createGraphics();
            gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);


            gfx.rotate(Math.toRadians(angle));

            //stripe one (background)
            gfx.setPaint(GraphicsUtil.toAWT(color1.getSelectedColor()));
            gfx.fillRect(0, (int) -getHeight(), (int) getWidth(), (int) getHeight() * 2);

            //stripe two (foreground)
            gfx.setPaint(GraphicsUtil.toAWT(color2.getSelectedColor()));
            gfx.fillRect((int)width1.getValue(), (int) -getHeight(), (int) width2.getValue(), (int) getHeight() * 2);
            gfx.fillRect((int)(width1.getValue()*2+width2.getValue()), (int)-getHeight(), (int) width2.getValue(), (int) getHeight()*2);
            gfx.rotate(-Math.toRadians(angle));

            if(showPatternGrid.isSelected()) {
                gfx.setPaint(GraphicsUtil.toAWT(FlatColor.GRAY));
                gfx.drawRect(0,0,img.getWidth(),img.getHeight());
            }

            gfx.dispose();

            g2.setOpacity(1.0-(noiseAmount.getValue()/100.0));
            g2.setPaint(pat);
            g2.fillRect(0,0,getWidth(),getHeight());

            g2.setPaint(FlatColor.BLACK);
            g2.drawRect(0, 0, getWidth(), getHeight());
        }
    }

    private static class NoiseGen {
        private static BufferedImage render(int width, int height) {
            BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            img = graynoise(img);
            return img;
        }
        private static BufferedImage graynoise(BufferedImage img) {
            for(int x=0; x<img.getWidth(); x++) {
                for(int y=0; y<img.getHeight(); y++) {
                    int v = (int)(Math.random()*256);
                    setRGB(img,x,y,v,v,v);
                }
            }
            return img;
        }
        private static void setRGB(BufferedImage img, int x, int y, int v, int v1, int v2) {
            img.setRGB(x,y,(0xFF<<24)|(v<<16)|(v1<<8)|v2);
        }
    }
}
