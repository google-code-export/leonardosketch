package org.joshy.gfx.stage.jogl;

import org.joshy.gfx.Core;
import org.joshy.gfx.SkinManager;
import org.joshy.gfx.css.CSSProcessor;
import org.joshy.gfx.css.CSSRuleSet;
import org.joshy.gfx.css.CSSSkin;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.stage.swing.SwingCore;
import org.joshy.gfx.util.u;
import org.parboiled.support.ParsingResult;

import javax.swing.*;
import java.net.URL;

public class JOGLCore extends Core {
    public JOGLCore() {
        super();
    }

    @Override
    public Iterable<Stage> getStages() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        EventBus.setSystem(new JOGLEventBus());
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    _gui_thread = Thread.currentThread();
                }
            } );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reloadSkins() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
