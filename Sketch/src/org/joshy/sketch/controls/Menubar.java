package org.joshy.sketch.controls;

import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.SkinEvent;
import org.joshy.gfx.util.u;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Nov 29, 2010
 * Time: 9:39:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class Menubar {
    private List<Menu> menus = new ArrayList<Menu>();
    private JFrame jframe;
    private JMenuBar jmenubar;

    public Menubar(JFrame frame) {
        jframe = frame;
        jmenubar = new JMenuBar();
        jframe.setJMenuBar(jmenubar);
        EventBus.getSystem().addListener(SkinEvent.SystemWideReload, new Callback<SkinEvent>() {
            public void call(SkinEvent skinEvent) throws Exception {
                u.p("skins were reloaded system wide. time to refresh the jmenus");
                jmenubar.removeAll();
                for(Menu m : menus) {
                    jmenubar.add(m.createJMenu());
                }
            }
        });
    }

    public void remove(Menu menu) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public void add(Menu menu) {
        this.menus.add(menu);
        this.jmenubar.add(menu.createJMenu());
    }
}
