package org.joshy.gfx.node.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.skin.FontSkin;
import org.joshy.gfx.util.u;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 31, 2010
 * Time: 1:33:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class TableView extends Control implements Focusable, ScrollPane.ScrollingAware {
    private TableModel model;
    private DataRenderer renderer;
    private int selectedRow = -1;
    private HeaderRenderer headerRenderer;
    private int selectedColumn = -1;
    private boolean focused;
    private double defaultColumnWidth = 50;
    private double scrollY = 0;
    private double scrollX = 0;

    public TableView() {
        setWidth(300);
        setHeight(200);

        //set default model
        setModel(new TableModel() {
            public int getRowCount() {
                return 10;
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
                    Font.drawCenteredVertically(g, cell.toString(), FontSkin.DEFAULT.getFont(), x+2, y, width, height, true);
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
                    Font.drawCenteredVertically(g, header.toString(), FontSkin.DEFAULT.getFont(), x+2, y, width, height, true);
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
    }

    private void setSelectedColumn(int selectedColumn) {
        u.p("selected column set to " + selectedColumn);
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
    }

    @Override
    public void doLayout() {
        setWidth(getModel().getColumnCount()*defaultColumnWidth);
    }
    final double rowHeight = 20;

    @Override
    public void draw(GFX g) {
        Bounds clip = g.getClipRect();
        g.setClipRect(new Bounds(0,0,width,height));
        //draw bg
        g.setPaint(FlatColor.WHITE);
        g.fillRect(0,0,width,height);

        double columnWidth = defaultColumnWidth;

        //draw headers
        for(int col = 0; col<model.getColumnCount(); col++) {
            Object header = model.getColumnHeader(col);
            headerRenderer.draw(g, this, header, col, col*columnWidth+scrollX, 0, columnWidth, 20);
        }

        int startRow = (int)(-scrollY/rowHeight);
        //draw data
        for(int row=0; row*rowHeight+20 < getHeight(); row++) {
            for(int col=0; col<model.getColumnCount(); col++) {
                Object item = null;
                if(row+startRow < model.getRowCount()) {
                    item = model.get(row+startRow,col);
                }
                renderer.draw(g, this, item, row+startRow, col, col*columnWidth+scrollX, row*20+1+20, columnWidth, 20);
            }
        }

        //draw border
        g.setPaint(FlatColor.BLACK);
        g.drawRect(0,0,width,height);
        g.setClipRect(clip);
    }

    public boolean isFocused() {
        return focused;
    }

    public void setDefaultColumnWidth(double defaultColumnWidth) {
        this.defaultColumnWidth = defaultColumnWidth;
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
        
    }


    public static interface TableModel<D,H> {
        public int getRowCount();
        public int getColumnCount();
        public H getColumnHeader(int column);

        /**
         * Return the data item at the specified row and column. May return null. 
         * @param row
         * @param column
         * @return the data at the specified row and column. May be null.
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
