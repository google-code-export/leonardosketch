package org.joshy.sketch.tools;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Textarea;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.UndoableAddNodeAction;
import org.joshy.sketch.model.ResizableGrid9Shape;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SText;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Point2D;

public class DrawTextTool extends CanvasTool {
    private SText textNode;
    private Textarea overlayTextBox;
    private boolean notInMainDocument;

    public DrawTextTool(VectorDocContext context) {
        super(context);
    }

    @Override
    public void call(KeyEvent event) {
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
    }


    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        if(textNode == null) {
            startTextEditing(event.getX(), event.getY());
        } else {
            endTextEditing();
            context.releaseControl();
        }
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
    }

    private void startTextEditing(double x, double y) {
        textNode = new SText();
        textNode.setFillPaint(FlatColor.BLACK);
        textNode.setFontName(Main.DEFAULT_FONT_NAME);
        textNode.setFontSize(24);
        Point2D.Double point = context.getSketchCanvas().transformToCanvas(x,y);
        textNode.setX(point.x);
        textNode.setY(point.y);

        Point2D pt2 = NodeUtils.convertToScene(context.getSketchCanvas(), point.x, point.y);

        overlayTextBox = new Textarea();
        //the -9 is to account for the textbox's insets
        overlayTextBox.setTranslateX(pt2.getX()-9);
        overlayTextBox.setTranslateY(pt2.getY()-9);
        overlayTextBox.setFont(Font
                .name(textNode.getFontName())
                .size((float) (textNode.getFontSize()*context.getSketchCanvas().getScale()))
                .style(textNode.getStyle())
                .weight(textNode.getWeight())
                .resolve());
        overlayTextBox.setSizeToText(true);
        context.getCanvas().getParent().getStage().getPopupLayer().add(overlayTextBox);
        overlayTextBox.selectAll();
        overlayTextBox.setVisible(true);
        Core.getShared().getFocusManager().setFocusedNode(overlayTextBox);
    }

    private void endTextEditing() {
        context.getCanvas().getParent().getStage().getPopupLayer().remove(overlayTextBox);
        overlayTextBox.setVisible(false);
        textNode.setText(overlayTextBox.getText());
        textNode.refresh();
        overlayTextBox = null;
        if(!notInMainDocument) {
            SketchDocument doc = context.getDocument();
            doc.getCurrentPage().add(textNode);
            context.getSelection().setSelectedNode(textNode);
        }
        context.getUndoManager().pushAction(new UndoableAddNodeAction(context,textNode,"text"));
        textNode = null;
        this.notInMainDocument = false;
    }

    public void startEditing(SNode node) {
        SketchDocument doc = context.getDocument();
        double offsetX = 0;
        double offsetY = 0;
        if(node instanceof SText) {
            this.notInMainDocument = false;
            textNode = (SText) node;
            doc.getCurrentPage().remove(textNode);
        }
        if(node instanceof ResizableGrid9Shape) {
            ResizableGrid9Shape grid9 = (ResizableGrid9Shape) node;
            textNode = grid9.getTextChild();
            this.notInMainDocument = true;
            offsetX = grid9.getTranslateX();
            offsetY = grid9.getTranslateY();
        }
        overlayTextBox = new Textarea();
        overlayTextBox.setFont(Font
            .name(textNode.getFontName())
            .size((float) (textNode.getFontSize()*context.getSketchCanvas().getScale()))
            .style(textNode.getStyle())
            .weight(textNode.getWeight())
            .resolve());
        overlayTextBox.setText(textNode.getText());
        Point2D point = context.getSketchCanvas().transformToDrawing(offsetX+textNode.getTranslateX()+textNode.getX(),
                                                  offsetY+textNode.getTranslateY()+textNode.getY()
                );

        //the -9 is to account for the textbox's insets
        Point2D pt2 = NodeUtils.convertToScene(context.getSketchCanvas(), point.getX()-9, point.getY()-9);
        overlayTextBox.setTranslateX(pt2.getX());
        overlayTextBox.setTranslateY(pt2.getY());
        overlayTextBox.setSizeToText(true);
        context.getCanvas().getParent().getStage().getPopupLayer().add(overlayTextBox);
        overlayTextBox.selectAll();
        overlayTextBox.setVisible(true);
        Core.getShared().getFocusManager().setFocusedNode(overlayTextBox);
        overlayTextBox.doSkins();
        overlayTextBox.doPrefLayout();
        overlayTextBox.doLayout();
    }

    public void drawOverlay(GFX g) {
    }

}
