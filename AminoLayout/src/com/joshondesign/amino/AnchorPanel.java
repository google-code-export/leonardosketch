package com.joshondesign.amino;

import java.util.HashMap;
import java.util.Map;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.Panel;

public class AnchorPanel extends Panel {

    private Map<Node, Constraint> constraints = new HashMap<Node, Constraint>();

    public static class Constraint {

        public boolean right = false;
        public boolean left = false;
        public double rightValue = 0;
        public double leftValue = 0;
        public boolean bottom = false;
        public double bottomValue;
    }

    public AnchorPanel() {
    }

    @Override
    public void doLayout() {
        super.doLayout();
        for(Control node : controlChildren()) {
            Constraint con = constraints.get(node);
            node.doLayout();
            if(con.right) {
                node.setTranslateX(getWidth()-node.getWidth()-con.rightValue);
            }
            if(con.bottom) {
                node.setTranslateY(getHeight()-node.getHeight()-con.bottomValue);
            }
        }
    }

    public Panel add(Node node, Constraint c) {
        constraints.put(node,c);
        this.add(node);
        return this;
    }
}
