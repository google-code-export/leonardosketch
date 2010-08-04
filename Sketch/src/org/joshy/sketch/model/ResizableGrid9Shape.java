package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Bounds;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Jun 19, 2010
* Time: 8:08:13 PM
* To change this template use File | Settings | File Templates.
*/
public class ResizableGrid9Shape extends AbstractResizeableNode implements SelfDrawable {
    private double left = 20;
    private double right = 100-20;
    private double top = 20;
    private double bottom = 100-20;
    private List<SNode> subNodes;
    private double originalWidth;
    private double originalHeight;
    private boolean VLocked;
    private boolean HLocked;
    private boolean hasTextChild;
    private SText textChild;

    public ResizableGrid9Shape(double x, double y, double w, double h) {
        super(x, y, w, h);
        subNodes = new ArrayList<SNode>();
        originalWidth = w;
        originalHeight = h;
        right = originalWidth-20;
        bottom = originalHeight-20;
    }

    public ResizableGrid9Shape() {
        this(0,0,100,100);        
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(getTranslateX()+getX(),getTranslateY()+getY(),getWidth(),getHeight());
    }

    @Override
    public boolean contains(Point2D point) {
        return getBounds().contains(point);
    }

    public void add(SNode node) {
        if(node instanceof SText) {
            hasTextChild = true;
            textChild = (SText) node;
        }
        subNodes.add(node);
        node.setTranslateX(node.getTranslateX()-getTranslateX());
        node.setTranslateY(node.getTranslateY()-getTranslateY());
    }

    public void setNodes(List<SNode> nodes) {
        subNodes.clear();
        subNodes.addAll(nodes);
        for(SNode node : subNodes) {
            if(node instanceof SText) {
                hasTextChild = true;
                textChild = (SText) node;
            }
        }
    }

    public void draw(GFX g) {
        g.setPaint(FlatColor.BLACK);

        ControlPointAdapter adapter = null;
        for(SNode node: subNodes) {
            if(node instanceof SResizeableNode) {
                adapter = new SResizeableNodeAdapter();
            }
            if(node instanceof SText) {
                adapter = new TextAdapter();
            }
            if(node instanceof SPoly) {
                adapter = new SPolyAdapter();
            }
            if(node instanceof NGon) {
                adapter = new NgonAdapter();
            }
            if(node instanceof SPath) {
                adapter = new PathAdapter();
            }
            if(adapter != null) {
                List<Point2D> points = adapter.getControlPoints(node,originalWidth,originalHeight);
                List<Point2D> resizedPoints = adjustPoints(points);
                adapter.setControlPoints(node,resizedPoints);
                g.translate(node.getTranslateX(),node.getTranslateY());
                ((SelfDrawable)node).draw(g);
                g.translate(-node.getTranslateX(),-node.getTranslateY());
                adapter.setControlPoints(node,points);
            }
            adapter = null;
        }
    }


