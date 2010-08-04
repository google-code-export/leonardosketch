package org.joshy.gfx.node.control;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.PatternPaint;
import org.joshy.gfx.event.BackgroundTask;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.ProgressUpdate;
import org.joshy.gfx.util.u;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 23, 2010
 * Time: 9:37:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressBar extends Control {
    double percentage = 0.0;
    private PatternPaint fill;

    public ProgressBar() {
        doLayout();
        try {
            fill = PatternPaint.create(new File("assets/progressbar_fill.png"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void doSkins() {
        u.p("prog bar doesn't use skins yet");
    }

    @Override
    public void doLayout() {
        setWidth(100);
        setHeight(20);
    }
    
    @Override
    public void draw(GFX g) {
        g.setPaint(FlatColor.WHITE);
        g.fillRect(0,0,getWidth(),getHeight());

        // a little animation to make the background texture appear to scroll
        double ax = -getWidth()*percentage;
        //g.translate(ax,0);
        g.setPaint(fill);
        //g.setPaint(FlatColor.GREEN);
        g.fillRect(0,0,getWidth()*percentage,getHeight());
        //g.translate(-ax,0);
        g.setPaint(FlatColor.BLACK);
        g.drawRect(0,0,getWidth(),getHeight());

    }



    public void setTask(BackgroundTask task) {
        EventBus.getSystem().addListener(task, ProgressUpdate.TYPE, new Callback<ProgressUpdate>() {
            public void call(ProgressUpdate event) {
                percentage = event.getPercentage();
                setDrawingDirty();
            }
        });
        setDrawingDirty();
    }

}
