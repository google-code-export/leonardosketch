package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.sketch.canvas.SketchCanvas;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SRect;

import java.awt.geom.Point2D;
import java.io.IOException;

public class RectPropsPalette extends VFlexBox {
    private Checkbox useGradient;
    private SwatchColorPicker gradientStartButton;
    private SwatchColorPicker gradientEndButton;
    private Slider gradientAngle;
    //private Checkbox useShadow;
//        private Knob shadowAngle;
    //private Slider shadowWidth;
    //private Slider shadowDistance;
    private Slider cornerRadius;
    private DragHandle dragHandle;
    private SketchCanvas canvas;

    public RectPropsPalette(SketchCanvas canvas) throws IOException {
        this.canvas = canvas;
        dragHandle = new DragHandle();
        dragHandle.setWidth(200);
        dragHandle.setHeight(20);
        add(dragHandle);

        useGradient = new Checkbox("Gradient Fill");
        add(useGradient);
        gradientStartButton = new SwatchColorPicker();
        gradientStartButton.setSelectedColor(FlatColor.RED);
        add(gradientStartButton);
        gradientEndButton = new SwatchColorPicker();
        gradientEndButton.setSelectedColor(FlatColor.BLUE);
        add(gradientEndButton);
        add(new Label("Angle"));
        gradientAngle = new Slider(false).setMin(0).setMax(360).setValue(180);
        add(gradientAngle);
//        useShadow = new Checkbox("Shadow");
//        box.add(useShadow);
        //shadowAngle = new Knob();
        //box.add(shadowAngle);
//        shadowWidth = new Slider(false);
//        box.add(shadowWidth);
//        shadowDistance = new Slider(false);
//        box.add(shadowDistance);

        add(new Label("Corner Radius"));
        cornerRadius = new Slider(false);
        add(cornerRadius);

        EventBus.getSystem().addListener(ChangedEvent.ColorChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                if(event.getSource() == gradientStartButton) {
                    updateGradient();
                }
                if(event.getSource() == gradientEndButton) {
                    updateGradient();
                }
            }

        });
        EventBus.getSystem().addListener(ChangedEvent.DoubleChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                if(event.getSource() == gradientAngle) {
                    if(useGradient.isSelected()) {
                        updateGradient();
                    }
                }
                if(event.getSource() == cornerRadius) {
                    updateRoundRect();
                }
            }
        });
        EventBus.getSystem().addListener(ActionEvent.Action, new Callback<ActionEvent>() {
            public void call(ActionEvent event) {
                if(event.getSource() == useGradient) {
                    updateGradient();
                }
            }
        });

        setFill(FlatColor.GRAY.deriveWithAlpha(0.8));

    }

    private void updateRoundRect() {
        if(canvas.selection.size() == 1) {
            SNode shape = canvas.selection.items().iterator().next();
            if(shape instanceof SRect) {
                SRect rect = (SRect) shape;
                rect.setCorner(cornerRadius.getValue());
            }
        }
    }

    private void updateGradient() {
        if(canvas.selection.size() != 1) return;

        SNode shape = canvas.selection.firstItem();
        if(shape instanceof SRect) {
            SRect rect = (SRect) shape;
            if(useGradient.isSelected()) {
                GradientFill gf = new GradientFill(
                        gradientStartButton.getSelectedColor(),
                        gradientEndButton.getSelectedColor(),
                        gradientAngle.getValue(),
                        true
                        );
                rect.setFillPaint(gf);
            } else {
                rect.setFillPaint(FlatColor.BLACK);
            }
            canvas.redraw();
        }

    }

    private class DragHandle extends Control {
        public Point2D startPoint;
        private DragHandle() {
            EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>() {
                public void call(MouseEvent event) {
                    if(event.getType() == MouseEvent.MousePressed) {
                        startPoint = event.getPointInSceneCoords();
                        Point2D pt = event.getPointInNodeCoords(DragHandle.this);
                        Bounds bounds = new Bounds(5,5,10,10);
                        if(bounds.contains(pt.getX(),pt.getY())) {
                            RectPropsPalette.this.setVisible(false);
                        }
                    }
                    if(event.getType() == MouseEvent.MouseDragged) {
                        Point2D currentPoint = event.getPointInSceneCoords();
                        double diffx = currentPoint.getX() - startPoint.getX();
                        double diffy = currentPoint.getY() - startPoint.getY();
                        startPoint = currentPoint;
                        Node parent = (Node) getParent();
                        parent.setTranslateX(parent.getTranslateX()+diffx);
                        parent.setTranslateY(parent.getTranslateY()+diffy);
                    }
                }
            });
        }

        @Override
        public void doLayout() {
        }

        @Override
        public void doPrefLayout() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void doSkins() {
        }

        @Override
        public void draw(GFX g) {
            g.setPaint(FlatColor.BLACK);
            g.fillRect(0,0,getWidth(),getHeight());
            g.setPaint(FlatColor.RED);
            g.fillRect(5,5,10,10);
        }
    }
}