    private List<Point2D> adjustPoints(List<Point2D> points) {
        double x = getX();
        double x2 = getX()+getWidth();
        double y = getY();
        double y2 = getY()+getHeight();
        double rightin = originalWidth-right;
        double bottomin = originalHeight-bottom;

        List<Point2D> resizedPoints = new ArrayList<Point2D>();
        for(Point2D pt : points) {
            double tx = pt.getX();
            double ty = pt.getY();
            if(tx < left) {
                tx = x + tx;
            } else if(tx >= left && tx <= right) {
                double scale = (getWidth()-left-rightin)/(originalWidth-left-rightin);
                tx = x + (tx-left)*scale+left;
            } else if(tx > right) {
                tx = x2-(originalWidth-tx);
            }
            if(ty < top) {
                ty = y + ty;
            } else if(ty >= top && ty <= bottom) {
                double scale = (getHeight()-top-bottomin)/(originalHeight-top-bottomin);
                ty = y + (ty-top)*scale+top;
            } else if (ty > bottom) {
                ty = y2-(originalHeight-ty);
            }
            resizedPoints.add(new Point2D.Double(tx,ty));
        }
        return resizedPoints;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public void setRight(double right) {
        this.right = right;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public void setBottom(double bottom) {
        this.bottom = bottom;
    }

    public double getLeft() {
        return left;
    }

    public double getBottom() {
        return bottom;
    }

    public double getTop() {
        return top;
    }

    public double getRight() {
        return right;
    }

    public double getOriginalWidth() {
        return originalWidth;
    }

    public double getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalWidth(double originalWidth) {
        this.originalWidth = originalWidth;
    }

    public void setOriginalHeight(double originalHeight) {
        this.originalHeight = originalHeight;
    }

    @Override
    public void setWidth(double width) {
        if(HLocked) {
            super.setWidth(originalWidth);
        } else {
            super.setWidth(width);
        }
    }

    @Override
    public void setHeight(double height) {
        if(VLocked) {
            super.setHeight(originalHeight);
        } else {
            super.setHeight(height);
        }
    }

    public Iterable<SNode> getNodes() {
        return subNodes;
    }

    @Override
    public SNode duplicate(SNode dupe) {
        if(dupe == null) {
            dupe = new ResizableGrid9Shape();
        }
        ((ResizableGrid9Shape)dupe).setLeft(this.getLeft());
        ((ResizableGrid9Shape)dupe).setRight(this.getRight());
        ((ResizableGrid9Shape)dupe).setTop(this.getTop());
        ((ResizableGrid9Shape)dupe).setBottom(this.getBottom());
        ((ResizableGrid9Shape)dupe).setOriginalWidth(this.getOriginalWidth());
        ((ResizableGrid9Shape)dupe).setOriginalHeight(this.getOriginalHeight());
        ((ResizableGrid9Shape)dupe).setHLocked(this.isHLocked());
        ((ResizableGrid9Shape)dupe).setVLocked(this.isVLocked());

        List<SNode> subdupes = new ArrayList<SNode>();
        for(SNode nd : getNodes()) {
            subdupes.add(nd.duplicate(null));
        }
        ((ResizableGrid9Shape)dupe).setNodes(subdupes);
        return super.duplicate(dupe);
    }

    public void setVLocked(boolean VLocked) {
        this.VLocked = VLocked;
    }

    public void setHLocked(boolean HLocked) {
        this.HLocked = HLocked;
    }

    public boolean isHLocked() {
        return HLocked;
    }

    public boolean isVLocked() {
        return VLocked;
    }

    public boolean hasTextChild() {
        return hasTextChild;
    }

    public SText getTextChild() {
        return textChild;
    }

    interface ControlPointAdapter<T> {
        public List<Point2D> getControlPoints(T node, double originalWidth, double originalHeight);
        public void setControlPoints(T node, List<Point2D> points);
    }

    private class SResizeableNodeAdapter implements ControlPointAdapter<SResizeableNode> {

        public List<Point2D> getControlPoints(SResizeableNode node, double originalWidth, double originalHeight) {
            List<Point2D> points = new ArrayList<Point2D>();
            points.add(new Point2D.Double(node.getTranslateX()+node.getX(),node.getTranslateY()+node.getY()));
            points.add(new Point2D.Double(node.getTranslateX()+node.getX()+node.getWidth(),node.getTranslateY()+node.getY()+node.getHeight()));
            return points;
        }
        public void setControlPoints(SResizeableNode sn, List<Point2D> points) {
            sn.setTranslateX(points.get(0).getX());
            sn.setX(0);
            sn.setTranslateY(points.get(0).getY());
            sn.setY(0);
            sn.setWidth(points.get(1).getX()-points.get(0).getX());
            sn.setHeight(points.get(1).getY()-points.get(0).getY());
        }
    }

    private class SPolyAdapter implements ControlPointAdapter<SPoly> {
        public List<Point2D> getControlPoints(SPoly node, double originalWidth, double originalHeight) {
            List<Point2D> points = new ArrayList<Point2D>();
            for(Point2D pt : node.getPoints()) {
                points.add(new Point2D.Double(node.getTranslateX()+pt.getX(),node.getTranslateY()+pt.getY()));
            }
            return points;
        }

        public void setControlPoints(SPoly node, List<Point2D> points) {
            List<Point2D> pts = new ArrayList<Point2D>();
            for(Point2D pt : points) {
                pts.add(new Point2D.Double(pt.getX()-node.getTranslateX(),pt.getY()-node.getTranslateY()));
            }
            node.setPoints(pts);
        }
    }

    private class NgonAdapter implements ControlPointAdapter<NGon> {
        public List<Point2D> getControlPoints(NGon node, double originalWidth, double originalHeight) {
            List<Point2D> points = new ArrayList<Point2D>();
            points.add(new Point2D.Double(node.getTranslateX(),node.getTranslateY()));
            return points;
        }
        public void setControlPoints(NGon node, List<Point2D> points) {
            node.setTranslateX(points.get(0).getX());
            node.setTranslateY(points.get(0).getY());
        }
    }

    private class PathAdapter implements ControlPointAdapter<SPath> {

        public List<Point2D> getControlPoints(SPath node, double originalWidth, double originalHeight) {
            List<Point2D> points = new ArrayList<Point2D>();
            for(SPath.PathPoint pt : node.getPoints()) {
                points.add(np(node.getTranslateX()+pt.x,  node.getTranslateY()+pt.y));
                points.add(np(node.getTranslateX()+pt.cx1,node.getTranslateY()+pt.cy1));
                points.add(np(node.getTranslateX()+pt.cx2,node.getTranslateY()+pt.cy2));
            }
            return points;
        }

        public void setControlPoints(SPath node, List<Point2D> points) {
            int i = 0;
            int j = 0;
            node.setTranslateX(0);
            node.setTranslateY(0);
            while(true) {
                SPath.PathPoint pt = node.getPoints().get(i);
                pt.x   = points.get(j).getX();
                pt.y   = points.get(j).getY();
                j++;

                pt.cx1 = points.get(j).getX();
                pt.cy1 = points.get(j).getY();
                j++;

                pt.cx2 = points.get(j).getX();
                pt.cy2 = points.get(j).getY();
                j++;

                i++;
                if(i > node.getPoints().size()-1) break;
            }
        }
    }

    private class TextAdapter implements ControlPointAdapter<SText> {

        public List<Point2D> getControlPoints(SText node, double originalWidth, double originalHeight) {
//            Font font = Font.name("Arial")
//                    .weight(node.getWeight())
//                    .style(node.getStyle())
//                    .size((float)node.getFontSize())
//                    .resolve();
//            double w = font.calculateWidth(node.getText());
//            double h = font.calculateHeight(node.getText());
            List<Point2D> points = new ArrayList<Point2D>();
            points.add(new Point2D.Double(0,0));
            points.add(new Point2D.Double(originalWidth,originalHeight));
            return points;
        }

        public void setControlPoints(SText node, List<Point2D> points) {
//            Font font = Font.name("Arial")
//                    .weight(node.getWeight())
//                    .style(node.getStyle())
//                    .size((float)node.getFontSize())
//                    .resolve();
//            double w = font.calculateWidth(node.getText());
//            double h = font.calculateHeight(node.getText());
            node.setTranslateX(points.get(0).getX());
            node.setTranslateY(points.get(0).getY());
            node.setX(0);
            node.setY(0);
            node.setWidth(points.get(1).getX());
            node.setHeight(points.get(1).getY());
        }
    }
    
    private static Point2D np(double x, double y) {
        return new Point2D.Double(x,y);
    }

}
