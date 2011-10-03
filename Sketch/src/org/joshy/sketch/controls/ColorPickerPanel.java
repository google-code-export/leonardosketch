package org.joshy.sketch.controls;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.ImageBuffer;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.control.Radiobutton;
import org.joshy.gfx.node.control.Textbox;
import org.joshy.gfx.node.layout.GridBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.sketch.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 4/7/11
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class ColorPickerPanel extends org.joshy.gfx.node.layout.Panel {

    public static void main(String ... args) throws Exception {
        Core.init();
        Core.getShared().defer(new Runnable() {
            public void run() {

                InputStream stream = ColorPickerPanel.class.getResourceAsStream("colorpickerpanel.css");
                URL uri = ColorPickerPanel.class.getResource("colorpickerpanel.css");
                try {
                    Core.getShared().loadCSS(stream,uri);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (URISyntaxException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                Stage stage = Stage.createStage();
                stage.setContent(new ColorPickerPanel(250,200));
                EventBus.getSystem().addListener(SystemMenuEvent.Quit, new Callback<Event>() {
                    public void call(Event event) throws Exception {
                        System.exit(0);
                    }
                });
            }
        });
    }

    private FlatColor color = FlatColor.hsb(85,0.5,0.5);

    private Control area;
    private Control slider;
    private Control preview;
    private Radiobutton hueSelect;
    private Radiobutton satSelect;
    private Radiobutton brightSelect;
    private Radiobutton redSelect;
    private Radiobutton greenSelect;
    private Radiobutton blueSelect;
    private ToggleGroup select;
    private Textbox hueText;
    private Textbox satText;
    private Textbox brightText;
    private Textbox redText;
    private Textbox greenText;
    private Textbox blueText;
    private Textbox hexText;


    private void setColor(FlatColor color) {
        this.color = color;
        hueText.setText(""+(int)color.getHue());
        satText.setText(""+(int)(color.getSaturation()*100));
        brightText.setText(""+(int)(color.getBrightness()*100));
        redText.setText(""+(int)(color.getRed()*255));
        greenText.setText(""+(int)(color.getGreen()*255));
        blueText.setText(""+(int)(color.getBlue()*255));
        hexText.setText(""+Integer.toHexString(color.getRGBA()&0x00FFFFFF));
        setDrawingDirty();
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.ColorChanged,this.color,this,true));

    }

    ColorPickerPanel() {
        this(300,300);
    }

    public ColorPickerPanel(int width, int height) {
        this.setPrefWidth(width);
        this.setPrefHeight(height);
        setFill(FlatColor.fromRGBInts(0xf0,0xf0,0xf0));

        final int size = width-100;
        area = new Control() {
            public ImageBuffer img;

            @Override
            public void doLayout() {            }

            @Override
            public void doPrefLayout() {
                this.setWidth(size);
                this.setHeight(size);
            }

            @Override
            public void doSkins() {      }

            @Override
            public void draw(GFX gfx) {
                if(img == null) {
                    img =new ImageBuffer((int)size,(int)size);
                }
                double hue = getColor().getHue();
                double sat = getColor().getSaturation();
                double bri = getColor().getBrightness();
                double red = getColor().getRed();
                double green = getColor().getGreen();
                double blue = getColor().getBlue();
                for(int x=0; x<size; x++) {
                    for(int y=0; y<size; y++) {
                        FlatColor c = FlatColor.BLACK;
                        if(select.getSelectedButton() == hueSelect) {
                            c = FlatColor.hsb(hue,(double)x/size,1.0-(double)y/size);
                        }
                        if(select.getSelectedButton() == satSelect) {
                            c = FlatColor.hsb((double)x/size*360.0,sat,1.0-(double)y/size);
                        }
                        if(select.getSelectedButton() == brightSelect) {
                            c = FlatColor.hsb((double)x/size*360.0,1.0-(double)y/size,bri);
                        }
                        if(select.getSelectedButton() == redSelect) {
                            c = new FlatColor(red,1.0-(double)y/size,(double)x/size,1);
                        }
                        if(select.getSelectedButton() == greenSelect) {
                            c = new FlatColor(1.0-(double)y/size,green,(double)x/size,1);
                        }
                        if(select.getSelectedButton() == blueSelect) {
                            c = new FlatColor( (double)x/size, 1.0-(double)y/size, blue, 1);
                        }
                        img.buf.setRGB(x, y, c.getRGBA());
                    }
                }
                gfx.draw(img,0,0);
                gfx.setPaint(FlatColor.BLACK);
                gfx.drawRect(0,0,getWidth(),getHeight());
                double x = 0;
                double y = 0;
                if(select.getSelectedButton() == hueSelect){
                    x = getColor().getSaturation()*size;
                    y = (1.0- getColor().getBrightness())*size;
                }
                if(select.getSelectedButton() == satSelect){
                    x = getColor().getHue()/360.0*size;
                    y = (1.0- getColor().getBrightness())*size;
                }
                if(select.getSelectedButton() == brightSelect){
                    x = getColor().getHue()/360.0*size;
                    y = (1.0- getColor().getSaturation())*size;
                }
                if(select.getSelectedButton() == redSelect){
                    y = (1.0-getColor().getGreen())*size;
                    x = getColor().getBlue()*size;
                }
                if(select.getSelectedButton() == greenSelect){
                    y = (1.0-getColor().getRed())*size;
                    x = getColor().getBlue()*size;
                }
                if(select.getSelectedButton() == blueSelect){
                    x = getColor().getRed()*size;
                    y = (1.0-getColor().getGreen())*size;
                }
                gfx.setPaint(FlatColor.BLACK);
                gfx.drawOval(x-3,y-3,6,6);
                gfx.setPaint(FlatColor.WHITE);
                gfx.drawOval(x-2,y-2,4,4);
            }
        };

        EventBus.getSystem().addListener(area, MouseEvent.MouseDragged, new Callback<MouseEvent>() {
            public void call(MouseEvent event) throws Exception {
                double x = Util.clamp(0, event.getX(), size);
                double y = Util.clamp(0,event.getY(),size);
                if(select.getSelectedButton() == hueSelect) {
                    setColor(FlatColor.hsb(getColor().getHue(),x/size,1.0-y/size));
                }
                if(select.getSelectedButton() == satSelect) {
                    setColor(FlatColor.hsb(x/size*360, getColor().getSaturation(),1.0-y/size));
                }
                if(select.getSelectedButton() == brightSelect) {
                    setColor(FlatColor.hsb(x/size*360,1.0-y/size, getColor().getBrightness()));
                }
                if(select.getSelectedButton() == redSelect) {
                    setColor(new FlatColor(getColor().getRed(),1.0-y/size,x/size,1));
                }
                if(select.getSelectedButton() == greenSelect) {
                    setColor(new FlatColor(1.0-y/size, getColor().getGreen(),x/size,1));
                }
                if(select.getSelectedButton() == blueSelect) {
                    setColor(new FlatColor(x/size,1.0-y/size, getColor().getBlue(), 1));
                }
            }
        });

        area.setTranslateX(0);
        area.setTranslateY(0);
        this.add(area);

        slider = new Control() {
            @Override
            public void doLayout() {            }
            @Override
            public void doPrefLayout() {
                this.setWidth(15);
                this.setHeight(size);
            }
            @Override
            public void doSkins() {           }
            @Override
            public void draw(GFX gfx) {
                double hue = getColor().getHue();
                double sat = getColor().getSaturation();
                double bri = getColor().getBrightness();
                double red = getColor().getRed();
                double green = getColor().getGreen();
                double blue = getColor().getBlue();
                for(int i=0; i<size;i++) {
                    if(select.getSelectedButton() == hueSelect) {
                        hue = ((double)i)/size*360.0;
                        gfx.setPaint(FlatColor.hsb(hue,1,1));
                    }
                    if(select.getSelectedButton() == satSelect) {
                        sat = (double)i/size;
                        gfx.setPaint(FlatColor.hsb(hue,1.0-sat,bri));
                    }
                    if(select.getSelectedButton() == brightSelect) {
                        bri = (double)i/size;
                        gfx.setPaint(FlatColor.hsb(hue,sat,1.0-bri));
                    }
                    if(select.getSelectedButton() == redSelect) {
                        red = (double)i/size;
                        gfx.setPaint(new FlatColor(1.0-red,green,blue,1));
                    }
                    if(select.getSelectedButton() == greenSelect) {
                        green = (double)i/size;
                        gfx.setPaint(new FlatColor(red,1.0-green,blue,1));
                    }
                    if(select.getSelectedButton() == blueSelect) {
                        blue = (double)i/size;
                        gfx.setPaint(new FlatColor(red,green,1.0-blue,1));
                    }
                    gfx.fillRect(0,i,getWidth(),1);
                }
                gfx.setPaint(FlatColor.BLACK);
                gfx.drawRect(0,0,getWidth(),getHeight());
                double y = 0;
                if(select.getSelectedButton() == hueSelect) y = getColor().getHue()/360.0*size;
                if(select.getSelectedButton() == satSelect) y = (1.0-getColor().getSaturation())*size;
                if(select.getSelectedButton() == brightSelect) y = (1.0-getColor().getBrightness())*size;
                if(select.getSelectedButton() == redSelect) y = (1.0-getColor().getRed())*size;
                if(select.getSelectedButton() == greenSelect) y = (1.0-getColor().getGreen())*size;
                if(select.getSelectedButton() == blueSelect) y = (1.0-getColor().getBlue())*size;
                gfx.setPaint(FlatColor.BLACK);
                gfx.fillRect(0,y,getWidth(),1);
                gfx.setPaint(FlatColor.WHITE);
                gfx.fillRect(0,y+1,getWidth(),1);
            }
        };
        slider.setTranslateX(size+10);
        slider.setTranslateY(0);
        EventBus.getSystem().addListener(slider, MouseEvent.MouseDragged, new Callback<MouseEvent>() {
            public void call(MouseEvent event) throws Exception {
                double hue = getColor().getHue();
                double sat = getColor().getSaturation();
                double bri = getColor().getBrightness();
                double red = getColor().getRed();
                double green = getColor().getGreen();
                double blue = getColor().getBlue();

                double y = Util.clamp(0,event.getY(),size);
                if(select.getSelectedButton() == hueSelect) {
                    hue = y/size * 360;
                    setColor(FlatColor.hsb(hue,sat,bri));
                }
                if(select.getSelectedButton() == satSelect) {
                    sat = 1.0-y/size;
                    setColor(FlatColor.hsb(hue,sat,bri));
                }
                if(select.getSelectedButton() == brightSelect) {
                    bri = 1.0-y/size;
                    setColor(FlatColor.hsb(hue,sat,bri));
                }
                if(select.getSelectedButton() == redSelect) {
                    red = 1.0-y/size;
                    setColor(new FlatColor(red,green,blue,1));
                }
                if(select.getSelectedButton() == greenSelect) {
                    green = 1.0-y/size;
                    setColor(new FlatColor(red,green,blue,1));
                }
                if(select.getSelectedButton() == blueSelect) {
                    blue = 1.0-y/size;
                    setColor(new FlatColor(red,green,blue,1));
                }
                setDrawingDirty();
            }
        });
        this.add(slider);

        preview = new Control(){
            @Override
            public void doLayout() {            }
            @Override
            public void doPrefLayout() {
                this.setWidth(50);
                this.setHeight(50);
            }
            @Override
            public void doSkins() {            }
            @Override
            public void draw(GFX gfx) {
                gfx.setPaint(getColor());
                gfx.fillRect(0, 0, getWidth(), getHeight());
                gfx.setPaint(FlatColor.BLACK);
                gfx.drawRect(0,0,getWidth(),getHeight());
            }
        };
        preview.setTranslateX(width-60).setTranslateY(0);
        add(preview);

        select = new ToggleGroup();

        hueSelect = new Radiobutton("H:");
        hueText = new Textbox();
        select.add(hueSelect);
        satSelect = new Radiobutton("S:");
        satText = new Textbox();
        select.add(satSelect);
        brightSelect = new Radiobutton("B:");
        brightText = new Textbox();
        select.add(brightSelect);
        redSelect = new Radiobutton("R:");
        redText = new Textbox();
        select.add(redSelect);
        greenSelect = new Radiobutton("G:");
        greenText = new Textbox();
        select.add(greenSelect);
        blueSelect = new Radiobutton("B:");
        blueText = new Textbox();
        select.add(blueSelect);
        hexText = new Textbox();
        GridBox selectors = new GridBox()
                .setPadding(0)
                .createColumn(30,GridBox.Align.Left)
                .createColumn(35, GridBox.Align.Fill)
                .addControl(hueSelect.addCSSClass("rgbbox")).addControl(hueText.addCSSClass("rgbbox"))
                .nextRow()
                .addControl(satSelect.addCSSClass("rgbbox")).addControl(satText.addCSSClass("rgbbox"))
                .nextRow()
                .addControl(brightSelect.addCSSClass("rgbbox")).addControl(brightText.addCSSClass("rgbbox"))
                .nextRow()
                .addControl(redSelect.addCSSClass("rgbbox")).addControl(redText.addCSSClass("rgbbox"))
                .nextRow()
                .addControl(greenSelect.addCSSClass("rgbbox")).addControl(greenText.addCSSClass("rgbbox"))
                .nextRow()
                .addControl(blueSelect.addCSSClass("rgbbox")).addControl(blueText.addCSSClass("rgbbox"))
                .nextRow()
                .addControl(new Label("  #: ").addCSSClass("rgbbox")).addControl(hexText.addCSSClass("rgbbox"))
                ;
        selectors.debug(false);
        selectors.setPrefWidth(70);
        selectors.setPrefHeight(140);
        add(selectors);
        selectors.setTranslateX(width-60).setTranslateY(60);

        select.setSelectedButton(hueSelect);
        setColor(FlatColor.PURPLE);

    }

    public FlatColor getColor() {
        return color;
    }
}
