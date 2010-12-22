package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.SwatchColorPicker;
import org.joshy.gfx.node.layout.Container;
import org.joshy.sketch.canvas.SketchCanvas;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class GradientHandle extends Handle implements SRect.SRectUpdateListener {
    private SRect rect;
    private GradientPosition pos;
    private List<Control> controls;
    private SwatchColorPicker colorPopup;
    private VectorDocContext context;
    private boolean xSnapped = false;
    private boolean ySnapped = false;

    public GradientHandle(SRect rect, GradientPosition pos, VectorDocContext context) {
        super();
        this.rect = rect;
        this.rect.addListener(this);
        GradientFill grad = (GradientFill) rect.getFillPaint();
        this.context = context;
        this.pos = pos;

        colorPopup = new SwatchColorPicker();
        colorPopup.setPrefWidth(10);
        colorPopup.setPrefHeight(10);
        colorPopup.onColorSelected(new Callback<ChangedEvent>() {
            public void call(ChangedEvent changedEvent) throws Exception {
                GradientFill grad = (GradientFill) GradientHandle.this.rect.getFillPaint();
                FlatColor val = (FlatColor) changedEvent.getValue();
                if(val == null) {
                    val = FlatColor.WHITE_TRANSPARENT;
                }
                if(GradientHandle.this.pos == GradientHandle.GradientPosition.Start) {
                    grad.setStartColor(val);
                } else {
                    grad.setEndColor(val);
                }
            }
        });
        if(pos == GradientPosition.Start) {
            colorPopup.setSelectedColor(grad.getStartColor());
        } else {
            colorPopup.setSelectedColor(grad.getEndColor());
        }

        controls = new ArrayList<Control>();
        controls.add(colorPopup);
        updated();
    }

    @Override
    public boolean hasControls() {
        return true;
    }

    @Override
    public Iterable<? extends Control> getControls() {
        return controls;
    }

    @Override
    public double getX() {
        GradientFill grad = (GradientFill) rect.getFillPaint();
        if(pos == GradientHandle.GradientPosition.Start) {
            return rect.getTranslateX()+rect.getX()+grad.getStartX();
        } else {
            return rect.getTranslateX()+rect.getX()+grad.getEndX();
        }
    }

    @Override
    public void setX(double x, boolean constrain) {
        x = x - rect.getTranslateX() - rect.getX();
        xSnapped = true;
        x = snapX(x, 0);
        x = snapX(x, rect.getWidth()/2);
        x = snapX(x, rect.getWidth());
        GradientFill grad = (GradientFill) rect.getFillPaint();
        if(pos == GradientHandle.GradientPosition.Start) {
            updateGrad(x,grad.getStartY());
        } else {
            updateGrad(x,grad.getEndY());
        }
        updated();
    }

    @Override
    public double getY() {
        GradientFill grad = (GradientFill) rect.getFillPaint();
        if(pos == GradientHandle.GradientPosition.Start) {
            return rect.getTranslateY()+rect.getY()+grad.getStartY();
        } else {
            return rect.getTranslateY()+rect.getY()+grad.getEndY();
        }
    }

    @Override
    public void setY(double y, boolean constrain) {
        y = y-rect.getTranslateY()-rect.getY();
        ySnapped = false;
        y = snapY(y, 0);
        y = snapY(y, rect.getHeight() / 2);
        y = snapY(y, rect.getHeight());
        GradientFill grad = (GradientFill) rect.getFillPaint();
        if(pos == GradientHandle.GradientPosition.Start) {
            updateGrad(grad.getStartX(),y);
        } else {
            updateGrad(grad.getEndX(),y);
        }
        updated();
    }

    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        if(!(rect.getFillPaint() instanceof GradientFill)) return;

        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);

        double x = pt.getX();
        double y = pt.getY();

        GradientFill grad = (GradientFill) rect.getFillPaint();

        if(this.pos == GradientPosition.Start) {
            Bounds bounds = rect.getBounds();
            Point2D start = canvas.transformToDrawing(bounds.getX()+grad.getStartX(),bounds.getY()+grad.getStartY());
            Point2D end = canvas.transformToDrawing(bounds.getX()+grad.getEndX(),bounds.getY()+grad.getEndY());
            g.setPaint(FlatColor.BLACK);
            g.drawLine(start.getX(),start.getY(),end.getX(),end.getY());
            g.setPaint(FlatColor.WHITE);
            g.drawLine(start.getX()+1,start.getY()+1,end.getX()+1,end.getY()+1);
        }

        DrawUtils.drawStandardHandle(g, x, y, FlatColor.GREEN);


    }

    private double snapX(double x, double value) {
        if(Math.abs(x-value)<5) {
            x = value;
            xSnapped = true;
        }
        return x;
    }
    private double snapY(double y, double value) {
        if(Math.abs(y-value)<5) {
            y = value;
            ySnapped = true;
        }
        return y;
    }

    private void updateGrad(double x, double y) {
        GradientFill grad = (GradientFill) rect.getFillPaint();
        switch (pos) {
            case Start:
                grad = grad.derive(x,y,grad.getEndX(),grad.getEndY());
                grad.setStartSnapped(xSnapped && ySnapped);
                rect.setFillPaint(grad);
                break;
            case End:
                grad = grad.derive(grad.getStartX(),grad.getStartY(),x,y);
                grad.setEndSnapped(xSnapped && ySnapped);
                rect.setFillPaint(grad);
                break;
        }
    }


    public void updated() {
        Point2D pt = context.getSketchCanvas().transformToDrawing(getX(), getY());
        Container popupLayer = context.getSketchCanvas().getParent().getStage().getPopupLayer();
        pt = NodeUtils.convertToScene(context.getSketchCanvas(),pt);
        pt = NodeUtils.convertFromScene(popupLayer,pt);
        colorPopup.setTranslateX((int)(pt.getX()+20));
        colorPopup.setTranslateY((int)(pt.getY()-size));
    }

    public enum GradientPosition {
        End, Start
    }
}
