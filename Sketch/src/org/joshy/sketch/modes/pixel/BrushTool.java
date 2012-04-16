package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Checkbox;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.layout.FlexBox;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.sketch.pixel.model.PixelGraphics;
import org.joshy.sketch.pixel.model.PixelLayer;

import java.awt.geom.Point2D;

/**
 * The basic pencil tool. For now it just fills in pixels one at a time
 * using the color black.
 */
public class BrushTool extends PixelTool {
    int radius = 9;
    private FlexBox panel;
    private Checkbox smooth;

    public BrushTool(PixelDocContext context) {
        super(context);
        panel = new HFlexBox().setBoxAlign(HFlexBox.Align.Baseline);
        panel.add(new Label("Brush"));
        panel.add(new Button("small").onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                radius = 5;
            }
        }));
        panel.add(new Button("medium").onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                radius = 11;
            }
        }));
        panel.add(new Button("large").onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                radius = 17;
            }
        }));
        smooth = new Checkbox("Smooth");
        panel.add(smooth);
    }

    @Override
    public void enable() {
        super.enable();
        NodeUtils.doSkins(panel);
        panel.doPrefLayout();
        panel.doLayout();
        panel.setFill(FlatColor.WHITE.deriveWithAlpha(0.7));
        getContext().getCanvas().getParent().getStage().getPopupLayer().add(panel);
        Point2D pt = NodeUtils.convertToScene(getContext().getCanvas(), 20, 20);
        panel.setTranslateX(pt.getX());
        panel.setTranslateY(pt.getY());
    }

    @Override
    public void disable() {
        super.disable();
        getContext().getCanvas().getParent().getStage().getPopupLayer().remove(panel);
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D cursor) {
        PixelLayer layer = getContext().getDocument().getCurrentLayer();
        PixelGraphics g = layer.getGraphics();
        g.setFill(getContext().getDocument().getForegroundColor());
        g.fillOval((int)cursor.getX()-radius/2,(int)cursor.getY()-radius/2,radius,radius,smooth.isSelected());
        getContext().getCanvas().redraw();
    }

    @Override
    protected void mouseDragged(MouseEvent event,  Point2D cursor) {
        PixelLayer layer = getContext().getDocument().getCurrentLayer();
        PixelGraphics g = layer.getGraphics();
        g.setFill(getContext().getDocument().getForegroundColor());
        g.fillOval((int)cursor.getX()-radius/2,(int)cursor.getY()-radius/2,radius,radius,smooth.isSelected());
        getContext().getCanvas().redraw();
    }

    @Override
    protected void mouseReleased(MouseEvent event,  Point2D cursor) {
        getContext().getCanvas().getScrollPane().doLayout();
    }

}
