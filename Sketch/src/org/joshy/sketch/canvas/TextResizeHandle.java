package org.joshy.sketch.canvas;

import org.joshy.sketch.model.SText;

/**
 * Overrides the standard ResizeHandle for one purpose:
 * to disable autosizing whenever the user manually resizes the text bounds
 */
public class TextResizeHandle extends ResizeHandle {
    public TextResizeHandle(SText node, PositionHandle.Position position) {
        super(node,position);
    }

    @Override
    public void setX(double x, boolean constrain) {
        super.setX(x, constrain);
        ((SText)this.getResizeableNode()).setAutoSize(false);
    }

    @Override
    public void setXY(double x, double y, boolean constrain) {
        super.setXY(x, y, constrain);
        ((SText)this.getResizeableNode()).setAutoSize(false);
    }
}
