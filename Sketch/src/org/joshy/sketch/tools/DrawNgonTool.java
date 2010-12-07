package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.control.Slider;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.sketch.actions.UndoableAddNodeAction;
import org.joshy.sketch.canvas.SketchCanvas;
import org.joshy.sketch.model.Handle;
import org.joshy.sketch.model.NGon;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.Point2D;

import static org.joshy.gfx.util.localization.Localization.getString;

public class DrawNgonTool extends CanvasTool {
    private Point2D start;
    private NGon node;
    private HFlexBox panel;
    private Control slider;
    private Label sliderLabel;
    private int nValue;
    private boolean editingExisting = false;
    private NGonSizeHandle sizeHandle;
    private boolean sizeHandleSelected;
    private boolean startedEditing;
    private boolean sizeHandleHovered;

    public DrawNgonTool(VectorDocContext context) {
        super(context);
        panel = new HFlexBox();
        panel.add(new Label(getString("drawNgonTool.sides")));
        slider = new Slider(false).setMin(3).setMax(20).setValue(5).setWidth(200);
        panel.add(slider);
        sliderLabel = new Label("N");
        panel.add(sliderLabel);
        EventBus.getSystem().addListener(slider, ChangedEvent.DoubleChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                nValue = ((Double)event.getValue()).intValue();
                sliderLabel.setText(""+nValue);
                if(node != null) {
                    node.setSides(nValue);
                }
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
        NodeUtils.doSkins(panel);
        panel.doPrefLayout();
        panel.doLayout();
        panel.setFill(FlatColor.BLACK.deriveWithAlpha(0.3));
        context.getCanvas().getParent().getStage().getPopupLayer().add(panel);
        Point2D pt = NodeUtils.convertToScene(context.getCanvas(), 20, 20);
        panel.setTranslateX(pt.getX());
        panel.setTranslateY(pt.getY());
    }

    public void disable() {
        super.disable();
        context.getCanvas().getParent().getStage().getPopupLayer().remove(panel);
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
        if(editingExisting) {
            if(sizeHandle != null) {
                sizeHandleHovered = sizeHandle.contains(cursor);
                context.redraw();
            }
        }
    }


    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        sizeHandleHovered = false;
        if(!editingExisting) {
            start = cursor;
            node = new NGon(nValue);
            node.setTranslateX(start.getX());
            node.setTranslateY(start.getY());
        } else {
            if(sizeHandle != null) {
                if(sizeHandle.contains(cursor)) {
                    sizeHandleSelected = true;
                }
            }
        }
    }
    
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {

        if(sizeHandle != null && sizeHandleSelected == true) {
            sizeHandle.setX(cursor.getX(),event.isShiftPressed());
            sizeHandle.setY(cursor.getY(),event.isShiftPressed());
            context.redraw();
            return;
        }
        if(!editingExisting) {
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
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        if(editingExisting) {
            if(sizeHandleSelected == false && startedEditing==false) {
                context.redraw();
                context.releaseControl();
                editingExisting = false;
            }
            sizeHandleSelected = false;
            startedEditing = false;
            return;
        } else {
            SketchDocument doc = (SketchDocument) context.getDocument();
            doc.getCurrentPage().add(node);
            context.getUndoManager().pushAction(new UndoableAddNodeAction(context,node,"Ngon"));
            context.getSelection().setSelectedNode(node);
            node = null;
            start = null;
            context.redraw();
            context.releaseControl();
            editingExisting = false;
        }
    }

    public void drawOverlay(GFX g) {
        if(node != null) {
            g.translate(context.getSketchCanvas().getPanX(),context.getSketchCanvas().getPanY());
            g.scale(context.getSketchCanvas().getScale(),context.getSketchCanvas().getScale());
            g.translate(node.getTranslateX(),node.getTranslateY());
            node.draw(g);
            g.translate(-node.getTranslateX(),-node.getTranslateY());
            g.scale(1/context.getSketchCanvas().getScale(),1/context.getSketchCanvas().getScale());
            g.translate(-context.getSketchCanvas().getPanX(),-context.getSketchCanvas().getPanY());
        }
        if(sizeHandle != null) {
            Point2D.Double center = context.getSketchCanvas().transformToDrawing(node.getTranslateX(),node.getTranslateY());
            g.setPaint(FlatColor.GRAY);
            g.fillOval(center.getX()-5,center.getY()-5,10,10);
            g.setPaint(FlatColor.BLACK);
            g.drawOval(center.getX()-5,center.getY()-5,10,10);
            Point2D.Double pt = context.getSketchCanvas().transformToDrawing(sizeHandle.getX(),sizeHandle.getY());
            g.setPaint(FlatColor.BLACK);
            g.drawLine(center.getX(),center.getY(),pt.getX(),pt.getY());

            FlatColor color = FlatColor.BLUE;
            if(sizeHandleHovered) {
                color = FlatColor.RED;
            }
            DrawUtils.drawStandardHandle(g,pt.getX(),pt.getY(),color);
        }
    }

    public void startEditing(NGon ngon) {
        editingExisting = true;
        this.node = ngon;
        sizeHandle = new NGonSizeHandle(node);
        startedEditing = true;
    }

    private class NGonSizeHandle extends Handle {
        private NGon node;
        private double x;
        private double y;

        public NGonSizeHandle(NGon node) {
            this.node = node;
            double angle = node.getAngle() + Math.PI/2;
            Point2D pt = GeomUtil.calcPoint(new Point2D.Double(node.getTranslateX(), node.getTranslateY()),
                    Math.toDegrees(angle), node.getRadius());
            this.x = pt.getX();
            this.y = pt.getY();
        }

        @Override
        public double getX() {
            return this.x;
        }

        @Override
        public void setX(double x, boolean constrain) {
            this.x = x;
            update(constrain);
        }

        private void update(boolean constrain) {
            Point2D center = new Point2D.Double(node.getTranslateX(),node.getTranslateY());
            double radius = center.distance(x,y);
            node.setRadius(radius);

            double angle = GeomUtil.calcAngle(center,new Point2D.Double(x,y));
            if(constrain) {
                angle = Math.toRadians(GeomUtil.snapTo45(angle));
            }
            angle = angle - Math.PI/2;
            node.setAngle(angle);
        }

        @Override
        public double getY() {
            return this.y;
        }

        @Override
        public void setY(double y, boolean constrain) {
            this.y = y;
            update(constrain);
        }

        @Override
        public void draw(GFX g, SketchCanvas sketchCanvas) {
        }
    }
}
