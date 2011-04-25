package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.Container;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.sketch.model.AbstractResizeableNode;
import org.joshy.sketch.model.Handle;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.tools.MouseEventHandle;
import org.joshy.sketch.util.DrawUtils;
import org.joshy.sketch.util.Util;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/15/11
 * Time: 4:19 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseGradientHandle<G extends MultiGradientFill>
        extends Handle
        implements MouseEventHandle, SShape.SShapeListener {
    protected SShape shape;
    protected VectorDocContext context;
    protected Point2D.Double hoverPoint;
    protected boolean hovered;
    protected RadialGradientFill.Stop activeStop;
    protected boolean onStop;
    protected boolean onPoint;
    protected boolean draggingStop;
    protected Point2D.Double startPoint;
    protected Map<RadialGradientFill.Stop,Control> controlMap;
    protected ArrayList<Control> controls;
    protected boolean showAddIndicator;
    protected boolean couldDelete;
    private MultiGradientFill.Stop hoverStop;

    public BaseGradientHandle(SShape shape, VectorDocContext context) {
        this.shape = shape;
        this.context = context;
        this.shape.addListener(this);
        controls = new ArrayList<Control>();
        controlMap = new HashMap<RadialGradientFill.Stop,Control>();
        for(final MultiGradientFill.Stop stop : getFill().getStops()) {
            addStopControl(stop);
        }
        updateControlPositions();
    }

    protected G getFill() {
        return (G) shape.getFillPaint();
    }

    protected void addStopControl(final RadialGradientFill.Stop stop) {
        CustomSwatchColorPicker colorPopup = new CustomSwatchColorPicker();
        colorPopup.setPrefWidth(10);
        colorPopup.setPrefHeight(10);
        colorPopup.setSelectedColor(stop.getColor());
        controls.add(colorPopup);
        controlMap.put(stop,colorPopup);
        colorPopup.onColorSelected(new Callback<ChangedEvent>() {
            public void call(ChangedEvent changedEvent) throws Exception {
                FlatColor val = (FlatColor) changedEvent.getValue();
                if(val == null) {
                    val = FlatColor.WHITE_TRANSPARENT;
                }
                stop.setColor(val);
                refresh();
            }
        });
    }

    protected void removeStopControl(MultiGradientFill.Stop activeStop) {
        Control popup = controlMap.get(activeStop);
        controls.remove(popup);
        controlMap.remove(activeStop);
        Container popupLayer = context.getSketchCanvas().getParent().getStage().getPopupLayer();
        popupLayer.remove(popup);
    }

    protected void refresh() {
        shape.setFillPaint(shape.getFillPaint());
    }

    abstract void updateControlPositions();

    @Override
    public boolean hasControls() {
        return true;
    }

    @Override
    public Iterable<? extends Control> getControls() {
        return controls;
    }

    @Override
    public void detach() {
        if(shape instanceof AbstractResizeableNode) {
            ((AbstractResizeableNode)this.shape).removeListener(this);
        }
    }

    public void mouseMoved(boolean hovered, MouseEvent event, Point2D.Double cursor) {
        this.hovered = hovered;
        hoverPoint = cursor;
        showAddIndicator = true;
        hoverStop = null;
        for(MultiGradientFill.Stop stop: getFill().getStops()) {
            Point2D pt = Util.interpolatePoint(getStart(), getEnd(), stop.getPosition());
            if(pt.distance(cursor) < 10) {
                showAddIndicator = false;
                hoverStop = stop;
            }
        }
    }


    public boolean contains(double x, double y, double scale) {
        double size = this.size/scale;
        if(getEnd().distance(x,y) < 5) {
            return false;
        }
        Line2D.Double line = new Line2D.Double(getStart(),getEnd());
        if(line.ptSegDist(x,y) < size) {
            return true;
        } else {
            return false;
        }
    }

    public void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        if(onStop && !draggingStop) {
            //u.p("did a click");
        } else {
            //u.p("did a drag");
        }
        activeStop = null;
        onStop = false;
        onPoint = false;
        draggingStop = false;
        startPoint = null;
    }

    public void changed() {
        refresh();
        updateControlPositions();
    }


    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        if(!(shape.getFillPaint() instanceof MultiGradientFill)) return;

        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);

        double x = pt.getX();
        double y = pt.getY();

        drawConnectingLine(g, canvas);
        /*
        if(hovered) {
            if(hoverPoint.getY()-shape.getBounds().getY()-getY() < 5) {
                DrawUtils.drawStandardHandle(g, x, y, FlatColor.GREEN);
            } else {
                DrawUtils.drawStandardHandle(g, x, y, FlatColor.RED);
            }
        } else {
            DrawUtils.drawStandardHandle(g, x, y, FlatColor.GREEN);
        }
          */
        DrawUtils.drawStandardHandle(g, x, y, FlatColor.GREEN);

        drawStopHandles(g, canvas);
        if(showAddIndicator && hovered) {
            drawNewStopIndicator(g, canvas);
        }
    }

    protected void drawNewStopIndicator(GFX g, SketchCanvas canvas) {
        Point2D closest = closestPoint(getStart(), getEnd(), hoverPoint);
        Point2D hp = canvas.transformToDrawing(closest.getX(), closest.getY());
        g.translate(hp.getX(),hp.getY());

        double angle = GeomUtil.calcAngle(getStart(), getEnd());
        g.rotate(-Math.toDegrees(angle), Transform.Z_AXIS);
        Path2D.Double path = createHandlePath();
        g.setPaint(FlatColor.WHITE);
        g.fillPath(path);
        g.setPaint(FlatColor.BLACK);
        g.drawPath(path);
        g.rotate(Math.toDegrees(angle), Transform.Z_AXIS);
        g.translate(-hp.getX(),-hp.getY());
    }

    private static Point2D closestPoint(Point2D a, Point2D b, Point2D c) {
        double theta1 = GeomUtil.calcAngle(a,c);
        double theta2 = GeomUtil.calcAngle(a,b);
        double theta = theta2-theta1;

        double ac = a.distance(c);
        double ad = Math.cos(theta)*ac;
        return GeomUtil.calcPoint(a, Math.toDegrees(GeomUtil.calcAngle(a, b)), ad);
    }

    protected void drawStopHandles(GFX g, SketchCanvas canvas) {
        g.setPureStrokes(true);
        //draw the handles
        for(MultiGradientFill.Stop stop: getFill().getStops()) {
            if(getFill().isFirst(stop)) continue;
            if(getFill().isLast(stop)) continue;
            Point2D pt = getDragHandlePosition(stop,canvas);
            g.translate(pt.getX(),pt.getY());

            double angle = GeomUtil.calcAngle(getStart(),getEnd());
            g.rotate(-Math.toDegrees(angle), Transform.Z_AXIS);
            Path2D.Double path = createHandlePath();

            //fill stop
            g.setPaint(stop.getColor());
            if(stop == hoverStop) {
                g.setPaint(FlatColor.WHITE);
            }
            if(stop == activeStop) {
                g.setPaint(FlatColor.WHITE);
                if(couldDelete) {
                    g.setPaint(FlatColor.GRAY);
                }
            }
            g.fillPath(path);

            //stroke stop
            g.setPaint(FlatColor.BLACK);
            if(stop == activeStop && couldDelete) {
                g.setPaint(FlatColor.GRAY);
            }
            g.drawPath(path);


            g.rotate(Math.toDegrees(angle), Transform.Z_AXIS);
            g.translate(-pt.getX(),-pt.getY());
        }
        g.setPureStrokes(false);
    }

    private Path2D.Double createHandlePath() {
        Path2D.Double path = new Path2D.Double();
        double s = 9;
        path.moveTo(0,-s/2);
        path.lineTo(s, 0);
        path.lineTo(0,s/2);
        path.lineTo(-s,0);
        path.closePath();
        return path;
    }

    protected abstract Point2D getDragHandlePosition(MultiGradientFill.Stop stop, SketchCanvas canvas);

    protected void drawConnectingLine(GFX g, SketchCanvas canvas) {
        Point2D s = getConnectingLineStart();
        Point2D e = getConnectingLineEnd();
        //draw the gradient line
        g.setPaint(FlatColor.BLACK);
        g.drawLine(s.getX(), s.getY(), e.getX(), e.getY());
        g.setPaint(FlatColor.WHITE);
        g.drawLine(s.getX()+1, s.getY()+1, e.getX()+1,e.getY()+1);
    }

    abstract Point2D getConnectingLineStart();

    abstract Point2D getConnectingLineEnd();

    protected abstract Point2D getStart();

    protected abstract Point2D getEnd();

    private class CustomSwatchColorPicker extends Button {
        FlatColor selectedColor = FlatColor.RED;
        private PopupColorPicker popup;

        @Override
        protected void setPressed(boolean pressed) {
            super.setPressed(pressed);
            if (pressed) {
                if (popup == null) {
                    popup = new PopupColorPicker(this,200,100);
                    popup.setVisible(false);
                    Stage stage = getParent().getStage();
                    stage.getPopupLayer().add(popup);
                }
                Point2D pt = NodeUtils.convertToScene(this, 0, getHeight());

                double x = pt.getX();
                double y = pt.getY();
                FlatColor color = this.getSelectedColor();
                popup.positionAt(x, y, color);
                popup.setVisible(true);
                EventBus.getSystem().setPressedNode(popup);
            } else {
                popup.setVisible(false);
            }
        }

        @Override
        public void draw(GFX g) {
            if(!isVisible())return;
            g.setPureStrokes(true);
            g.setPaint(FlatColor.BLACK);
            g.fillOval(0, 0, getWidth(), getHeight());
            g.setPaint(FlatColor.WHITE);
            g.fillOval(0 + 1, 0 + 1, getWidth() - 2, getHeight() - 2);
            g.setPaint(selectedColor);
            g.fillOval(0 + 2, 0 + 2, getWidth() - 4, getHeight() - 4);
            g.setPureStrokes(false);
        }

        public void setSelectedColor(FlatColor color) {
            this.selectedColor = color;
            EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.ColorChanged,selectedColor,this));
            setDrawingDirty();
        }

        public void onColorSelected(Callback<ChangedEvent> callback) {
            EventBus.getSystem().addListener(this, ChangedEvent.ColorChanged, callback);
        }

        public FlatColor getSelectedColor() {
            return selectedColor;
        }
        private class PopupColorPicker extends Control {
            private Image image;
            private BufferedImage img;

            public PopupColorPicker(CustomSwatchColorPicker swatchColorPicker, int width, int height) {
                EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>() {
                    public void call(MouseEvent event) {
                        processMouse(event);
                    }
                });

                img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                for(int x=0; x<width; x++) {
                    for(int y=0; y<height; y++) {
                        double hue = (double)x/(double)width*360.0;
                        double sat = 1.0;
                        double bri = 1.0;
                        if (y < height/2) {
                            sat = (double)y/ (height/2.0);
                        } else {
                            bri = 1.0-(double)(y-height/2)/(height/2.0);
                        }
                        FlatColor c =  FlatColor.hsb(hue,sat,bri);
                        g.setPaint(new java.awt.Color(c.getRGBA()));
                        g.fillRect(x,y,1,1);
                    }
                }
                g.dispose();
                image = Image.create(img);
            }

            private void processMouse(MouseEvent event) {
                if (event.getType() == MouseEvent.MouseDragged) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    if(x < 0 || x > img.getWidth()-1 || y <0 || y > img.getHeight()-1 ) {
                        setSelectedColor(FlatColor.RED);
                        setDrawingDirty();return;
                    }
                    setSelectedColor(new FlatColor(img.getRGB(x,y)));
                    setDrawingDirty();
                }
                if (event.getType() == MouseEvent.MouseReleased) {
                    setDrawingDirty();
                    setVisible(false);
                    CustomSwatchColorPicker.this.setFinalColor(getSelectedColor());
                }

            }

            @Override
            public void doPrefLayout() {
                //noop
            }

            @Override
            public void doLayout() {
                setWidth(img.getWidth());
                setHeight(img.getHeight());
            }

            @Override
            public void doSkins() {
            }

            @Override
            public void draw(GFX g) {
                if (!isVisible()) return;
                g.setPaint(FlatColor.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.drawImage(image,0,0);
                Point2D pt = colorToPoint(getSelectedColor());
                g.setPaint(FlatColor.BLACK);
                g.drawOval(pt.getX()-5,pt.getY()-5,10,10);
                g.setPaint(FlatColor.WHITE);
                g.drawOval(pt.getX()-4,pt.getY()-4,8,8);
            }

            public Point2D colorToPoint(FlatColor color) {
                //u.p("hue = " + color.getHue() + " " + getWidth() + " sat = " + color.getSaturation() + " brig = " + color.getBrightness());
                double sat = color.getSaturation();
                double bri = color.getBrightness();
                double dx = color.getHue()/360.0*img.getWidth();

                double dy = 0;
                double hh = img.getHeight()/2.0;
                if(sat < 1.0) {
                    dy = sat*hh;
                } else {
                    dy = hh + (1.0-bri)*hh;
                }

                return new Point2D.Double(dx,dy);
            }
            public void positionAt(double x, double y, FlatColor color) {
                Point2D pt = colorToPoint(color);
                setTranslateX(x-pt.getX()+5);
                setTranslateY(y-pt.getY()-5);
            }
        }

        private void setFinalColor(FlatColor selectedColor) {
        }
    }
}
