package org.joshy.gfx.stage;

import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.Parent;

import java.awt.geom.Point2D;

public class EventPublisher {
    protected Parent parent;

    public EventPublisher(Parent parent) {
        this.parent = parent;
    }

    protected Node findTopNode(double x, double y) {
        return findTopNode(parent,x,y);
    }

    public static Node findTopNode(Parent parent, double x, double y) {
        if(parent instanceof Node) {
            x -= ((Node)parent).getTranslateX();
            y -= ((Node)parent).getTranslateY();
            if(!((Node)parent).isVisible()) return null;
        }

        for(Node node : parent.reverseChildren()) {
            if(node instanceof Parent) {
                Node pc = findTopNode(((Parent)node),x,y);
                if(pc != null) return pc;
            }
            if(node.getInputBounds().contains(x,y) && node.isVisible()) {
                return node;
            }
        }
        return null;
    }

    public static Point2D.Double convertSceneToNode(double x, double y, Node node) {
        if(node == null) return new Point2D.Double(x,y);
        Point2D.Double point = null;
        if(node.getParent() instanceof Node) {
            point = convertSceneToNode(x,y, (Node) node.getParent());
            point.x -= node.getTranslateX();
            point.y -= node.getTranslateY();
        } else {
            point = new Point2D.Double(x,y);
        }
        return point;
    }
}
