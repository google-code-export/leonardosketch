package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Togglebutton;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.util.u;
import org.joshy.sketch.canvas.PositionHandle;
import org.joshy.sketch.model.ResizableGrid9Shape;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.util.DrawUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class EditResizableShapeTool extends CanvasTool {
    private ResizableGrid9Shape shape;
    private VHandle selectedHandle;
    private ArrayList<VHandle> handles;
    private double sizedWidth;
    private double sizedHeight;
    private Togglebutton hlocked;
    private Togglebutton vlocked;
    private VFlexBox panel;

    public EditResizableShapeTool(VectorDocContext context) {
        super(context);
        handles = new ArrayList<VHandle>();
    }

    @Override
    public void enable() {
        super.enable();
        context.getSketchCanvas().setShowSelection(false);
        shape = (ResizableGrid9Shape)context.getSelection().items().iterator().next();
        sizedWidth = shape.getWidth();
        sizedHeight = shape.getHeight();
        shape.setWidth(shape.getOriginalWidth());
        shape.setHeight(shape.getOriginalHeight());
        Bounds bounds = shape.getBounds();
        handles.add(new VHandle(shape,shape.getLeft()+bounds.getX(),0, PositionHandle.Position.Left));
        handles.add(new VHandle(shape,shape.getRight()+bounds.getX(),0, PositionHandle.Position.Right));
        handles.add(new VHandle(shape,0,shape.getTop()+bounds.getY(), PositionHandle.Position.Top));
        handles.add(new VHandle(shape,0,shape.getBottom()+bounds.getY(), PositionHandle.Position.Bottom));

        hlocked = new Togglebutton("h locked");
        hlocked.setSelected(shape.isHLocked());
        hlocked.onClicked(new Callback<ActionEvent>(){
            public void call(ActionEvent event) {
                shape.setHLocked(hlocked.isSelected());
            }
        });
        vlocked = new Togglebutton("v locked");
        vlocked.setSelected(shape.isVLocked());
        vlocked.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) {
                shape.setVLocked(vlocked.isSelected());
            }
        });
        panel = new VFlexBox();
        panel.add(hlocked,vlocked);
        panel.setTranslateX(100);
        panel.setTranslateY(20);
        context.getCanvas().getParent().getStage().getPopupLayer().add(panel);

    }

    @Override
    public void disable() {
        super.disable();
        context.getSketchCanvas().setShowSelection(true);
        if(shape != null) {
            shape.setWidth(sizedWidth);
            shape.setHeight(sizedHeight);
            shape = null;
        }
        handles.clear();
        if(panel != null) {
            context.getCanvas().getParent().getStage().getPopupLayer().remove(panel);
        }
    }

    @Override
    public void call(KeyEvent event) {
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        Bounds bounds = shape.getBounds();
        //grow by 5 pixels on a side to account for the handles
        bounds = new Bounds(bounds.getX()-5,bounds.getY()-5,bounds.getWidth()+10,bounds.getHeight()+10);
        if(bounds.contains(cursor)) {
            for(VHandle handle : handles) {
                if((handle.getPosition() == PositionHandle.Position.Left) || (handle.getPosition() == PositionHandle.Position.Right)) {
                    if(Math.abs(event.getX()-handle.getX()) < 10) {
                        selectedHandle = handle;
                        return;
                    }
                }
                if((handle.getPosition() == PositionHandle.Position.Top || handle.getPosition() == PositionHandle.Position.Bottom)) {
                    if(Math.abs(event.getY()-handle.getY()) < 10) {
                        selectedHandle = handle;
                        return;
                    }
                }
            }
        } else {
            context.releaseControl();
        }
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        if(selectedHandle != null) {
            double x = event.getX();
            selectedHandle.setX(x);
            selectedHandle.setY(event.getY());
            context.redraw();
        }
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        selectedHandle = null;
    }

    public void drawOverlay(GFX g) {
        g.setPaint(FlatColor.RED);
        Bounds bounds = shape.getBounds();
        g.drawRect(bounds.getX(),bounds.getY(),bounds.getWidth(),bounds.getHeight());
        FlatColor color = new FlatColor(0xff097d);
        for(VHandle handle : handles) {
            switch(handle.getPosition()) {
                case Top:
                case Bottom:
                    g.setPaint(color);
                    g.drawLine(bounds.getX(),handle.getY(),bounds.getX()+bounds.getWidth(),handle.getY());
                    DrawUtils.drawStandardHandle(g,bounds.getX(),handle.getY(),color);
                    DrawUtils.drawStandardHandle(g,bounds.getX()+bounds.getWidth(),handle.getY(),color);
                    break;
                case Right:
                case Left:
                    g.setPaint(color);
                    g.drawLine(handle.getX(),bounds.getY(),handle.getX(),bounds.getY()+bounds.getHeight());
                    DrawUtils.drawStandardHandle(g,handle.getX(),bounds.getY(),color);
                    DrawUtils.drawStandardHandle(g,handle.getX(),bounds.getY()+bounds.getHeight(),color);
                    break;
            }
        }
    }

    public static class VHandle {
        private double x;
        private double y;
        private PositionHandle.Position position;
        private ResizableGrid9Shape shape;

        public VHandle(ResizableGrid9Shape shape, double x, double y, PositionHandle.Position position) {
            this.shape = shape;
            this.x = x;
            this.y = y;
            this.position = position;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            Bounds bounds = shape.getBounds();
            x = Math.max(bounds.getX(),x);
            x = Math.min(bounds.getX()+bounds.getWidth(),x);
            this.x = x;
            x -= bounds.getX();
            if(position == PositionHandle.Position.Left) {
                shape.setLeft(x);
            }
            if(position == PositionHandle.Position.Right) {
                shape.setRight(x);
            }
        }

        public PositionHandle.Position getPosition() {
            return position;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            Bounds bounds = shape.getBounds();
            y = Math.max(bounds.getY(),y);
            y = Math.min(bounds.getY()+bounds.getHeight(),y);
            this.y = y;
            y -= bounds.getY();
            if(position == PositionHandle.Position.Top) {
                shape.setTop(y);
            }
            if(position == PositionHandle.Position.Bottom) {
                shape.setBottom(y);
            }
        }
    }
}
