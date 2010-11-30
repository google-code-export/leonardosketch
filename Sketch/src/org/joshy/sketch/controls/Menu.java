package org.joshy.sketch.controls;

import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.ToggleAction;
import org.joshy.sketch.canvas.SketchCanvas;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 8, 2010
 * Time: 6:41:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class Menu {
    private CharSequence title;
    private List actions;

    public Menu() {
        actions = new ArrayList();
    }

    public Menu setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    public Menu addItem(CharSequence title, SAction action) {
        return this.addItem(title,null,action);
    }

    public Menu addItem(CharSequence title, String key, SAction action) {
        if(action instanceof ToggleAction) {
            actions.add(new ToggleActionAdapter(title,key, (ToggleAction) action));
        } else {
            actions.add(new ActionAdapter(title,key,action));
        }
        return this;
    }

    public Menu separator() {
        actions.add(new JSeparator());
        return this;
    }

    public Menu addMenu(Menu menu) {
        actions.add(menu);
        return this;
    }

    JMenu createJMenu() {
        JMenu menu = new JMenu(title.toString());
        for(Object action : actions) {
            if(action instanceof JSeparator) {
                menu.add((JSeparator)action);
                continue;
            }
            if(action instanceof ToggleActionAdapter) {
                menu.add(new JCheckBoxMenuItem((ToggleActionAdapter)action));
                continue;
            }
            if(action instanceof ActionAdapter) {
                menu.add((ActionAdapter)action);
                continue;
            }
            if(action instanceof Menu) {
                menu.add(((Menu)action).createJMenu());
                continue;
            }
        }
        return menu;
    }

    private static class ActionAdapter extends AbstractAction {
        private final SketchCanvas canvas;
        private CharSequence name;
        private SAction action;
        private String acceleratorKey;

        public ActionAdapter(SketchCanvas canvas, CharSequence name, String acceleratorKey, SAction action) {
            this.canvas = canvas;
            this.name = name;
            this.action = action;
            this.acceleratorKey = acceleratorKey;
        }
        public ActionAdapter(CharSequence name, String acceleratorKey, SAction action) {
            this(null,name,acceleratorKey,action);
        }

        @Override
        public Object getValue(String key) {
            if(Action.NAME.equals(key)) {
                if(name != null) return name.toString();
                return action.getDisplayName().toString();
            }
            if(Action.ACCELERATOR_KEY.equals(key)) {
                if(acceleratorKey != null) {
                    if(OSUtil.isMac()) {
                        return KeyStroke.getKeyStroke("meta " + acceleratorKey);
                    } else {
                        return KeyStroke.getKeyStroke("control " + acceleratorKey);
                    }
                }
                return null;
            }
            return super.getValue(key);
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            try {
                action.execute();
            } catch (Exception e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private class ToggleActionAdapter extends ActionAdapter {
        private ToggleAction toggleAction;

        public ToggleActionAdapter(CharSequence title, String key, ToggleAction action) {
            super(title,key,action);
            toggleAction = action;
        }

        @Override
        public void putValue(String key, Object newValue) {
            if(Action.SELECTED_KEY.equals(key)) {
                toggleAction.setToggleState((Boolean)newValue);
            }
            super.putValue(key, newValue);
        }

        @Override
        public Object getValue(String key) {
            if(Action.SELECTED_KEY.equals(key)) {
                return toggleAction.getToggleState();
            }
            return super.getValue(key);
        }
    }
}
