package org.joshy.gfx.node;

import java.awt.geom.Point2D;

public class NodeUtils {
    public static Point2D convertToScene(Node node, double x, double y) {
        x+= node.getTranslateX();
        y+= node.getTranslateY();
        if(node.getParent() instanceof Node) {
            return convertToScene((Node) node.getParent(),x,y);
        }
        return new Point2D.Double(x,y);
    }

    public static Point2D convertFromScene(Node node, Point2D point2D) {
        double x = point2D.getX();
        double y=  point2D.getY();
        x -= node.getTranslateX();
        y -= node.getTranslateY();
        if(node.getParent() instanceof Node) {
            return convertFromScene((Node)node.getParent(),new Point2D.Double(x,y));
        }
        return new Point2D.Double(x,y);
    }
}
