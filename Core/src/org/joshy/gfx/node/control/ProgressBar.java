package org.joshy.gfx.node.control;

import org.joshy.gfx.SkinManager;
import org.joshy.gfx.css.CSSMatcher;
import org.joshy.gfx.css.CSSSkin;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.BackgroundTask;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.ProgressUpdate;
import org.joshy.gfx.node.Bounds;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 23, 2010
 * Time: 9:37:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressBar extends Control {
    double percentage = 0.0;
    private CSSSkin.BoxState size;

    public ProgressBar() {
        doLayout();
        /*
        try {
            fill = PatternPaint.create(new File("assets/progressbar_fill.png"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        */        
    }

    @Override
    public void doSkins() {
        cssSkin = SkinManager.getShared().getCSSSkin();
        setLayoutDirty();
    }

    @Override
    public void doLayout() {
    }
    
    @Override
    public void doPrefLayout() {
        if(cssSkin != null) {
            size = cssSkin.getSize(this,"ASDFASDFASDF");
            if(prefWidth != CALCULATED) {
                setWidth(prefWidth);
                size.width = prefWidth;
            } else {
                setWidth(size.width);
            }
            setHeight(size.height);
        }
    }

    @Override
    public void draw(GFX g) {
        if(!isVisible()) return;
        if(cssSkin != null) {
            if(size == null) {
                doPrefLayout();
            }
            CSSMatcher matcher = new CSSMatcher("ProgressBar");
            cssSkin.drawBackground(g, matcher, "", new Bounds(0,0,getWidth(), getHeight()));
            cssSkin.drawBorder(g, matcher, "", new Bounds(0,0,getWidth(), getHeight()));
            cssSkin.drawBackground(g, matcher, "bar-", new Bounds(0,0,getWidth()*percentage, getHeight()));
            cssSkin.drawBorder(g, matcher, "bar-", new Bounds(0,0,getWidth()*percentage, getHeight()));
        }
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
