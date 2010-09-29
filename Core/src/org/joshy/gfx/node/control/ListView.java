package org.joshy.gfx.node.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.SkinManager;
import org.joshy.gfx.css.CSSMatcher;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.Bounds;

import java.util.List;

public class ListView<E> extends Control implements Focusable, ScrollPane.ScrollingAware, SelectableControl {

    public static int NO_SELECTION = -1;

    private ListModel<E> model;
    private ItemRenderer<E> renderer;
    private int selectedIndex = NO_SELECTION;
    private boolean focused;

    private double rowHeight = 20.0;
    private double colWidth = 100.0;
    private double scrollX = 0;
    private double scrollY = 0;
    private ScrollPane scrollPane;
    private Orientation orientation = Orientation.Vertical;
    private boolean dropIndicatorVisible;
    private int dropIndicatorIndex;
    private TextRenderer<E> textRenderer;
    private Font font;


    public ListView() {
        setWidth(200);
        setHeight(300);
        setRenderer(defaultItemRenderer);
        setTextRenderer(new TextRenderer<E>(){
            public String toString(ListView view, E item, int index) {
                if(item == null) return "null";
                return item.toString();
            }
        });

        setModel(new ListModel<E>() {
            public E get(int i) {
                return (E)("dummy item " + i);
            }
            public int size() {
                return 3;
            }
        });
        EventBus.getSystem().addListener(FocusEvent.All, new Callback<FocusEvent>(){
            public void call(FocusEvent event) {
                if(event.getType() == FocusEvent.Lost && event.getSource() == ListView.this) {
                    focused = false;
                    setDrawingDirty();
                }
                if(event.getType() == FocusEvent.Gained && event.getSource() == ListView.this) {
                    focused = true;
                    setDrawingDirty();
                }
            }
        });

        // click listener
        EventBus.getSystem().addListener(this, MouseEvent.MousePressed, new Callback<MouseEvent>(){
            public void call(MouseEvent event) {
                if(event.getType() == MouseEvent.MousePressed) {
                    int startRow = (int)(-scrollY/rowHeight);
                    int startCol = (int)(-scrollX/colWidth);
                    int index = 0;
                    int row = (int) (event.getY()/rowHeight+startRow);
                    int col = (int) (event.getX()/colWidth+startCol);
                    switch(orientation) {
                        case Vertical: index = (int) ((event.getY()/rowHeight)+startRow); break;
                        case Horizontal: index = (int) (event.getX()/colWidth+startCol); break;
                        case HorizontalWrap:
                            int rowLength = (int) (getWidth()/colWidth);
                            index = row * rowLength + col;
                            break;
                        case VerticalWrap:
                            int colLength = (int) (getHeight()/rowHeight);
                            index = col * colLength + row;
                            break;
                    }
                    setSelectedIndex(index);
                    setDrawingDirty();
                }
                Core.getShared().getFocusManager().setFocusedNode(ListView.this);
            }
        });
        
        //keyboard listener
        EventBus.getSystem().addListener(this, KeyEvent.KeyPressed, new Callback<KeyEvent>() {
            public void call(KeyEvent event) {
                //check for focus changes
                if(event.getKeyCode() == KeyEvent.KeyCode.KEY_TAB) {
                    if(event.isShiftPressed()) {
                        Core.getShared().getFocusManager().gotoPrevFocusableNode();
                    } else {
                        Core.getShared().getFocusManager().gotoNextFocusableNode();
                    }
                }
                
                //check for arrow keys
                switch(event.getKeyCode()) {
                    case KEY_LEFT_ARROW:
                    case KEY_RIGHT_ARROW:
                    case KEY_DOWN_ARROW:
                    case KEY_UP_ARROW:
                       handleArrowKeys(event);
                }
            }
        });


    }

