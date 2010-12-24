package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.event.ScrollEvent;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 30, 2010
 * Time: 9:15:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class IncrementalRotateTool extends CanvasTool {
    public IncrementalRotateTool(VectorDocContext context) {
        super(context);
    }

    @Override
    public void drawOverlay(GFX g) {
        g.setPaint(FlatColor.BLACK);
        g.drawText("ROTATE", Font.DEFAULT,100,100);
    }

    @Override
    protected void call(KeyEvent event) {
    }

    @Override
    protected void call(ScrollEvent event) {
        SNode node = context.getSelection().firstItem();
        double scale = 2.0;
        node.setRotate(node.getRotate()+event.getAmount()*scale);
        context.redraw();
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        context.releaseControl();
        context.redraw();
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
    }
}
