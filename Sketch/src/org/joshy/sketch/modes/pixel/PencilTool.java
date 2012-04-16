package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.layout.FlexBox;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.sketch.pixel.model.PixelGraphics;
import org.joshy.sketch.pixel.model.PixelLayer;

import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 6/18/11
 * Time: 5:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class PencilTool extends PixelTool {
    private FlexBox panel;
    public PencilTool(PixelDocContext pixelDocContext) {
        super(pixelDocContext);
        panel = new HFlexBox().setBoxAlign(HFlexBox.Align.Baseline);
        panel.add(new Label("Pencil"));

    }

    @Override
    public void enable() {
        super.enable();    //To change body of overridden methods use File | Settings | File Templates.

        NodeUtils.doSkins(panel);
        panel.doPrefLayout();
        panel.doLayout();
        panel.setFill(FlatColor.BLACK.deriveWithAlpha(0.3));
        getContext().getCanvas().getParent().getStage().getPopupLayer().add(panel);
        Point2D pt = NodeUtils.convertToScene(getContext().getCanvas(), 20, 20);
        panel.setTranslateX(pt.getX());
        panel.setTranslateY(pt.getY());
    }

    @Override
    public void disable() {
        super.disable();    //To change body of overridden methods use File | Settings | File Templates.
        getContext().getCanvas().getParent().getStage().getPopupLayer().remove(panel);
    }

    @Override
    protected void mousePressed(MouseEvent event,  Point2D cursor) {
        PixelLayer layer = getContext().getDocument().getCurrentLayer();
        PixelGraphics g = layer.getGraphics();
        g.setFill(getContext().getDocument().getForegroundColor());
        g.fillPixel((int)cursor.getX(),(int)cursor.getY());
        getContext().getCanvas().redraw();
    }

    @Override
    protected void mouseDragged(MouseEvent event,  Point2D cursor) {
        PixelLayer layer = getContext().getDocument().getCurrentLayer();
        PixelGraphics g = layer.getGraphics();
        g.setFill(getContext().getDocument().getForegroundColor());
        g.fillPixel((int)cursor.getX(),(int)cursor.getY());
        getContext().getCanvas().redraw();
    }

    @Override
    protected void mouseReleased(MouseEvent event,  Point2D cursor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
