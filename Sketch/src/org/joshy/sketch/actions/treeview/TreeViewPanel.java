package org.joshy.sketch.actions.treeview;

import java.util.ArrayList;
import java.util.List;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.Event;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.sketch.Main;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;

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
    private Checkbox bitmap_text;
    
    private List<Control> customPropsControls = new ArrayList<Control>();
    private Checkbox lock_node;
    private Checkbox visible_node;
    private Textbox width_box;
    private Textbox height_box;

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
        
        
        //lock
        lock_node = new Checkbox("locked");
        propsPanel.add(new HFlexBox()
            .add(lock_node));
        EventBus.getSystem().addListener(lock_node, ActionEvent.Action, new Callback<Event>() {
            public void call(Event event) throws Exception {
                Selection sel = ctx.getSelection();
                if (sel.size() == 1) {
                    SNode n = sel.firstItem();
                    n.setLocked(lock_node.isSelected());
                }
            }
        });

        //visible
        visible_node = new Checkbox("visible");
        propsPanel.add(new HFlexBox().add(visible_node));
        EventBus.getSystem().addListener(visible_node, ActionEvent.Action, new Callback<Event>() {
            public void call(Event event) throws Exception {
                Selection sel = ctx.getSelection();
                if (sel.size() == 1) {
                    SNode n = sel.firstItem();
                    n.setVisible(visible_node.isSelected());
                }
            }
        });

        //cache
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
        bitmap_text = new Checkbox("use bitmap text");
        EventBus.getSystem().addListener(bitmap_text, ActionEvent.Action, new Callback<Event>() {
            public void call(Event event) throws Exception {
                Selection sel = ctx.getSelection();
                if (sel.size() == 1) {
                    SNode n = sel.firstItem();
                    n.setBooleanProperty("com.joshondesign.amino.bitmaptext",bitmap_text.isSelected());
                }
            }
        });
        propsPanel.add(bitmap_text);

        this.add(propsPanel,0);

        addProp("Width","width");
        addProp("Height","height");
        addProp("X:","x");
        addProp("Y:","y");
        EventBus.getSystem().addListener(Selection.SelectionChangeEvent.Changed, selectionCallback);
    }

    private void addProp(String label, final String name) {
        final Textbox box = new Textbox();
        box.setPrefWidth(100);
        propsPanel.add(new HFlexBox()
                .add(new Label(label))
                .add(box));

        EventBus.getSystem().addListener(box, ActionEvent.Action, new Callback<Event>() {
            public void call(Event event) throws Exception {
                if(!main.propMan.isClassAvailable(SResizeableNode.class)) return;
                double v = Double.parseDouble(box.getText());
                main.propMan.getProperty(name).setValue(v);
                ctx.redraw();
            }
        });


        EventBus.getSystem().addListener(Selection.SelectionChangeEvent.Changed, new Callback<Selection.SelectionChangeEvent>() {
            public void call(Selection.SelectionChangeEvent selectionChangeEvent) throws Exception {
                if(!main.propMan.isClassAvailable(SResizeableNode.class)) return;
                box.setText(""+main.propMan.getProperty(name).getDoubleValue());
            }
        });
    }

    private Callback<? extends Selection.SelectionChangeEvent> selectionCallback = new Callback<Selection.SelectionChangeEvent>() {
        public void call(Selection.SelectionChangeEvent selectionEvent) throws Exception {
            Selection sel = selectionEvent.getSelection();

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
                    lock_node.setSelected(n.isLocked());
                    visible_node.setSelected(n.isVisible());
                }
            } else {
                all_name.setText("");
            }

            //remove custom controls before adding them back
            for(Control control : customPropsControls) {
                TreeViewPanel.this.remove(control);
            }
            customPropsControls.clear();

            if(!sel.isEmpty()) {
                SNode n = sel.firstItem();
                if(n instanceof SText) {
                    bitmap_text.setEnabled(true);
                    bitmap_text.setSelected(n.getBooleanProperty("com.joshondesign.amino.bitmaptext"));
                } else {
                    bitmap_text.setEnabled(false);
                    bitmap_text.setSelected(false);
                }

                if(n instanceof CustomProperties) {
                    for(Control control : ((CustomProperties)n).getControls()) {
                        customPropsControls.add(control);
                        TreeViewPanel.this.add(control);
                    }
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
        }
    };

    public void setDocument(SketchDocument doc) {
        model.setRoot(doc);
    }
}

