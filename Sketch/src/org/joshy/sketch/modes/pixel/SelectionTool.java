package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.sketch.pixel.model.PixelSelection;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Let you create a rectangular selection which can then
 * be operated on.
 */
public class SelectionTool extends PixelTool {
    private Point2D startPoint;
    private Point2D currentPoint;

    public SelectionTool(PixelDocContext pixelDocContext) {
        super(pixelDocContext);
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D cursor) {
        startPoint = event.getPointInNodeCoords(getContext().getCanvas());
        getContext().getCanvas().redraw();
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D cursor) {
        currentPoint = event.getPointInNodeCoords(getContext().getCanvas());
        getContext().getCanvas().redraw();
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D cursor) {
        PixelSelection selection = getContext().getCanvas().getSelection();
        selection.clear();
        selection.add(new Rectangle(
                (int)startPoint.getX(),
                (int)startPoint.getY(),
                (int)(currentPoint.getX()-startPoint.getX()),
                (int)(currentPoint.getY()-startPoint.getY())
                ));
        getContext().getCanvas().redraw();
        startPoint = null;
        currentPoint = null;
    }

    @Override
    public void drawOverlay(GFX gfx) {
        if(currentPoint != null) {
            gfx.setPaint(FlatColor.RED);
            gfx.drawRect(
                    startPoint.getX(),
                    startPoint.getY(),
                    currentPoint.getX()-startPoint.getX(),
                    currentPoint.getY()-startPoint.getY()
                    );
        }
    }
}
