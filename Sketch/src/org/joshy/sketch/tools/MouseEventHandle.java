package org.joshy.sketch.tools;

import org.joshy.gfx.event.MouseEvent;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/15/11
 * Time: 1:49 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MouseEventHandle {
    public void mouseMoved(boolean hovered, MouseEvent event, Point2D.Double cursor);

    public void mouseDragged(double nx, double ny, boolean shiftPressed, Point2D.Double cursor);

    public void mousePressed(MouseEvent event, Point2D.Double cursor);

    public void mouseReleased(MouseEvent event, Point2D.Double cursor);
}
