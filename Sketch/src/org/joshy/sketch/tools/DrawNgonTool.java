package org.joshy.sketch.tools;

import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.control.Slider;
import org.joshy.gfx.node.layout.HBox;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.actions.UndoableAddNodeAction;
import org.joshy.sketch.model.NGon;
import org.joshy.sketch.model.SketchDocument;

import java.awt.geom.Point2D;

public class DrawNgonTool extends CanvasTool {
    private Point2D start;
    private NGon node;
    private HBox panel;
    private Control slider;
    private Label sliderLabel;
    private int nValue;

    public DrawNgonTool(VectorDocContext context) {
        super(context);
        panel = new HBox();
        panel.setSpacing(5);
        panel.add(new Label("sides"));
        slider = new Slider(false).setMin(3).setMax(20).setValue(5).setWidth(200);
        panel.add(slider);
        sliderLabel = new Label("N");
        panel.add(sliderLabel);
        EventBus.getSystem().addListener(slider, ChangedEvent.DoubleChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                nValue = ((Double)event.getValue()).intValue();
                sliderLabel.setText(""+nValue);
            }
        });
        panel.setTranslateX(100);
        panel.setTranslateY(20);
    }

    @Override
    public void call(KeyEvent event) {
    }


    public void enable() {
        super.enable();
        context.getCanvas().getParent().getStage().getPopupLayer().add(panel);
    }

    public void disable() {
        super.disable();
        context.getCanvas().getParent().getStage().getPopupLayer().remove(panel);
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
    }


    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        start = cursor;
        node = new NGon(nValue);
        node.setTranslateX(start.getX());
        node.setTranslateY(start.getY());
    }
    
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        double radius = start.distance(cursor);
        double angle = GeomUtil.calcAngle(start,cursor);
        if(event.isShiftPressed()) {
            angle = Math.toRadians(GeomUtil.snapTo45(angle));
        }
        angle = angle - Math.PI/2;
        node.setAngle(angle);
        node.setRadius(radius);
        context.redraw();
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        SketchDocument doc = (SketchDocument) context.getDocument();
        doc.getCurrentPage().add(node);
        context.getUndoManager().pushAction(new UndoableAddNodeAction(context,node,"Ngon"));
        context.getSelection().setSelectedNode(node);
        node = null;
        start = null;
        context.redraw();
        context.releaseControl();
    }

    public void drawOverlay(GFX g) {
        if(node != null) {
            g.scale(context.getSketchCanvas().getScale(),context.getSketchCanvas().getScale());
            g.translate(node.getTranslateX(),node.getTranslateY());
            node.draw(g);
            g.translate(-node.getTranslateX(),-node.getTranslateY());
            g.scale(1/context.getSketchCanvas().getScale(),1/context.getSketchCanvas().getScale());
        }
    }

}
