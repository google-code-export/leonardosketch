package org.joshy.sketch.tools;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.Textbox;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.actions.UndoableAddNodeAction;
import org.joshy.sketch.model.ResizableGrid9Shape;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SText;
import org.joshy.sketch.model.SketchDocument;

import java.awt.geom.Point2D;

public class DrawTextTool extends CanvasTool {
    private SText textNode;
    private Textbox overlayTextBox;
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
        textNode.setFontSize(24);
        Point2D.Double point = context.getSketchCanvas().transformToCanvas(x,y);
        textNode.setX(point.x);
        textNode.setY(point.y);

        Point2D pt2 = NodeUtils.convertToScene(context.getSketchCanvas(), point.x, point.y);

        overlayTextBox = new Textbox();
        overlayTextBox.setTranslateX(pt2.getX());
        overlayTextBox.setTranslateY(pt2.getY());
        overlayTextBox.setFont(Font.name("Arial")
            .size((float) (textNode.getFontSize()*context.getSketchCanvas().getScale()))
            .style(textNode.getStyle())
            .weight(textNode.getWeight())
            .resolve());
        overlayTextBox.setPrefWidth(300);
        overlayTextBox.setPrefHeight(10+overlayTextBox.getFont().calculateHeight("WXYwxy"));
        context.getCanvas().getParent().getStage().getPopupLayer().add(overlayTextBox);
        overlayTextBox.selectAll();
        overlayTextBox.setVisible(true);
        Core.getShared().getFocusManager().setFocusedNode(overlayTextBox);
    }

    private void endTextEditing() {
        context.getCanvas().getParent().getStage().getPopupLayer().remove(overlayTextBox);
        overlayTextBox.setVisible(false);
        textNode.text = overlayTextBox.getText();
        Font textFont = Font.name("Arial")
                .size((float)textNode.getFontSize())
                .style(textNode.getStyle())
                .weight(textNode.getWeight())
                .resolve();
        textNode.setWidth(textFont.calculateWidth(overlayTextBox.getText()));
        textNode.setHeight(textFont.calculateHeight(overlayTextBox.getText()));
        overlayTextBox = null;
        if(!notInMainDocument) {
            SketchDocument doc = (SketchDocument) context.getDocument();
            doc.getCurrentPage().add(textNode);
            context.getSelection().setSelectedNode(textNode);
        }
        context.getUndoManager().pushAction(new UndoableAddNodeAction(context,textNode,"text"));
        textNode = null;
        this.notInMainDocument = false;
    }

    public void startEditing(SNode node) {
        SketchDocument doc = (SketchDocument) context.getDocument();
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
        overlayTextBox = new Textbox();
        overlayTextBox.setFont(Font.name("Arial")
            .size((float) (textNode.getFontSize()*context.getSketchCanvas().getScale()))
            .style(textNode.getStyle())
            .weight(textNode.getWeight())
            .resolve());
        overlayTextBox.setText(textNode.text);
        Point2D point = context.getSketchCanvas().transformToDrawing(offsetX+textNode.getTranslateX()+textNode.getX(),
                                                  offsetY+textNode.getTranslateY()+textNode.getY()
                );

        Point2D pt2 = NodeUtils.convertToScene(context.getSketchCanvas(), point.getX(), point.getY());
        overlayTextBox.setTranslateX(pt2.getX());
        overlayTextBox.setTranslateY(pt2.getY());
        overlayTextBox.setPrefWidth(overlayTextBox.getFont().calculateWidth(textNode.text)+100);
        overlayTextBox.setPrefHeight(10+overlayTextBox.getFont().calculateHeight(textNode.text));
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
