package org.joshy.gfx.node.layout;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.Control;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 26, 2010
 * Time: 4:57:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class GridLineLayout extends Panel {
    private List<Position> positions;
    private List<Column> columns;
    private List<Row> rows;
    private static final boolean DEBUG = false;
    private double columnGutter = 10.0;
    private double sideGutter = 5.0;
    private double rowGutter = 10.0;

    public GridLineLayout() {
        positions = new ArrayList<Position>();
        columns = new ArrayList<Column>();
        rows = new ArrayList<Row>();
    }

    public void setColumn(int column, double width, ColumnAnchor anchor) {
        columns.add(new Column(column,width,anchor));
    }

    public void setRow(int row, RowAnchor anchor) {
        rows.add(new Row(row,anchor));
    }

    public void add(int column, int row, Node node, Resize resize) {
        super.add(node);
        positions.add(new Position(column,row,node,resize, VResize.NONE));
    }

    public void add(int column, int row, Node node, Resize resize, VResize vResize) {
        super.add(node);
        positions.add(new Position(column,row,node,resize,vResize));
    }


    public enum Resize {
        FILL, RIGHT, NO
    }

    private class Position {
        private int column;
        private int row;
        private Node node;
        private Resize resize;
        private VResize vresize;

        public Position(int column, int row, Node node, Resize resize, VResize vResize) {
            this.column = column;
            this.row = row;
            this.node = node;
            this.resize = resize;
            this.vresize = vResize;
        }
    }

    public enum ColumnAnchor {
        LEFT,
        RIGHT
    }

    private class Column {
        private int column;
        private double width;
        private ColumnAnchor anchor;
        private double x = 0;

        public Column(int column, double width, ColumnAnchor anchor) {
            this.column = column;
            this.width = width;
            this.anchor = anchor;
        }
    }

    public enum RowAnchor {
        BOTTOM, TOP
    }

    private class Row {
        private int row;
        private RowAnchor anchor;
        public double y;
        public double maxHeight;

        public Row(int row, RowAnchor anchor) {
            this.row = row;
            this.anchor = anchor;
        }
    }

    @Override
    public void draw(GFX g) {
        drawSelf(g);
        for(Node child : children) {
            g.translate(child.getTranslateX(),child.getTranslateY());
            child.draw(g);
            g.translate(-child.getTranslateX(),-child.getTranslateY());
        }
        if(DEBUG) {
            g.setPaint(new FlatColor(1.0,0,0,0.5));
            for(Column c : columns) {
                g.fillRect(c.x-2,0,4,getHeight());
            }
            for(Row r : rows) {
                g.fillRect(0, r.y-2, getWidth(), 4);
            }
        }
        this.drawingDirty = false;
    }


    @Override
    public void doLayout() {
        double x = sideGutter;
        //lay out the columns first
        for(Column c : columns) {
            if(c.anchor == ColumnAnchor.LEFT) {
                x+=c.width;
                c.x = x;
            }
            if(c.anchor == ColumnAnchor.RIGHT) {
                c.x = getWidth()- sideGutter -c.width;
            }
        }
        //lay out the children


        int maxRow = 0;
        for(Node node : children()) {
            if(node instanceof Control) {
                Control control = (Control) node;
                control.doLayout();
                Position position = getPosition(control);
                if(position.row > maxRow) maxRow = position.row;
//                u.p("laying out horiz child : " + control);
                Column c = columns.get(position.column);
                control.setTranslateX(c.x + columnGutter/2.0);
                if(position.column+1 < columns.size()) {
                    Column next = columns.get(position.column+1);
                    if(position.resize == Resize.RIGHT) {
                        control.setTranslateX(next.x - control.getWidth() - columnGutter/2.0);
                    }
                    if(position.resize == Resize.FILL) {
                        control.setWidth(next.x-c.x);
                    }
                }
            }
        }

        for(Node node : children()) {
            if(node instanceof Control) {
                Control control = (Control) node;
                Position position = getPosition(control);
                rows.get(position.row).maxHeight = Math.max(rows.get(position.row).maxHeight, control.getHeight()+rowGutter);
//                u.p("max height for row " + position.row + " is " + rows.get(position.row).maxHeight);
            }
        }

        double y = sideGutter;
        Row prev = null;
        for(Row r : rows) {
            if(r.anchor == RowAnchor.TOP) {
                r.y = y;
                y+= r.maxHeight;
            }
            if(r.anchor == RowAnchor.BOTTOM) {
                r.y = getHeight()-sideGutter-r.maxHeight;
                if(prev != null) {
                    prev.maxHeight = r.y - prev.y; 
                }
            }
//            u.p("row y = " + r.y);
            prev = r;
        }
        for(Node node : children()) {
            if(node instanceof Control) {
                Control control = (Control) node;
                Position position = getPosition(control);
                Row row = rows.get(position.row);
                control.setTranslateY(row.y);
                if(position.vresize == VResize.FILL) {
//                    u.p("sizing control " + control + " to " + row.maxHeight);
                    control.setHeight(row.maxHeight-rowGutter);
                }
//                u.p("laying out vert child : " + row.y + " " + control);
            }
        }
    }

    private Position getPosition(Control control) {
        for(Position p : positions) {
            if(p.node == control) {
                return p;
            }
        }
        return null;
    }

    public enum VResize {
        NONE, FILL
    }
}
