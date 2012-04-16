package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.sketch.modes.pixel.PixelDocContext;
import org.joshy.sketch.pixel.model.PixelDoc;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class PixelSetTool implements Callback<MouseEvent> {
    private Point2D start;
    double left = 0;
    private PixelDocContext context;
    private Point2D cursor;
    private boolean pressed;
    private Cursor emptyCursor;
    private boolean doingPanZoom;
    private Point2D panZoomStart;

    public PixelSetTool(PixelDocContext context) {
        this.context = context;
        EventBus.getSystem().addListener(context.getCanvas(), MouseEvent.MouseAll, this);
        BufferedImage cursorImage = new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);//ImageIO.read(Main.class.getResource("resources/pentool_cursor.png"));
        emptyCursor = java.awt.Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new java.awt.Point(8,8),"empty");
    }

    public void call(MouseEvent event) {
        cursor = context.getCanvas().transformToCanvas(event.getX(),event.getY());

        if(event.getType() == MouseEvent.MouseMoved) {
            context.redraw();
        }
        if(event.getType() == MouseEvent.MousePressed) {
            if(event.isCommandPressed()) {
                doingPanZoom = true;
                panZoomStart = event.getPointInNodeCoords(context.getCanvas());
                return;
            }

            pressed = true;
            hideCursor();
            start = cursor;//event.getPointInNodeCoords(context.getCanvas());
            //PixelDocument doc = (PixelDoc) context.getDocument();
            FlatColor color = context.getPixelToolbar().pixelColorPicker.getSelectedColor();
            //ndoc.setBrush(doc.createBrush(context.getPixelToolbar().brushWidthSlider.getValue(), color,
            //        context.getPixelToolbar().brushHardnessSlider.getValue(), true));
            context.getPixelToolbar().histogramColorPicker.addColor(color);
        }
        if(event.getType() == MouseEvent.MouseDragged) {
            if(doingPanZoom) {
                Point2D current = event.getPointInNodeCoords(context.getCanvas());
                Point2D diff = GeomUtil.subtract(current,panZoomStart);
                if(event.isShiftPressed()) {
                    double scale = 0.97;
                    double xoff = 5;
                    double yoff = 5;
                    if(diff.getY() < 0) {
                        scale = 1/scale;
                        xoff *= -1;
                        yoff *= -1;
                    }
//                    context.getCanvas().setPanX(context.getCanvas().getPanX()+xoff);
//                    context.getCanvas().setPanY(context.getCanvas().getPanY()+yoff);
                    context.getCanvas().setScale(context.getCanvas().getScale()*scale);
                } else {
//                    context.getCanvas().setPanX(context.getCanvas().getPanX()+diff.getX());
//                    context.getCanvas().setPanY(context.getCanvas().getPanY()+diff.getY());
                }
                panZoomStart = current;
                return;
            }
            Point2D end = cursor;
            PixelDoc doc = context.getDocument();
            //left = doc.stampBrush(start,end,left,context.getPixelToolbar().brushOpacitySlider.getValue());
            start = end;
            context.redraw();
        }
        if(event.getType() == MouseEvent.MouseReleased) {
            if(doingPanZoom) {
                doingPanZoom = false;
                return;
            }
            pressed = false;
            showCursor();
            left = 0;
        }
    }


    private void hideCursor() {
        Frame frame = (Frame) context.getStage().getNativeWindow();
        frame.setCursor(emptyCursor);
    }

    private void showCursor() {
        Frame frame = (Frame) context.getStage().getNativeWindow();
        frame.setCursor(Cursor.getDefaultCursor());
    }

    public void drawOverlay(GFX g) {
        if(cursor == null) return;
        g.setPaint(FlatColor.BLACK);
        if(pressed) {
            g.setPaint(new FlatColor(0,0,0,0.3));
        }
        double brushWidth = context.getPixelToolbar().brushWidthSlider.getValue()*2;
        g.drawOval(cursor.getX()-brushWidth/2,cursor.getY()-brushWidth/2,brushWidth,brushWidth);
    }
}
