package org.joshy.gfx.node.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.SkinManager;
import org.joshy.gfx.css.CSSMatcher;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.Bounds;

/** A table with columns and rows. Used for displaying tabular data.
 */
public class TableView extends Control implements Focusable, ScrollPane.ScrollingAware, SelectableControl {
    private TableModel model;
    private DataRenderer renderer;
    private int selectedRow = -1;
    private HeaderRenderer headerRenderer;
    private int selectedColumn = -1;
    private boolean focused;
    private double defaultColumnWidth = 50;
    private double scrollY = 0;
    private double scrollX = 0;
    final double rowHeight = 20;
    private ScrollPane scrollPane;
    private Font font;

    public TableView() {
        setWidth(300);
        setHeight(200);

        //set default model
        setModel(new TableModel() {
            public int getRowCount() {
                return 20;
            }

            public int getColumnCount() {
                return 3;
            }

            public Object getColumnHeader(int column) {
                return "Column " + column;
            }

            public Object get(int row, int column) {
                return "Data " + row + "," + column;
            }
        });

        //set default renderer
        setRenderer(new DataRenderer() {
            public void draw(GFX g, TableView table, Object cell, int row, int column, double x, double y, double width, double height) {
                if(cssSkin != null) {
                    CSSMatcher matcher = new CSSMatcher("TableView");
                    Bounds bounds = new Bounds(x,y,width,height);
                    String prefix = "item-";
                    if(getSelectedIndex() == row) {
                        prefix = "selected-item-";
                    }
                    cssSkin.drawBackground(g,matcher,prefix,bounds);
                    cssSkin.drawBorder(g,matcher,prefix,bounds);
                    int col = cssSkin.getCSSSet().findColorValue(matcher, prefix + "color");
                    g.setPaint(new FlatColor(col));
                    if(cell != null) {
                        //String s = textRenderer.toString(listView, item, index);
                        String s = cell.toString();
                        //g.drawText(s, font, x+2, y+15);
                        Font.drawCenteredVertically(g, s, font, x+2, y, width, height, true);
                    }
                    return;
                }

                g.setPaint(FlatColor.WHITE);
                if(row % 2 == 0) {
                    g.setPaint(new FlatColor("#eeeeee"));
                }
                if(row == table.getSelectedRow()) {
                    if(table.focused) {
                        g.setPaint(new FlatColor("#ddddff"));
                    } else {
                        g.setPaint(new FlatColor("#dddddd"));
                    }
                }
                g.fillRect(x,y,width,height);
                g.setPaint(FlatColor.BLACK);
                if(cell != null) {
                    Font.drawCenteredVertically(g, cell.toString(), Font.DEFAULT, x+2, y, width, height, true);
                }
                g.setPaint(new FlatColor("#d0d0d0"));
                g.drawLine(x+width-1,y, x+width-1,y+height);
            }
        });

        setHeaderRenderer(new HeaderRenderer() {
            public void draw(GFX g, TableView table, Object header, int column, double x, double y, double width, double height) {
                g.setPaint(new FlatColor("#d0d0d0"));
                if(column == table.getSelectedColumn()) {
                    g.setPaint(new FlatColor("#ddddff"));
                }
                g.fillRect(x,y,width,height);
                g.setPaint(FlatColor.BLACK);
                if(header != null) {
                    Font.drawCenteredVertically(g, header.toString(), Font.DEFAULT, x+2, y, width, height, true);
                }
            }
        });

        // click listener
        EventBus.getSystem().addListener(this, MouseEvent.MousePressed, new Callback<MouseEvent>(){
            public void call(MouseEvent event) {
                if(event.getType() == MouseEvent.MousePressed) {
                    if(event.getY() < 20) {
                        setSelectedColumn((int)event.getX()/ (int)(getWidth()/model.getColumnCount()));
                    } else {
                        int startRow = (int)(-scrollY/rowHeight);
                        setSelectedRow((int)((event.getY()-20)/20)+startRow);
                    }
                }
                Core.getShared().getFocusManager().setFocusedNode(TableView.this);
            }
        });

        EventBus.getSystem().addListener(FocusEvent.All, new Callback<FocusEvent>(){
            public void call(FocusEvent event) {
                if(event.getType() == FocusEvent.Lost && event.getSource() == TableView.this) {
                    focused = false;
                    setDrawingDirty();
                }
                if(event.getType() == FocusEvent.Gained && event.getSource() == TableView.this) {
                    focused = true;
                    setDrawingDirty();
                }
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
                //check for arrow up and down
                if(event.getKeyCode() == KeyEvent.KeyCode.KEY_DOWN_ARROW) {
                    int index = getSelectedRow()+1;
                    if(index < getModel().getRowCount()) {
                        setSelectedRow(index);
                    }
                }
                if(event.getKeyCode() == KeyEvent.KeyCode.KEY_UP_ARROW) {
                    int index = getSelectedRow()-1;
                    if(index >= 0) {
                        setSelectedRow(index);
                    }
                }
            }
        });

        setDefaultColumnWidth(50);
    }

