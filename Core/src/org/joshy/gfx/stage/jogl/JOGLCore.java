package org.joshy.gfx.stage.jogl;

import org.joshy.gfx.Core;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.stage.Stage;

import javax.swing.*;

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
        //To change body of implemented methods use File | Settings | File Templates.
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
