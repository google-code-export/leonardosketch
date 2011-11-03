package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.sketch.pixel.model.PixelGraphics;
import org.joshy.sketch.pixel.model.PixelLayer;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 11/2/11
 * Time: 3:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class LineTool extends PixelTool {
    private Point2D startPoint;
    private Point2D currentPoint;

    public LineTool(PixelDocContext context) {
        super(context);
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D cursor) {
        startPoint = cursor;
        getContext().getCanvas().redraw();
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D cursor) {
        currentPoint = cursor;
        getContext().getCanvas().redraw();
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D cursor) {
        PixelLayer layer = getContext().getDocument().getCurrentLayer();
        PixelGraphics g = layer.getGraphics();
        g.setFill(getContext().getDocument().getForegroundColor());
        drawPixelLine(g,
                (int)startPoint.getX()
                ,(int)startPoint.getY()
                ,(int)currentPoint.getX()
                ,(int)currentPoint.getY()
        );
        //g.fillOval((int)startPoint.getX(),(int)startPoint.getY(),10,10);
        getContext().getCanvas().redraw();
        currentPoint = null;
    }

    private static void drawPixelLine(Object g, int x0, int y0, int x1, int y1) {
            int dx = abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
            int dy = -abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
            int err = dx + dy, e2; /* error value e_xy */

            for (; ; ) {  /* loop */
                setPixel(g,x0, y0);
                if (x0 == x1 && y0 == y1) break;
                e2 = 2 * err;
                if (e2 >= dy) {
                    err += dy;
                    x0 += sx;
                } /* e_xy+e_x > 0 */
                if (e2 <= dx) {
                    err += dx;
                    y0 += sy;
                } /* e_xy+e_y < 0 */
            }
        }

    private static void setPixel(Object g, int x0, int y0) {
        if(g instanceof PixelGraphics) {
            ((PixelGraphics)g).fillPixel((int)x0,(int)y0);
        }
        if(g instanceof GFX) {
            ((GFX)g).drawLine(x0+0.5,y0+0.5,x0+0.5,y0+0.5);
        }
    }

    private static int abs(int i) {
        return Math.abs(i);
    }

    @Override
    public void drawOverlay(GFX gfx) {
        if(currentPoint != null) {
            gfx.setPaint(FlatColor.RED);
            drawPixelLine(gfx,
                    (int)startPoint.getX(),
                    (int)startPoint.getY(),
                    (int)currentPoint.getX(),
                    (int)currentPoint.getY()
            );
        }
    }
}
