package org.joshy.sketch.actions.pages;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Transform;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.event.SelectionEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.ListModel;
import org.joshy.gfx.node.control.ListView;
import org.joshy.gfx.node.control.ScrollPane;
import org.joshy.gfx.node.layout.StackPanel;
import org.joshy.gfx.util.u;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;

/** A scrolling list of mini-views of the pages in the doc.
 * Lets you jump from page to page.
 */
public class PageListPanel extends StackPanel {
    private ScrollPane scroll;
    public ListView<SketchDocument.SketchPage> listview;
    private SketchDocument.SketchPage dragItem;
    private double dragX;
    private int dragIndex;
    private boolean doDuplicate;
    private double dragY;
    private VectorDocContext context;

    public PageListPanel(final VectorDocContext context) {
        this.context = context;
        scroll = new ScrollPane();
        scroll.setHorizontalVisiblePolicy(ScrollPane.VisiblePolicy.WhenNeeded);
        scroll.setVerticalVisiblePolicy(ScrollPane.VisiblePolicy.Never);
        listview = new ListView<SketchDocument.SketchPage>();
        listview.setModel(new ListModel<SketchDocument.SketchPage>(){
            public SketchDocument.SketchPage get(int i) {
                return context.getDocument().getPages().get(i);
            }
            public int size() {
                return context.getDocument().getPages().size();
            }
        });
        listview.setRenderer(new ListView.ItemRenderer<SketchDocument.SketchPage>(){
            public void draw(GFX gfx, ListView listView, SketchDocument.SketchPage item, int index, double x, double y, double width, double height) {
                gfx.setPaint(context.getDocument().getBackgroundFill());
                gfx.fillRect(x, y, width, height);
                Bounds oldClip = gfx.getClipRect();
                gfx.setClipRect(new Bounds(x,y,width, height));
                if(item != null) {
                    gfx.translate(x,y);
                    double w = context.getDocument().getWidth();
                    double h = context.getDocument().getHeight();
                    double s = 100.0/w;
                    for(SNode node : item.getNodes()) {
                        gfx.scale(s,s);
                        PageListPanel.this.draw(gfx,node);
                        gfx.scale(1/s,1/s);
                    }
                    gfx.translate(-x,-y);
                }
                if(context.getDocument().getCurrentPage() == item) {
                    gfx.setPaint(new FlatColor(0,0,0,0.1));
                    gfx.fillRect(x,y,width,height);
                    gfx.setPaint(FlatColor.GRAY);
                    gfx.drawRect(x,y,width-1,height-1);
                } else {
                    gfx.setPaint(FlatColor.BLACK);
                    gfx.drawRect(x,y,width,height);
                }
                gfx.setClipRect(oldClip);
            }
        });
        listview.setOrientation(ListView.Orientation.Horizontal);
        scroll.setContent(listview);
        this.add(scroll);

        EventBus.getSystem().addListener(listview, SelectionEvent.Changed, new Callback<SelectionEvent>() {
            public void call(SelectionEvent event) {
                int n = event.getView().getSelectedIndex();
                context.getDocument().setCurrentPage(n);
                context.getSelection().clear();
            }
        });
        EventBus.getSystem().addListener(listview, MouseEvent.MouseAll, new Callback<MouseEvent>() {
            public double start;

            public void call(MouseEvent event) {
                if(event.getType() == MouseEvent.MousePressed) {
                    start = event.getX();
                }
                if(event.getType() == MouseEvent.MouseDragged) {
                    doDuplicate = event.isAltPressed();
                    dragX = event.getX();
                    dragY = event.getY();
                    if(dragItem != null) {
                        dragX = event.getX();
                        dragIndex = (int)(dragX/100);
                        listview.setDropIndicatorVisible(true);
                        listview.setDropIndicatorIndex(dragIndex);
                        u.p("drag index = " + dragIndex);
                        setDrawingDirty();
                        return;
                    }
                    double diff = event.getX()-start;
                    if(Math.abs(diff) > 10){
                        u.p("drag threshold!");
                        dragItem = listview.getModel().get(listview.getSelectedIndex());
                        setDrawingDirty();
                        return;
                    }
                }
                if(event.getType() == MouseEvent.MouseReleased) {
                    listview.setDropIndicatorVisible(false);
                    if(dragItem != null) {
                        if(doDuplicate) {
                            duplicateItem(dragItem,dragIndex);
                        } else {
                            moveItem(dragItem,dragIndex);
                        }
                    }
                    dragItem = null;
                    doDuplicate = false;
                }
            }
        });

        EventBus.getSystem().addListener(CanvasDocument.DocumentEvent.ViewDirty, new Callback<CanvasDocument.DocumentEvent>() {
            public void call(CanvasDocument.DocumentEvent event) {
                setDrawingDirty();
            }
        });
    }

    private void duplicateItem(SketchDocument.SketchPage dragItem, int dragIndex) {
        SketchDocument doc = context.getDocument();
        SketchDocument.SketchPage dupe = doc.duplicate(dragItem);
        if(dragIndex >= doc.getPages().size()) {
            doc.insertPage(doc.getPages().size(),dupe);
            setDrawingDirty();
            return;
        }
        if(dragIndex <= 0) {
            doc.insertPage(0,dupe);
            setDrawingDirty();
            return;
        }
        SketchDocument.SketchPage newPageSpot = doc.getPages().get(dragIndex);
        int newIndex = doc.getPages().indexOf(newPageSpot);
        doc.insertPage(newIndex,dragItem);
        setDrawingDirty();
        return;
    }

    private void moveItem(SketchDocument.SketchPage dragItem, int dragIndex) {
        SketchDocument doc = context.getDocument();
        if(dragIndex >= doc.getPages().size()) {
            doc.removePage(dragItem);
            doc.insertPage(doc.getPages().size(),dragItem);
            setDrawingDirty();
            return;
        }
        if(dragIndex <=0) {
            doc.removePage(dragItem);
            doc.insertPage(0,dragItem);
            setDrawingDirty();
            return;
        }

        SketchDocument.SketchPage newPageSpot = doc.getPages().get(dragIndex);
        doc.removePage(dragItem);


        if(dragIndex >= doc.getPages().size()) {
            doc.insertPage(doc.getPages().size()-1,dragItem);
            setDrawingDirty();
            return;
        }

        int newIndex = doc.getPages().indexOf(newPageSpot);
        doc.insertPage(newIndex,dragItem);
        setDrawingDirty();
    }

    @Override
    public void draw(GFX g) {
        super.draw(g);
        if(dragItem != null) {
            g.setPaint(FlatColor.BLUE);
            g.drawRect(dragX,0,100,100);
            g.setPaint(new FlatColor(0,0,1.0,0.3));
            g.fillRect(dragX,0,100,100);
        }
        if(doDuplicate) {
            g.setPaint(FlatColor.PURPLE);
            g.fillRect(dragX-10,dragY-5,20,10);
            g.fillRect(dragX-5,dragY-10,10,20);
        }
    }

    private void draw(GFX g, SNode node) {
        g.translate(node.getTranslateX(),node.getTranslateY());
        g.scale(node.getScaleX(),node.getScaleY());
        g.rotate(node.getRotate(), Transform.Z_AXIS);
        if(node instanceof SelfDrawable) {
            ((SelfDrawable)node).draw(g);
        }
        if(node instanceof Button9) {
            draw(g,(Button9)node);
        }
        g.rotate(-node.getRotate(), Transform.Z_AXIS);
        g.scale(1/node.getScaleX(),1/node.getScaleY());
        g.translate(-node.getTranslateX(),-node.getTranslateY());
    }
}
