package org.joshy.sketch.controls;

import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.ListModel;
import org.joshy.gfx.node.control.PopupMenu;
import org.joshy.gfx.node.layout.Container;
import org.joshy.gfx.stage.Stage;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.tools.SelectMoveTool;

import java.awt.geom.Point2D;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 7, 2010
 * Time: 4:43:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContextMenu extends PopupMenu {
    private static ContextMenu sharedContextMenu;
    private static Container sharedLayer;

    private ArrayList<SelectMoveTool.ActionItem> items;
    ListModel mdl = new ListModel<CharSequence>() {
        public CharSequence get(int i) {
            return items.get(i).getLabel();
        }
        public int size() {
            return items.size();
        }
    };


    public ContextMenu() {
        items = new ArrayList<SelectMoveTool.ActionItem>();
        setModel(mdl);
        setCallback(new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) throws Exception {
                int i = ((Integer)event.getValue());
                SAction action = getAction(i);
                action.execute();
            }
        });

    }

    public void addActions(SAction ... actions) {
        for(SAction a : actions) {
            items.add(new SelectMoveTool.ActionItem(a,a.getDisplayName()));
        }
    }

    public SAction getAction(int i) {
        return this.items.get(i).action;
    }

    public void show(Node node, double x, double y) {
        if(sharedContextMenu != null) {
            sharedContextMenu.setVisible(false);
            sharedLayer.remove(sharedContextMenu);
        }
        Stage stage = node.getParent().getStage();
        sharedLayer = stage.getPopupLayer();
        Point2D pt = NodeUtils.convertToScene(node, x, y);
        pt = NodeUtils.convertFromScene(sharedLayer,pt);
        setTranslateX(pt.getX()+2);
        setTranslateY(pt.getY());
        setVisible(true);
        sharedContextMenu = this;
        sharedLayer.add(this);
    }

    public static void hideAll() {
        if(sharedContextMenu != null) {
            sharedContextMenu.setVisible(false);
            sharedLayer.remove(sharedContextMenu);
        }
    }
}
