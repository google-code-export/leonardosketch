package org.joshy.sketch.actions.treeview;

import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.Event;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.sketch.Main;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.SGroup;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 6/18/11
 * Time: 6:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class TreeViewPanel extends VFlexBox {
    private Main main;
    private VectorDocContext ctx;
    private TreeView tree;
    private ScrollPane scroll;
    private TreeView.AbstractTreeTableModel<Object, String> model;
    private VFlexBox propsPanel;
    private Textbox all_name;
    private Checkbox all_cache;
    private Checkbox all_cache_image;

    public TreeViewPanel(Main main, final VectorDocContext ctx) {
        this.main = main;
        this.ctx = ctx;

        this.setBoxAlign(VFlexBox.Align.Stretch);

        this.tree = new TreeView();
        tree.setWidth(500);
        tree.setHeight(500);
        model = new TreeView.AbstractTreeTableModel<Object,String>(){
            public int getColumnCount() {
                return 2;
            }

            public boolean hasChildren(Object o) {
                if(o instanceof SketchDocument) return true;
                if(o instanceof SketchDocument.SketchPage) return true;
                if(o instanceof SGroup) return true;
                return false;
            }

            public Iterable<? extends Object> getChildren(Object o) {
                if(o instanceof SketchDocument) {
                    SketchDocument sd = (SketchDocument) o;
                    return sd.getPages();
                }
                if (o instanceof SketchDocument.SketchPage) {
                    SketchDocument.SketchPage sp = (SketchDocument.SketchPage) o;
                    return sp.getNodes();
                }
                if (o instanceof SGroup) {
                    SGroup g = (SGroup) o;
                    return g.getNodes();
                }
                return  new ArrayList<Object>();
            }

            public String getColumnHeader(int i) {
                switch(i) {
                    case 0: return "Kind";
                    case 1: return "Name";
                }
                return "???";
            }

            public String getColumnData(Object o, int i) {
                if(o == null) return "null";
                if(i == 1) {
                    if(o instanceof SNode) {
                        String id =  ((SNode)o).getId();
                        if(id == null) return "";
                        return id;
                    }
                    return "";
                }
                return o.getClass().getSimpleName();
            }
        };
        model.setRoot(ctx.getDocument());
        tree.setModel(model);

        scroll = new ScrollPane();
        scroll.setContent(tree);
        this.add(scroll,1);

        propsPanel = new VFlexBox();
        //propsPanel.setPrefHeight(200);


        //all nodes

        //name
        all_name = new Textbox();
        all_name.setPrefWidth(100);
        all_name.setHintText("no name");
        propsPanel.add(new HFlexBox()
            .add(new Label("name"))
            .add(all_name));
        EventBus.getSystem().addListener(all_name, ActionEvent.Action, new Callback<Event>() {
             public void call(Event event) throws Exception {
                 Selection sel = ctx.getSelection();
                 if (sel.size() == 1) {
                     SNode n = sel.firstItem();
                     n.setId(all_name.getText());
                 }
             }
         });

        //cache
        //name
        all_cache = new Checkbox("cache");
        EventBus.getSystem().addListener(all_cache, ActionEvent.Action, new Callback<Event>() {
            public void call(Event event) throws Exception {
                Selection sel = ctx.getSelection();
                if (sel.size() == 1) {
                    SNode n = sel.firstItem();
                    n.setBooleanProperty("com.joshondesign.amino.nodecache",all_cache.isSelected());
                }
            }
        });
        propsPanel.add(all_cache);
        //cache as png image
        all_cache_image = new Checkbox("cache as image");
        EventBus.getSystem().addListener(all_cache_image, ActionEvent.Action, new Callback<Event>() {
            public void call(Event event) throws Exception {
                Selection sel = ctx.getSelection();
                if (sel.size() == 1) {
                    SNode n = sel.firstItem();
                    n.setBooleanProperty("com.joshondesign.amino.nodecacheimage",all_cache_image.isSelected());
                }
            }
        });
        propsPanel.add(all_cache_image);

        //text
        //cache as dynamic bitmap text

        this.add(propsPanel,0);


        EventBus.getSystem().addListener(Selection.SelectionChangeEvent.Changed, selectionCallback);
    }

    private Callback<? extends Selection.SelectionChangeEvent> selectionCallback = new Callback<Selection.SelectionChangeEvent>() {
        public void call(Selection.SelectionChangeEvent selectionEvent) throws Exception {
            Selection sel = selectionEvent.getSelection();
            //u.p("selection changed");

            boolean enabled = sel.size()==1;

            all_name.setEnabled(enabled);
            all_cache.setEnabled(enabled);
            all_cache_image.setEnabled(enabled);

            if(enabled) {
                SNode n = sel.firstItem();
                if(n != null) {
                    if(n.getId() != null) {
                        all_name.setText(n.getId());
                    } else {
                        all_name.setText("");

                    }
                    all_cache.setSelected(n.getBooleanProperty("com.joshondesign.amino.nodecache"));
                    all_cache_image.setSelected(n.getBooleanProperty("com.joshondesign.amino.nodecacheimage"));
                }
            } else {
                all_name.setText("");
            }

            for(int i=0; i<model.getRowCount(); i++) {
                Object row = model.get(i,0);
                //u.p("row = " + row);
                if(row instanceof SNode && sel.contains((SNode)row)) {
                    tree.setSelectedRow(i);
                    return;
                }
            }

        }
    };

    public void setDocument(SketchDocument doc) {
        model.setRoot(doc);
    }
}

