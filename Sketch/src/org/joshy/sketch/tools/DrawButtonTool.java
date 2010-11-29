package org.joshy.sketch.tools;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.util.GraphicsUtil;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.model.Button9;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Apr 7, 2010
 * Time: 7:46:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class DrawButtonTool extends DrawTool {

    public DrawButtonTool(VectorDocContext context) {
        super(context);
    }

    @Override
    public void call(KeyEvent event) {
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        node = new Button9(event.getX(),event.getY(),100,50);
        node.setFillPaint(GraphicsUtil.randomColor());
        super.mousePressed(event, cursor);
    }
    
    public void drawOverlay(GFX g) {
        
    }
}
