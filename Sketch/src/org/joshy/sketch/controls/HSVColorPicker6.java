package org.joshy.sketch.controls;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.event.Event;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.stage.Stage;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/8/11
 * Time: 6:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class HSVColorPicker6 extends Control {
    FlatColor color = FlatColor.PURPLE;
    private Point2D centerPoint = new Point(100,100);
    private FlatColor startColor = FlatColor.RED;

    public static void main(String ... args) throws Exception {
        Core.init();
        Core.getShared().defer(new Runnable() {
            public void run() {
                Stage stage = Stage.createStage();
                stage.setContent(new HSVColorPicker6());
                stage.setWidth(600);
                stage.setHeight(600);
                EventBus.getSystem().addListener(SystemMenuEvent.Quit, new Callback<Event>() {
                    public void call(Event event) throws Exception {
                        System.exit(0);
                    }
                });
            }
        });
    }

    public HSVColorPicker6() {
        EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>(){
            public void call(MouseEvent mouseEvent) throws Exception {
                if(mouseEvent.getType() == MouseEvent.MousePressed) {
                    centerPoint = mouseEvent.getPointInNodeCoords(HSVColorPicker6.this);
                    startColor = color;
                    setDrawingDirty();
                }
                if(mouseEvent.getType() == MouseEvent.MouseDragged) {
                    double x = mouseEvent.getX()-centerPoint.getX();
                    double y = mouseEvent.getY()-centerPoint.getY();
                    float[] comps = toHSB(color);
                    float[] start = toHSB(startColor);
                    if(x > -10 && x < 10 && y > -10 && y < 10) {
                        return;
                    }
                    if(x > -5 && x < 5 && y > -100 && y < 100) {
                        double off = start[0];
                        color = FlatColor.hsb((y/200.0+off)*360.0,comps[1],comps[2]);
                        setDrawingDirty();
                        return;
                    }


                    double sin = Math.sin(Math.toRadians(-30));
                    double cos = Math.cos(Math.toRadians(-30));
                    double tx = cos*x + sin*y;
                    double ty = sin*x + cos*y;
                    if(tx > -100 && tx < 100 && ty > -5 && ty < 5) {
                        double sat = (tx + 100)/100.0;
                        sat = start[1] - (1-sat);
                        sat = clamp(0,sat,1);
                        color = FlatColor.hsb(comps[0]*360,sat,comps[2]);
                        setDrawingDirty();
                        return;
                    }

                    sin = Math.sin(Math.toRadians(60));
                    cos = Math.cos(Math.toRadians(60));
                    tx = cos*x + sin*y;
                    ty = sin*x + cos*y;
                    if(tx > -5 && tx < 5 && ty > -100 && ty < 100) {
                        double bright = ty/100.0;
                        bright = 1.0-bright;
                        bright = start[2] - (1-bright);
                        bright = clamp(0,bright,1);
                        color = FlatColor.hsb(comps[0]*360,comps[1], bright);
                        setDrawingDirty();
                        return;
                    }

                    return;
                }
            }
        });
    }

    private double clamp(double min, double value, double max) {
        if(value < min) return min;
        if(value > max) return max;
        return value;
    }

    @Override
    public void doLayout() {
        setWidth(500);
        setHeight(500);
    }

    @Override
    public void doPrefLayout() {

    }

    @Override
    public void doSkins() {

    }

    @Override
    public void draw(GFX gfx) {
        float[] curr = toHSB(color);
        float[] start = toHSB(startColor);


        //hue, vertical
        gfx.translate(centerPoint.getX(),centerPoint.getY());
        for(double i=0; i<200; i++) {
            FlatColor c = FlatColor.hsb(i/200*360.0, curr[1], curr[2]);
            gfx.setPaint(c);
            double y = start[0]*200;
            gfx.drawLine(-5,i-y,+5, i-y);
        }
        gfx.translate(-centerPoint.getX(),-centerPoint.getY());



        //saturation, 30 degrees
        gfx.translate(centerPoint.getX(),centerPoint.getY());
        for(double i=0; i<200; i++) {
            FlatColor c = FlatColor.hsb(curr[0]*360, i/200.0, curr[2]);
            gfx.setPaint(c);
            double sin = Math.sin(Math.toRadians(30));
            double cos = Math.cos(Math.toRadians(30));
            double x = cos*i - cos*start[1]*200 + 0;
            double y = sin*i - sin*start[1]*200 + 0;
            //tx = cos(30)*i
            //tx/cos(30) = i
            gfx.drawLine(x,y-5,x,y+5);
        }
        gfx.translate(-centerPoint.getX(),-centerPoint.getY());



        //brightness, 150 degrees
        gfx.translate(centerPoint.getX(),centerPoint.getY());
        for(double i=0; i<200; i++) {
            FlatColor c = FlatColor.hsb(curr[0]*360, curr[1], i/200.0);
            gfx.setPaint(c);
            double sin = Math.sin(Math.toRadians(150));
            double cos = Math.cos(Math.toRadians(150));
            double x = cos*i - cos*start[2]*200;
            double y = sin*i - sin*start[2]*200;
            gfx.drawLine(x,y-5,x,y+5);
        }
        gfx.translate(-centerPoint.getX(),-centerPoint.getY());


        gfx.setPaint(color);
        double s = 16;
        gfx.fillOval(centerPoint.getX()-s/2,centerPoint.getY()-s/2,s,s);
        gfx.setPaint(FlatColor.BLACK);
        gfx.drawOval(centerPoint.getX()-s/2,centerPoint.getY()-s/2,s,s);
        gfx.drawRect(0,0,getWidth(), getHeight());
    }

    private float[] toHSB(FlatColor color) {

        java.awt.Color col = new java.awt.Color(
                (float) color.getRed(),
                (float) color.getGreen(),
                (float) color.getBlue(),
                1.0f);
        float[] comps = java.awt.Color.RGBtoHSB(
                col.getRed(),
                col.getGreen(),
                col.getBlue(),
                null
        );
        return comps;
    }
}