    private void handleArrowKeys(KeyEvent event) {
        int index = 0;
        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_DOWN_ARROW) {
            switch (orientation) {
                case Vertical: 
                case VerticalWrap: index = getSelectedIndex()+1; break;
                case Horizontal: index = getSelectedIndex(); break;
                case HorizontalWrap: index = getSelectedIndex()+(int)(getWidth()/colWidth); break;
            }
        }
        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_UP_ARROW) {
            switch (orientation) {
                case Vertical:
                case VerticalWrap: index = getSelectedIndex()-1; break;
                case Horizontal: index = getSelectedIndex(); break;
                case HorizontalWrap: index = getSelectedIndex()-(int)(getWidth()/colWidth); break;
            }
        }
        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_LEFT_ARROW) {
            switch (orientation) {
                case Vertical: index = getSelectedIndex(); break;
                case VerticalWrap: index = getSelectedIndex()-(int)(getHeight()/rowHeight); break;
                case Horizontal:
                case HorizontalWrap: index = getSelectedIndex()-1; break;
            }
        }
        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_RIGHT_ARROW) {
            switch (orientation) {
                case Vertical: index = getSelectedIndex(); break;
                case VerticalWrap: index = getSelectedIndex()+(int)(getHeight()/rowHeight); break;
                case Horizontal:
                case HorizontalWrap: index = getSelectedIndex()+1;break;
            }
        }
        if(index >= 0 && index < getModel().size()) {
            setSelectedIndex(index);
        }
    }

    public void setRenderer(ItemRenderer<E> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doSkins() {
        cssSkin = SkinManager.getShared().getCSSSkin();
        font = cssSkin.getDefaultFont();
        setLayoutDirty();
    }


    @Override
    public void doLayout() {
    }

    @Override
    public void draw(GFX g) {
        if(getWidth() < 1) return;
        CSSMatcher matcher = new CSSMatcher("ListView");
        
        if(cssSkin != null) {
            cssSkin.drawBackground(g,matcher,"",new Bounds(0,0,width,height));
        } else {
            g.setPaint(FlatColor.WHITE);
            g.fillRect(0,0,width,height);
            g.setPaint(FlatColor.BLACK);
            g.drawRect(0,0,width,height);
        }

        Bounds oldClip = g.getClipRect();
        g.setClipRect(new Bounds(0,0,width,height));

        if(orientation == Orientation.Vertical) {
            double dy = scrollY - ((int)(scrollY/rowHeight))*rowHeight;
            int startRow = (int)(-scrollY/rowHeight);
            for(int i=0; i<model.size();i++) {
                if(i*rowHeight > getHeight() + rowHeight) break;
                E item = null;
                if(i+startRow < model.size()) {
                    try {
                        item = model.get(i+startRow);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        item = null;
                    }
                }
                renderer.draw(g, this, item, i+startRow, 0+1, i*rowHeight+1+dy, getWidth()-1, rowHeight);
            }
        }

        if(orientation == Orientation.Horizontal) {
            double dx = scrollX - ((int)(scrollX/colWidth))*colWidth;
            int startCol = (int)(-scrollX/colWidth);
            for(int i=0; i<model.size();i++) {
                if(i*colWidth > getWidth()+colWidth) break;
                E item = null;
                if(i+startCol < model.size()) {
                    item = model.get(i+startCol);
                }
                renderer.draw(g, this, item, i+startCol, i*colWidth+1+dx, 0+1, colWidth, getHeight()-1);
            }

            if(dropIndicatorVisible) {
                g.setPaint(new FlatColor(1.0,0,0,0.7));
                g.fillRect(dx+dropIndicatorIndex*colWidth-2,0,5,getHeight()-1);
            }

        }

        if(orientation == Orientation.HorizontalWrap || orientation == Orientation.VerticalWrap) {
            double dy = scrollY - ((int)(scrollY/rowHeight))*rowHeight;
            double dx = scrollX - ((int)(scrollX/colWidth))*colWidth;
            int startRow = (int)(-scrollY/rowHeight);
            int startCol = (int)(-scrollX/colWidth);
            for(int i=0; i<model.size();i++) {
                int x = 0;
                int y = 0;
                int ioff = 0;
                if(orientation == Orientation.HorizontalWrap) {
                    int rowLength = (int) (getWidth()/colWidth);
                    x = i%rowLength;
                    y = i/rowLength;
                    ioff = startRow*rowLength;
                }
                if(orientation == Orientation.VerticalWrap) {
                    int colLength = (int) (getHeight()/rowHeight);
                    x = i/colLength;
                    y = i%colLength;
                    ioff = startCol*colLength;
                }
                E item = null;
                if(i+ioff < model.size()) {
                    item = model.get(i+ioff);
                }
                renderer.draw(g, this, item, i,
                        x*colWidth+1+dx, y*rowHeight+1+dy,
                        colWidth, rowHeight);
            }

        }

        
        g.setClipRect(oldClip);
        if(cssSkin != null) {
            cssSkin.drawBorder(g,matcher,"",new Bounds(0,0,width,height));
        }

    }


    public ListModel<E> getModel() {
        return model;
    }
    
    public ListView setModel(ListModel<E> listModel) {
        this.model = listModel;
        EventBus.getSystem().addListener(model, ListEvent.Updated, new Callback<ListEvent>() {
            public void call(ListEvent event) {
                setLayoutDirty();
                setDrawingDirty();
            }
        });
        return this;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        if(selectedIndex < 0) return;
        if(selectedIndex >= model.size()) return;
        this.selectedIndex = selectedIndex;

        if(scrollPane != null) {
            Bounds bounds = null;
            int colLen = (int) (getHeight()/rowHeight);
            int rowLen = (int) (getWidth()/colWidth);
            switch(orientation) {
                case Vertical:       bounds = new Bounds(0,selectedIndex*getRowHeight(),getWidth(),getRowHeight()); break;
                case Horizontal:     bounds = new Bounds(selectedIndex*getColumnWidth(),0,getColumnWidth(),getHeight()); break;
                case HorizontalWrap: bounds = new Bounds((selectedIndex%rowLen * getColumnWidth()), selectedIndex/rowLen * getRowHeight(), getColumnWidth(),getRowHeight()); break;
                case VerticalWrap:   bounds = new Bounds((selectedIndex/colLen * getColumnWidth()), selectedIndex%rowLen * getRowHeight(), getColumnWidth(),getRowHeight()); break;
            }
            scrollPane.scrollToShow(bounds);
        }

        EventBus.getSystem().publish(new SelectionEvent(SelectionEvent.Changed,this));
        setDrawingDirty();
    }
    
    public boolean isFocused() {
        return focused;
    }

    /* ===== ScrollingAware Implementation ===== */
    public double getFullWidth(double width, double height) {
        switch (orientation) {
            case Vertical: return width;
            case VerticalWrap:
                int colLen = (int) (getHeight()/rowHeight);
                return Math.max(getModel().size()/colLen*colWidth,width);
            case Horizontal: return Math.max(getModel().size()*colWidth,width);
            case HorizontalWrap: return width;
        }
        return width;
    }

    public double getFullHeight(double width, double height) {
        switch (orientation) {
            case Vertical:   return Math.max(getModel().size()*rowHeight,height);
            case VerticalWrap: return height;
            case Horizontal: return height;
            case HorizontalWrap:
                if(getWidth() < 1) return height;
                int rowLen = (int) (getWidth()/colWidth);
                return Math.max(getModel().size()/rowLen*rowHeight,height);
        }
        return height;
    }

    public void setScrollX(double value) {
        this.scrollX = value;
    }

    public void setScrollY(double value) {
        this.scrollY = value;
    }

    public void setScrollParent(ScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }






    /* ============= =================== */

    public static ListModel createModel(final List list) {
        return new ListModel() {

            public Object get(int i) {
                if(i >= list.size() || i < 0) {
                    return null;
                }
                return list.get(i);
            }

            public int size() {
                return list.size();
            }
        };
    }

    private double getRowHeight() {
        return rowHeight;
    }

    private double getColumnWidth() {
        return colWidth;
    }


    public void setRowHeight(double rowHeight) {
        this.rowHeight = rowHeight;
    }

    
    public void setColumnWidth(double colWidth) {
        this.colWidth = colWidth;
    }


    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public void setDropIndicatorVisible(boolean dropIndicatorVisible) {
        this.dropIndicatorVisible = dropIndicatorVisible;
    }

    public void setDropIndicatorIndex(int dropIndicatorIndex) {
        this.dropIndicatorIndex = dropIndicatorIndex;
    }

    public ListView setTextRenderer(TextRenderer<E> textRenderer) {
        this.textRenderer = textRenderer;
        return this;
    }


    public static class ListEvent extends Event {
        public static final EventType Updated = new EventType("ListEventUpdated");
        public ListEvent(EventType type, ListModel model) {
            super(type);
            this.source = model;            
        }
    }

    public static <T> ListModel<T> createModel(final T ... strings) {
        return new ListModel<T>() {

            public T get(int i) {
                if(i >= strings.length) {
                    return null;
                }
                return strings[i];
            }

            public int size() {
                return strings.length;
            }
        };
    }


    public static interface ItemRenderer<E> {
        public void draw(GFX gfx, ListView listView, E item, int index, double x, double y, double width, double height);
    }

    public enum Orientation {
        Horizontal, HorizontalWrap, VerticalWrap, Vertical
    }

    public static interface TextRenderer<E> {
        public String toString(ListView view, E item, int index);
    }

    ItemRenderer defaultItemRenderer =  new ItemRenderer<E>() {
        public void draw(GFX gfx, ListView listView, E item, int index, double x, double y, double width, double height) {
            if(cssSkin != null) {
                CSSMatcher matcher = new CSSMatcher("ListView");
                Bounds bounds = new Bounds(x,y,width,height);
                String prefix = "item-";
                if(listView.getSelectedIndex() == index) {
                    prefix = "selected-item-";
                }
                cssSkin.drawBackground(gfx,matcher,prefix,bounds);
                cssSkin.drawBorder(gfx,matcher,prefix,bounds);
                int col = cssSkin.getCSSSet().findColorValue(matcher, prefix + "color");
                gfx.setPaint(new FlatColor(col));
                if(item != null) {
                    String s = textRenderer.toString(listView, item, index);
                    gfx.drawText(s, font, x+2, y+15);
                }
            } else {
                if(listView.getSelectedIndex() == index) {
                    gfx.setPaint(new FlatColor(0xff00ff));
                    gfx.fillRect(x,y,width,height);
                }
                gfx.setPaint(FlatColor.BLACK);
                if(item != null) {
                    String s = textRenderer.toString(listView, item, index);
                    gfx.drawText(s, font, x+2, y+15);
                }
            }
        }
    };

}
