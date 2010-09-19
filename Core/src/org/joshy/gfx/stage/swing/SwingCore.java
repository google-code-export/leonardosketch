package org.joshy.gfx.stage.swing;

import org.joshy.gfx.Core;
import org.joshy.gfx.SkinManager;
import org.joshy.gfx.css.CSSProcessor;
import org.joshy.gfx.css.CSSRuleSet;
import org.joshy.gfx.css.CSSSkin;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.u;
import org.parboiled.support.ParsingResult;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 23, 2010
 * Time: 3:23:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class SwingCore extends Core {
    private List<Stage> stages = new ArrayList<Stage>();

    public SwingCore() {
        super();

    }

    @Override
    public Iterable<Stage> getStages() {
        return stages;
    }

    @Override
    protected void initSkinning() throws Exception {
        URL url = SwingCore.class.getResource("default.css");
        u.p("css resource = " + url);
        ParsingResult<?> result = CSSProcessor.parseCSS(url.openStream());
        CSSRuleSet set = new CSSRuleSet();
        set.setBaseURI(url.toURI());
        CSSSkin cssskin = new CSSSkin();
        cssskin.setRuleSet(set);
        CSSProcessor.condense(result.parseTreeRoot,set);
        u.p("default css parsed from: " + url);
        SkinManager.getShared().setCSSSkin(cssskin);
    }

    @Override
    protected void createDefaultEventBus() {
        EventBus.setSystem(new SwingEventBus());
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    u.p("setting the thread " + Thread.currentThread());
                    _gui_thread = Thread.currentThread();
                }
            } );
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Override
    public void reloadSkins() {
        for(Stage s : getStages()) {
            Node root = s.getContent();
            if(root instanceof Control) {
                ((Control)root).doSkins();
            }
        }
    }

    public void addStage(SwingStage swingStage) {
        this.stages.add(swingStage);
    }

}
