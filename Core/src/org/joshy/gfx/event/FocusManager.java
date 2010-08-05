package org.joshy.gfx.event;

import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.Parent;
import org.joshy.gfx.node.control.Focusable;
import org.joshy.gfx.util.u;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 26, 2010
 * Time: 9:56:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class FocusManager {
    private Focusable focusedNode;

    public Node findFocusedNode(Parent parent) {
        if(focusedNode != null) return (Node) focusedNode;
        for(Node node : parent.children()) {
            if(node instanceof Focusable) {
                focusedNode = (Focusable) node;
                return (Node) focusedNode;
            }
            if(node instanceof Parent) {
                findFocusedNode((Parent)node);
            }
        }
        return null;
    }

    public void setFocusedNode(Focusable focusableNode) {
//        u.p("switching focused node to " + focusableNode);
        EventBus.getSystem().publish(new FocusEvent(FocusEvent.Lost, focusedNode));
        focusedNode = focusableNode;
        EventBus.getSystem().publish(new FocusEvent(FocusEvent.Gained, focusedNode));
    }


    public Focusable getFocusedNode() {
        return focusedNode;
    }


    public void gotoPrevFocusableNode() {
        if(focusedNode != null) {
            Focusable prev = null;
            for(Node node : ((Node)focusedNode).getParent().children()) {
                if(node == focusedNode && prev != null) {
                    setFocusedNode(prev);
                    break;
                }
                if(node instanceof Focusable) {
                    prev = (Focusable) node;
                }
            }
        }
    }

    public void gotoNextFocusableNode() {
        if(focusedNode != null) {
            boolean found = false;
            for(Node node : ((Node)focusedNode).getParent().children()) {
                if(found && node instanceof Focusable) {
                    u.p("switched focus to node " + node);
                    setFocusedNode((Focusable) node);
                    break;
                }
                if(node == focusedNode) found = true;
            }
        }
    }
}