    private void setSelectedColumn(int selectedColumn) {
        //u.p("selected createColumn set to " + selectedColumn);
        this.selectedColumn = selectedColumn;
        setDrawingDirty();
    }

    public int getSelectedColumn() {
        return selectedColumn;
    }

    private void setHeaderRenderer(HeaderRenderer headerRenderer) {
        this.headerRenderer = headerRenderer;
    }

    private void setSelectedRow(int row) {
        if(row >= 0 && row < getModel().getRowCount()) {
            selectedRow = row;
        } else {
            selectedRow = -1;
        }
        if(scrollPane != null) {
            Bounds bounds = new Bounds(0,
                    getSelectedRow()*rowHeight,
                    getWidth(),
                    rowHeight+headerHeight);
            scrollPane.scrollToShow(bounds);
        }
        EventBus.getSystem().publish(new SelectionEvent(SelectionEvent.Changed,this));
        setDrawingDirty();        
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    private TableModel getModel() {
        return model;
    }

    public void setRenderer(DataRenderer renderer) {
        this.renderer = renderer;
    }

    public void setModel(TableModel model) {
        this.model = model;
    }


    @Override
    public void doSkins() {
        cssSkin = SkinManager.getShared().getCSSSkin();
        font = cssSkin.getDefaultFont();
        setLayoutDirty();
    }


    @Override
    public void doLayout() {
        //do nothing. width and height are purely controlled by the container
    }

    final int headerHeight = 20;
    @Override
    public void draw(GFX g) {
        CSSMatcher matcher = new CSSMatcher(this);

        Bounds clip = g.getClipRect();
        g.setClipRect(new Bounds(0,0,width,height));

        //draw bg
        if(cssSkin != null) {
            cssSkin.drawBackground(g,matcher,"",new Bounds(0,0,width,height));
        } else {
            g.setPaint(FlatColor.WHITE);
            g.fillRect(0,0,width,height);
        }

        //draw headers
        double columnWidth = getWidth()/model.getColumnCount();
        for(int col = 0; col<model.getColumnCount(); col++) {
            Object header = model.getColumnHeader(col);
            headerRenderer.draw(g, this, header, col, col*columnWidth+scrollX, 0, columnWidth, headerHeight);
        }

        //draw data
        int startRow = (int)(-scrollY/rowHeight);
        for(int row=0; row*rowHeight+headerHeight < getHeight(); row++) {
            for(int col=0; col<model.getColumnCount(); col++) {
                Object item = null;
                if(row+startRow < model.getRowCount()) {
                    item = model.get(row+startRow,col);
                }
                renderer.draw(g, this, item,
                        row+startRow, col,
                        (int)(col*columnWidth+scrollX),
                        (int)(row*20+1+20),
                        (int)columnWidth, rowHeight);
            }
        }

        //draw border
        g.setClipRect(clip);
        if(cssSkin != null) {
            cssSkin.drawBorder(g,matcher,"",new Bounds(0,0,width,height));
        }
    }

    public boolean isFocused() {
        return focused;
    }

    public void setDefaultColumnWidth(double defaultColumnWidth) {
        this.defaultColumnWidth = defaultColumnWidth;
        setWidth(getModel().getColumnCount()*defaultColumnWidth);
    }

    public double getFullWidth(double width, double height) {
        return getWidth();
    }

    public double getFullHeight(double width, double height) {
        return Math.max(getModel().getRowCount()*rowHeight,height);
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

    public int getSelectedIndex() {
        return getSelectedRow();
    }


    public static interface TableModel<D,H> {
        public int getRowCount();
        public int getColumnCount();
        public H getColumnHeader(int column);

        /**
         * Return the data item at the specified row and createColumn. May return null.
         * @param row
         * @param column
         * @return the data at the specified row and createColumn. May be null.
         */
        public D get(int row, int column);
    }

    public static interface HeaderRenderer<H> {
        public void draw(GFX g, TableView table, H header, int column, double x, double y, double width, double height);
    }
    
    public static interface DataRenderer<D> {
        /**
         *
         * @param g graphics context
         * @param table the table view
         * @param cellData the cell data, may be null
         * @param row
         * @param column
         * @param x
         * @param y
         * @param width
         * @param height
         */
        public void draw(GFX g, TableView table, D cellData, int row, int column, double x, double y, double width, double height);
    }
}
