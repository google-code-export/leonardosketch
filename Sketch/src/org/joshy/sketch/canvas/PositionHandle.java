package org.joshy.sketch.canvas;

import org.joshy.sketch.model.Handle;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 18, 2010
 * Time: 5:13:19 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PositionHandle extends Handle {
    public enum Position {
        TopRight, BottomLeft, BottomRight, TopLeft, Right, Left, Bottom, Top
    }

    public Position getPosition() {
        return position;
    }

    protected Position position;

    public PositionHandle(Position position) {
        this.position = position;
    }
}
