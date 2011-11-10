package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.FlexBox;
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
    private FlexBox panel;
    private Slider slider;
    private Label sliderLabel;
    private int nValue;
    private boolean editingExisting = false;
    private NGonSizeHandle sizeHandle;
    private NGonSizeHandle starHandle;
    private boolean startedEditing;
    private boolean isStar;
    private Checkbox starCheckbox;

    public DrawNgonTool(final VectorDocContext context) {
        super(context);
        panel = new HFlexBox().setBoxAlign(HFlexBox.Align.Baseline);

        panel.add(new Label(getString("drawNgonTool.sides")));
        slider = new Slider(false);
        slider.setMin(3).setMax(20).setValue(5).setWidth(200);
        panel.add(slider);
        sliderLabel = new Label("N");
        panel.add(sliderLabel);
        starCheckbox =new Checkbox("Star");
        panel.add(starCheckbox.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                isStar = ((Button) actionEvent.getSource()).isSelected();
                if (node != null) {
                    node.setStar(isStar);
                }
                context.redraw();
            }
        }));
        
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
            sizeHandle.processMouseMove(event,cursor);
            starHandle.processMouseMove(event,cursor);
            context.redraw();
        }
    }


    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        if(editingExisting) {
            sizeHandle.processMousePressed(event,cursor);
            starHandle.processMousePressed(event,cursor);
        } else {
            start = cursor;
            node = new NGon(nValue);
            node.setRadius(0);
            node.setInnerRadius(0);
            node.setStar(isStar);
            node.setTranslateX(start.getX());
            node.setTranslateY(start.getY());
        }
    }
    
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        if(editingExisting) {
            sizeHandle.processMouseDragged(event,cursor);
            starHandle.processMouseDragged(event,cursor);
            return;
        }
        if(!editingExisting) {
            double radius = start.distance(cursor);
            double dangle = GeomUtil.calcAngle(start,cursor);
            double angle = 0;
            if(event.isShiftPressed()) {
                angle = Math.toRadians(GeomUtil.snapTo45(dangle));
            } else {
                angle = Math.toRadians(dangle);
            }
            angle = angle - Math.PI/2;
            node.setAngle(angle);
            node.setRadius(radius);
            node.setInnerRadius(radius/2);
            context.redraw();
        }
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        if(editingExisting) {
            boolean quit = false;
            if(!sizeHandle.selected && !starHandle.selected) {
                quit = true;
            }
            sizeHandle.selected = false;
            starHandle.selected = false;
            if(quit && startedEditing==false) {
                context.redraw();
                context.releaseControl();
                editingExisting = false;
                node = null;
                sizeHandle = null;
            }
            startedEditing = false;
            return;
        } else {
            SketchDocument doc = context.getDocument();
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
            Point2D.Double pt = context.getSketchCanvas().transformToDrawing(sizeHandle.getX(),sizeHandle.getY());

            g.setPaint(new FlatColor(0x404040));
            g.drawLine(center.getX(),center.getY(),pt.getX(),pt.getY());
            if(isStar) {
                pt = context.getSketchCanvas().transformToDrawing(starHandle.getX(),starHandle.getY());
                g.drawLine(center.getX(),center.getY(),pt.getX(),pt.getY());
            }

            g.setPaint(FlatColor.GRAY);
            g.fillOval(center.getX()-5,center.getY()-5,10,10);
            g.setPaint(FlatColor.BLACK);
            g.drawOval(center.getX()-5,center.getY()-5,10,10);

            sizeHandle.draw(g);
            if(isStar) {
                starHandle.draw(g);
            }
        }
    }

    public void startEditing(NGon ngon) {
        editingExisting = true;
        this.node = ngon;
        sizeHandle = new NGonSizeHandle(node,true);
        starHandle = new NGonSizeHandle(node,false);
        starCheckbox.setSelected(ngon.isStar());
        slider.setValue(ngon.getSides());
        startedEditing = true;
    }

    private class NGonSizeHandle extends Handle {
        private NGon node;
        private double x;
        private double y;
        private boolean outer;
        private boolean hovered;
        private boolean selected;

        public NGonSizeHandle(NGon node, boolean outer) {
            this.node = node;
            this.outer = outer;
            double angle = node.getAngle() + Math.PI/2;
            Point2D pt = GeomUtil.calcPoint(new Point2D.Double(node.getTranslateX(), node.getTranslateY()),
                    Math.toDegrees(angle), getRadius());
            this.x = pt.getX();
            this.y = pt.getY();
        }

        private double getRadius() {
            if(outer) {
                return node.getRadius();
            } else {
                return node.getInnerRadius();
            }
        }

        @Override
        public double getX() {
            if(!outer) {
                double angle = node.getAngle() + Math.PI/2;
                angle += Math.PI*2.0/node.getSides()/2.0;
                Point2D pt = GeomUtil.calcPoint(new Point2D.Double(node.getTranslateX(), node.getTranslateY()),
                        Math.toDegrees(angle), getRadius());
                return pt.getX();
            }
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
            if(outer) {
                node.setRadius(radius);
            } else {
                node.setInnerRadius(radius);
            }

            double angle  = 0;
            double dangle = GeomUtil.calcAngle(center,new Point2D.Double(x,y));
            if(constrain) {
                angle = Math.toRadians(GeomUtil.snapTo45(dangle));
            } else {
                angle = Math.toRadians(dangle);
            }
            angle = angle - Math.PI/2;
            if(outer) {
                node.setAngle(angle);
            }
        }

        @Override
        public double getY() {
            if(!outer) {
                double angle = node.getAngle() + Math.PI/2;
                angle += Math.PI*2.0/node.getSides()/2.0;
                Point2D pt = GeomUtil.calcPoint(new Point2D.Double(node.getTranslateX(), node.getTranslateY()),
                        Math.toDegrees(angle), getRadius());
                return pt.getY();
            }
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

        public void processMouseMove(MouseEvent event, Point2D.Double cursor) {
            hovered = contains(cursor,context.getCanvas().getScale());
            context.redraw();
        }

        public void processMousePressed(MouseEvent event, Point2D cursor) {
            selected = contains(cursor,context.getCanvas().getScale());
        }

        public void processMouseDragged(MouseEvent event, Point2D.Double cursor) {
            if(selected) {
                setX(cursor.getX(),event.isShiftPressed());
                setY(cursor.getY(),event.isShiftPressed());
                context.redraw();
            }
        }
        public void draw(GFX g) {
            FlatColor color = FlatColor.BLUE;
            if(hovered) {
                color = FlatColor.RED;
            }
            Point2D.Double pt = context.getSketchCanvas().transformToDrawing(getX(),getY());
            DrawUtils.drawStandardHandle(g,pt.getX(),pt.getY(),color);
        }

    }
}
