package org.joshy.sketch.tools;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.util.u;
import org.joshy.sketch.canvas.PositionHandle;
import org.joshy.sketch.canvas.SketchCanvas;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.canvas.ResizeHandle;
import org.joshy.sketch.model.SNode;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 18, 2010
 * Time: 3:20:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransformTool extends CanvasTool {
    private ScaleHandle hoverHandle;
    private ScaleHandle dragHandle;
    private SNode node;
    private List<ScaleHandle> handles = new ArrayList<ScaleHandle>();

    public TransformTool(VectorDocContext context) {
        super(context);
    }

    @Override
    public void call(KeyEvent event) {

    }

    @Override
    public void enable() {
        super.enable();
        context.getSketchCanvas().setShowSelection(false);
        if(context.getSelection().size() != 1) return;
        node = context.getSelection().items().iterator().next();
        u.p("enabled with node: " + node);
        handles.clear();
        handles.add(new ScaleHandle(node, ResizeHandle.Position.TopLeft));
        handles.add(new ScaleHandle(node, ResizeHandle.Position.Top));
        handles.add(new ScaleHandle(node, ResizeHandle.Position.TopRight));

        handles.add(new ScaleHandle(node, ResizeHandle.Position.Left));
        handles.add(new ScaleHandle(node, ResizeHandle.Position.Right));

        handles.add(new ScaleHandle(node, ResizeHandle.Position.BottomLeft));
        handles.add(new ScaleHandle(node, ResizeHandle.Position.Bottom));
        handles.add(new ScaleHandle(node, ResizeHandle.Position.BottomRight));
        context.redraw();
    }

    @Override
    public void disable() {
        super.disable();
        context.getSketchCanvas().setShowSelection(true);
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
        if(context.getSelection().size() != 1) return;

        for(ScaleHandle h : handles) {
            if(h.contains(event.getPointInNodeCoords(context.getCanvas()))) {
                hoverHandle = h;
                context.redraw();
            }
        }
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        if(hoverHandle != null) {
            dragHandle = hoverHandle;
            node = context.getSelection().items().iterator().next();
        }
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        if(dragHandle == null) return;

        u.p("scaling");
        dragHandle.setX(event.getX(),false);
        dragHandle.setY(event.getY(),false);
        context.redraw();

    }
    
    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        dragHandle = null;
        hoverHandle = null;
    }

    public void drawOverlay(GFX g) {
        if(context.getSelection().size() != 1) return;
        if(node == null) return;
        Bounds bounds = node.getBounds();
        g.setPaint(FlatColor.RED);
        g.drawRect(bounds.getX(),bounds.getY(),bounds.getWidth()*node.getScaleX(),bounds.getHeight()*node.getScaleY());

        for(ScaleHandle h : handles) {
            g.drawOval(h.getX()-5,h.getY()-5,10,10);
        }

        if(hoverHandle != null) {
            g.drawOval(hoverHandle.getX()-10,hoverHandle.getY()-10,20,20);
        }
    }

    private class ScaleHandle extends PositionHandle {
        private SNode node;

        public ScaleHandle(SNode node, ResizeHandle.Position position) {
            super(position);
            this.node = node;
        }

        @Override
        public double getX() {
            Bounds bounds = node.getBounds();
            switch(position) {
                case BottomLeft:
                case Left:
                case TopLeft: return bounds.getX();

                case Bottom:
                case Top: return bounds.getX()+(bounds.getWidth()*node.getScaleX())/2.0;

                case TopRight:
                case Right:
                case BottomRight: return bounds.getX()+bounds.getWidth()*node.getScaleX();
            }
            return 0;
        }
        @Override
        public double getY() {
            Bounds bounds = node.getBounds();
            switch(position) {
                case TopLeft:
                case Top:
                case TopRight:    return bounds.getY();

                case Left:
                case Right:       return bounds.getY()+(bounds.getHeight()*node.getScaleY())/2.0;
                
                case BottomLeft:
                case Bottom:
                case BottomRight: return bounds.getY()+bounds.getHeight()*node.getScaleY();
            }
            return 0;
        }

        @Override
        public void setY(double y, boolean constrain) {
            Bounds bounds = node.getBounds();
            switch(position) {
                case TopLeft:
                case Top:
                case TopRight:
                    double height = bounds.getHeight();
                    double realHeight = height*node.getScaleY();
                    double deltay = node.getTranslateY()-y;
                    realHeight += deltay;
                    node.setTranslateY(y);
                    node.setScaleY((realHeight/height));
                    return;

                case Left:
                case Right: return;

                case BottomLeft:
                case Bottom:
                case BottomRight:
                    node.setScaleY((y-bounds.getY())/bounds.getHeight());
                    return;
            }
        }

        @Override
        public void draw(GFX g, SketchCanvas sketchCanvas) {
            
        }


        @Override
        public void setX(double x, boolean constrain) {
            Bounds bounds = node.getBounds();
            double width = node.getBounds().getWidth();
            switch(position) {
                case TopLeft:
                case Left:
                case BottomLeft:
                    double rw = width*node.getScaleX();
                    double deltax = node.getTranslateX()-x;
                    rw+=deltax;
                    node.setTranslateX(x);
                    node.setScaleX(rw/width);
                    return;
                case TopRight:
                case Right:
                case BottomRight:
                    double realwidth = x-node.getBounds().getX();
                    node.setScaleX(realwidth/width);
                    return;
                    
            }
        }
    }
}
