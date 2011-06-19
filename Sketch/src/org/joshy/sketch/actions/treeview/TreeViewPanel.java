package org.joshy.sketch.actions.treeview;

import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.ScrollPane;
import org.joshy.gfx.node.control.TreeView;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;
import org.joshy.sketch.model.SGroup;
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

    public TreeViewPanel(Main main, VectorDocContext ctx) {
        this.main = main;
        this.ctx = ctx;

        this.setBoxAlign(VFlexBox.Align.Stretch);

        this.tree = new TreeView();
        tree.setWidth(500);
        tree.setHeight(500);
        model = new TreeView.AbstractTreeTableModel<Object,String>(){
            public int getColumnCount() {
                return 1;
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
                }
                return "???";
            }

            public String getColumnData(Object o, int i) {
                if(o == null) return "null";
                return o.getClass().getSimpleName();
            }
        };
        model.setRoot(ctx.getDocument());
        tree.setModel(model);

        scroll = new ScrollPane();
        scroll.setContent(tree);
        this.add(scroll,1);
        this.add(new Button("foo"),0);

    }

    public void setDocument(SketchDocument doc) {
        u.p("doc updated");
        model.setRoot(doc);
    }
}

