package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.model.SRect;

import java.awt.geom.Point2D;

/**
 * Draw a new rect as a resizable node
 */
public class DrawRectTool extends DrawTool {

    public DrawRectTool(VectorDocContext context) {
        super(context);
    }

    @Override
    public void call(KeyEvent event) {
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        node = new SRect(0,0,0.0,0.0);
        node.setTranslateX(cursor.getX());
        node.setTranslateY(cursor.getY());
        node.setFillPaint(FlatColor.GRAY);
        super.mousePressed(event, cursor);
    }


    public void drawOverlay(GFX g) {
    }
}
