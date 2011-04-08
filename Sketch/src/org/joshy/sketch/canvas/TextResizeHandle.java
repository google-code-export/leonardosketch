package org.joshy.sketch.canvas;

import org.joshy.sketch.model.SText;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 4/7/11
 * Time: 8:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class TextResizeHandle extends ResizeHandle {
    public TextResizeHandle(SText node, PositionHandle.Position position) {
        super(node,position);
    }

    @Override
    public void setX(double x, boolean constrain) {
        super.setX(x, constrain);    //To change body of overridden methods use File | Settings | File Templates.
        ((SText)this.getResizeableNode()).setAutoSize(false);
    }
}
