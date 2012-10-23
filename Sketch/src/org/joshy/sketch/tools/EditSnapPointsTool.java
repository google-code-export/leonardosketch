package org.joshy.sketch.tools;

import java.awt.geom.Point2D;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.modes.vector.VectorDocContext;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 10/22/12
 * Time: 7:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditSnapPointsTool extends CanvasTool {
    private SNode node;
    private Point2D currentPoint;
    private Button addButton;
    private VFlexBox panel;
    private Button deleteButton;
    private Button endButton;

    public EditSnapPointsTool(VectorDocContext context) {
        super(context);
    }

    @Override
    public void enable() {
        super.enable();
        context.getSketchCanvas().setShowSelection(false);
        context.getPropPanel().setVisible(false);
        node = context.getSelection().items().iterator().next();
        addButton = new Button("add snap point");
        addButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) {
                node.addSnapPoint(new Point2D.Double(0.4,0.7));
                context.redraw();
            }
        });
        
        deleteButton = new Button("delete snap point");
        deleteButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                if(currentPoint != null) {
                    node.removeSnapPoint(currentPoint);
                    currentPoint = null;
                    context.redraw();
                }
            }
        });
        
        endButton = new Button("done");
        endButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                context.releaseControl();
                context.redraw();
            }
        });
        
        
        panel = new VFlexBox();
        panel.add(addButton,deleteButton, endButton);
        panel.setTranslateX(100);
        panel.setTranslateY(20);
        context.getCanvas().getParent().getStage().getPopupLayer().add(panel);
    }

    @Override
    public void disable() {
        super.disable();
        context.getSketchCanvas().setShowSelection(true);
        if(panel != null) {
            context.getCanvas().getParent().getStage().getPopupLayer().remove(panel);
        }
}

    @Override
    public void drawOverlay(GFX g) {
        if(node == null) return;
        Bounds b = node.getTransformedBounds();
        for(Point2D pt : node.getSnapPoints()) {
            double x = b.getX() + b.getWidth()*pt.getX();
            double y = b.getY() + b.getHeight()*pt.getY();
            g.setPaint(FlatColor.PURPLE);
            g.fillOval(x - 5, y - 5, 10, 10);
        }
        if(currentPoint != null) {
            double x = b.getX() + b.getWidth()*currentPoint.getX();
            double y = b.getY() + b.getHeight()*currentPoint.getY();
            g.setPaint(FlatColor.RED);
            g.fillOval(x - 8, y - 8, 16, 16);
        }
    }

    @Override
    protected void call(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_ESCAPE) {
            context.releaseControl();
        }
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
    }

    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        Point2D pt = getSnapPoint(cursor);
        if(pt != null) {
            currentPoint = pt;
        }
    }

    private Point2D getSnapPoint(Point2D.Double cursor) {
        for(Point2D pt : node.getSnapPoints()) {
            Bounds b = node.getBounds();
            double x = b.getX() + b.getWidth()*pt.getX();
            double y = b.getY() + b.getHeight()*pt.getY();
            Point2D pt2 = subtract(cursor,node.getTranslateX(),node.getTranslateY());
            double dist = pt2.distance(x,y);
            if(Math.abs(dist) < 10) {
                return pt;
            }
        }
        return null;
    }

    private Point2D.Double subtract(Point2D.Double cursor, double translateX, double translateY) {
        return new Point2D.Double(cursor.getX()-translateX,cursor.getY()-translateY);
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        if(currentPoint != null) {
            Bounds b = node.getBounds();
            cursor = subtract(cursor,node.getTranslateX(),node.getTranslateY());
            double x = (cursor.getX()-b.getX())/b.getWidth();
            double y = (cursor.getY()-b.getY())/b.getHeight();
            if(x < 0) x = 0;
            if(x > 1) x = 1;
            if(y < 0) y = 0;
            if(y > 1) y = 1;
            currentPoint.setLocation(x,y);
            context.redraw();
        }
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        //currentPoint = null;
    }
}
