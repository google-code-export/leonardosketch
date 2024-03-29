package org.joshy.sketch.actions.symbols;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.controls.ContextMenu;
import org.joshy.sketch.controls.StandardDialog;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SelfDrawable;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Point2D;

import static org.joshy.gfx.util.localization.Localization.getString;

public class SymbolPanel extends Panel {
    private double size = 75;
    private ListView<SNode> listView;
    private ScrollPane symbolPane;
    private Button symbolAddButton;
    private ContextMenu contextMenu;
    private SymbolManager symbolManager;
    private static final String SYMBOL_NAME = "SymbolName";
    private VectorDocContext context;
    private PopupMenuButton<SymbolManager.SymbolSet> setSwitcher;

    public SymbolPanel(final SymbolManager symbolManager, final VectorDocContext context) {
        this.context = context;
        this.listView = new ListView<SNode>();
        this.symbolManager = symbolManager;

        listView.setModel(symbolManager.getModel());
        listView.setRowHeight(size);
        listView.setColumnWidth(size);
        listView.setOrientation(ListView.Orientation.HorizontalWrap);
        listView.setRenderer(new ListView.ItemRenderer<SNode>(){
            public void draw(GFX gfx, ListView listView, SNode item, int index, double x, double y, double width, double height) {
                if(item == null) return;
                gfx.translate(x,y);
                if(item instanceof SelfDrawable) {
                    Bounds bounds = item.getBounds();
                    double scaleX = size/bounds.getWidth();
                    double scaleY = size/bounds.getHeight();
                    double scale = Math.min(scaleX,scaleY);
                    scale = Math.max(scale,0.2);  //don't scale down by more than a factor of 5
                    Bounds oldClip = gfx.getClipRect();
                    gfx.setClipRect(new Bounds(0,0,size,size));
                    gfx.scale(scale,scale);
                    gfx.translate(-bounds.getX(),-bounds.getY());
                    SelfDrawable sd = (SelfDrawable) item;
                    sd.draw(gfx);
                    gfx.translate(bounds.getX(),bounds.getY());
                    gfx.scale(1/scale,1/scale);
                    gfx.setClipRect(oldClip);
                }
                gfx.setPaint(FlatColor.BLACK);
                gfx.drawRect(0,0,width,height);
                if(listView.getSelectedIndex() == index) {
                    gfx.setPaint(new FlatColor(0.8,0.8,1.0,0.5));
                    gfx.fillRect(0,0,width,height);
                }
                gfx.translate(-x,-y);
            }
        });

        symbolPane = new ScrollPane();
        symbolPane.setContent(listView);
        symbolPane.setHorizontalVisiblePolicy(ScrollPane.VisiblePolicy.Never);

        symbolAddButton = new Button(getString("sidebar.insertSymbol"));
        symbolAddButton.onClicked(new Callback<ActionEvent>() { public void call(ActionEvent event) {
            if(listView.getSelectedIndex() < 0) return;
            SNode node = listView.getModel().get(listView.getSelectedIndex());
            SketchDocument sd = context.getDocument();
            sd.getCurrentPage().add(node.duplicate(null));
            context.redraw();
        }});


        EventBus.getSystem().addListener(listView, MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public double prevx;
            public boolean created;
            public SNode dupe;

            public void call(MouseEvent event) {
                if(event.getType() == MouseEvent.MousePressed) {
                    ContextMenu.hideAll();
                    if(event.getButton() == 3) { // check for right clicks to open the context menu
                        showContextMenu(event);
                        return;
                    }
                }
                if(event.getType() == MouseEvent.MouseDragged) {
                    if(created && dupe != null) {
                        Point2D pt = event.getPointInNodeCoords(context.getCanvas());
                        pt = context.getSketchCanvas().transformToCanvas(pt);
                        Bounds b = dupe.getBounds();
                        dupe.setTranslateX(pt.getX()-b.getWidth()/2);
                        dupe.setTranslateY(pt.getY()-b.getHeight()/2);
                        context.redraw();
                    }
                    if(event.getX() < 0 && prevx >= 0 && !created) {
                        created = true;
                        if(listView.getSelectedIndex() < 0) return;
                        SNode node = listView.getModel().get(listView.getSelectedIndex());
                        SketchDocument sd = context.getDocument();
                        dupe = node.duplicate(null);
                        Bounds b = dupe.getBounds();
                        sd.getCurrentPage().add(dupe);
                        Point2D pt = event.getPointInNodeCoords(context.getCanvas());
                        pt = context.getSketchCanvas().transformToCanvas(pt);
                        dupe.setTranslateX(pt.getX()-b.getWidth()/2);
                        dupe.setTranslateY(pt.getY()-b.getHeight()/2);
                        context.redraw();
                    }
                    prevx = event.getX();
                }
                if(event.getType() == MouseEvent.MouseReleased) {
                    if(created) {
                        context.getSelection().setSelectedNode(dupe);
                        dupe = null;
                        created = false;
                        prevx = 0;
                    }
                    context.redraw();
                }
            }
        });

        setSwitcher = new PopupMenuButton<SymbolManager.SymbolSet>();
        setSwitcher.setModel(new ListModel<SymbolManager.SymbolSet>(){
            public SymbolManager.SymbolSet get(int i) {
                if(symbolManager.getSetCount() < i-1) {
                    return null;
                }
                return symbolManager.getSet(i);
            }

            public int size() {
                return symbolManager.getSetCount();
            }
        });
        EventBus.getSystem().addListener(setSwitcher, SelectionEvent.Changed, new Callback<SelectionEvent>() {
            public void call(SelectionEvent event) {
                symbolManager.setCurrentSet(setSwitcher.getSelectedItem());
            }
        });

        this.add(setSwitcher);
        this.add(symbolPane);
        this.add(symbolAddButton);

        //this.setFill(new FlatColor(0x808080));
    }

    private void showContextMenu(MouseEvent event) {
        if(listView.getSelectedIndex()<0) return;
        if(listView.getModel().size() <= listView.getSelectedIndex()) return;
        
        final SNode shape = listView.getModel().get(listView.getSelectedIndex());
        contextMenu = new ContextMenu();
        contextMenu.addActions(
            new SAction(){
                @Override public String getDisplayName() { return "Delete"; }
                @Override
                public void execute() {
                    symbolManager.remove(shape);
                }
            },
            new SAction(){
                @Override public String getDisplayName() { return "Rename"; }
                @Override
                public void execute() {
                    u.p("renaming");
                    String name = shape.getStringProperty("symbolName");
                    if(name == null) {
                        name = shape.getClass().getSimpleName();
                    }
                    String result = StandardDialog.showEditText("Rename Symbol",name);
                    if(result != null) {
                        shape.setStringProperty(SYMBOL_NAME,result);
                        //symbolManager.save();
                    }
                }
            }
        );

        contextMenu.setWidth(100);
        contextMenu.setHeight(200);
        contextMenu.show(this,event.getX(),event.getY()+30);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        setSwitcher.doLayout();
        setSwitcher.setTranslateX(0);
        setSwitcher.setTranslateY(0);
        symbolPane.setTranslateX(0);
        double sy = setSwitcher.getVisualBounds().getY2();

        symbolAddButton.doLayout();
        symbolAddButton.setTranslateX(0);
        symbolAddButton.setTranslateY(getHeight()-symbolAddButton.getHeight());
        double bh = symbolAddButton.getHeight();
        

        symbolPane.setTranslateX(0);
        symbolPane.setTranslateY(sy);
        symbolPane.setHeight(getHeight()-sy-bh);
        symbolPane.setWidth(getWidth());
        symbolPane.doLayout();

    }
}
