package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Transform;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.ScrollPane;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;

import java.awt.geom.Point2D;
import java.util.List;

public class SketchCanvas extends DocumentCanvas implements ScrollPane.ScrollingAware {
    public Selection selection;
    private boolean showSelection;
    private VectorDocContext context;
    private SketchDocument document;
    private boolean vsnapVisible;
    private double hsnap;
    private boolean hsnapVisible;
    private double vsnap;
    private ScrollPane scrollPane;

    public SketchCanvas(VectorDocContext context) {
        this.context = context;
        document = new SketchDocument();
        selection = new Selection(context);
    }

    @Override
    public Bounds getVisualBounds() {
        return new Bounds(
                getTranslateX()
                ,getTranslateY()
                ,document.getWidth()*getScale()
                ,document.getHeight()*getScale()
        );
    }

    @Override
    public Bounds getInputBounds() {
        return new Bounds(
                getTranslateX()
                ,getTranslateY()
                ,getWidth()
                ,getHeight()
        );
    }

    @Override
    public void doLayout() {
    }

    @Override
    public void doPrefLayout() {

    }

    @Override
    public void doSkins() {
    }

    /*
    @Override
    public double getWidth() {
        return document.getWidth()*getScale();
    }

    @Override
    public double getHeight() {
        return document.getHeight()*getScale();
    }
  */
    public double getFullWidth(double width, double height) {
        return Math.max(document.getWidth()*getScale(),width);
    }

    public double getFullHeight(double width, double height) {
        return Math.max(document.getHeight()*getScale(),height);
    }

    public void setScrollX(double value) {
        this.panX = value;
    }

    public void setScrollY(double value) {
        this.panY = value;
    }

    public void setScrollParent(ScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    @Override
    public void draw(GFX g) {
        SketchDocument sdoc = document;
        g.setPaint(sdoc.getBackgroundFill());
        g.fillRect(0,0,getWidth(),getHeight());
        draw(g,sdoc);
    }

    private void draw(GFX g, SketchDocument sdoc) {
        g.translate(panX,panY);
        g.scale(getScale(),getScale());
        drawDocumentBounds(g,sdoc);
        drawDocumentGrid(g,sdoc);
        for(SNode node : sdoc.getCurrentPage().getNodes()) {
            draw(g,node);
        }

        drawGridlines(g,sdoc);
        drawSnaps(g,sdoc);
        g.scale(1/getScale(),1/getScale());
        g.translate(-panX,-panY);

        if(showSelection) {
            for(SNode node : selection.items()) {
                List<Handle> handles = selection.getHandles().get(node);
                if(handles != null && !handles.isEmpty()) {
                    drawSelection(g,node,handles);
                } else {
                    drawSelection(g,node);
                }
                if(node instanceof SResizeableNode) {
                    //drawSelection(g,(SResizeableNode)node);
                } else {
                    drawSelection(g,node);
                }
            }
        }

        if(context.selectedTool != null) {
            context.selectedTool.drawOverlay(g);
        }
    }

    private void drawGridlines(GFX g, SketchDocument sdoc) {
        for(SketchDocument.Guideline guideline : sdoc.getCurrentPage().getGuidelines()) {
            guideline.draw(g);
        }
    }

    private void drawSnaps(GFX g, SketchDocument doc) {
        g.setPaint(FlatColor.GREEN);
        if(hsnapVisible) {
            g.drawLine(hsnap,0, hsnap,doc.getHeight());
        }
        if(vsnapVisible) {
            g.drawLine(0,vsnap, doc.getWidth(),vsnap);
        }
    }

    private void drawDocumentBounds(GFX g, SketchDocument sdoc) {
        if(!sdoc.isDocBoundsActive()) return;
        g.setPaint(new FlatColor(1.0,0.8,0.8,1.0));
        g.drawRect(0,0,
            sdoc.getUnits().toPixels(sdoc.getWidth())
            ,sdoc.getUnits().toPixels(sdoc.getHeight()));
    }

    private void drawDocumentGrid(GFX g, SketchDocument sdoc) {
        if(!sdoc.isGridActive()) return;
        g.setPaint(new FlatColor(0.8,0.8,0.8,1.0));
        double w = sdoc.getUnits().toPixels(sdoc.getWidth());
        double gw = sdoc.getUnits().toPixels(sdoc.getGridWidth());
        double h = sdoc.getUnits().toPixels(sdoc.getHeight());
        double gh = sdoc.getUnits().toPixels(sdoc.getGridHeight());
        for(int i=0; i<w; i+=gw) {
            g.drawLine(i,0,i,h);
        }
        for(int i=0; i<h; i+=gh) {
            g.drawLine(0,i,w,i);
        }
    }

    private void draw(GFX g, SNode node) {
        g.translate(node.getTranslateX(),node.getTranslateY());
        g.scale(node.getScaleX(),node.getScaleY());
        g.rotate(node.getRotate(), Transform.Z_AXIS);
        if(node instanceof SelfDrawable) {
            ((SelfDrawable)node).draw(g);
        }
        if(node instanceof Button9) {
            draw(g,(Button9)node);
        }
        g.rotate(-node.getRotate(), Transform.Z_AXIS);
        g.scale(1/node.getScaleX(),1/node.getScaleY());
        g.translate(-node.getTranslateX(),-node.getTranslateY());
    }

    private static void draw(GFX g, Button9 button9) {
        button9.draw(g);
    }

    private void drawSelection(GFX g, SNode node, List<Handle> handles) {
        //List<Handle> handles = selection.getHandles().get(node);
        if(handles == null) return;
        for(Handle h : handles) {
            Point2D pt = new Point2D.Double(h.getX(),h.getY());
            pt = transformToDrawing(pt);
            h.draw(g,this);
        }
    }

    public void drawSelection(GFX g, SNode shape) {
        if(shape == null) return;
        g.setPaint(new FlatColor(1.0,0.5,0.5,0.5));
        Bounds bounds = shape.getBounds();
        bounds = transformToDrawing(bounds);
        g.setStrokeWidth(3);
        g.drawRect(bounds.getX(),bounds.getY(),bounds.getWidth(),bounds.getHeight());
        g.setStrokeWidth(1);
        g.setPaint(new FlatColor(0.5,0.2,0.2,1.0));
        g.drawRect(bounds.getX(),bounds.getY(),bounds.getWidth(),bounds.getHeight());
    }



    public boolean isFocused() {
        return true;
    }

    public SketchDocument getDocument() {
        return document;
    }

    public void setDocument(SketchDocument doc) {
        selection.clear();
        this.document = doc;
        if(this.getParent() != null) {
            this.getParent().getStage().setTitle(document.getTitle());
        }
        setDrawingDirty();
        EventBus.getSystem().addListener(SketchDocument.DocumentEvent.PageChanged, new Callback<SketchDocument.DocumentEvent>() {
            public void call(CanvasDocument.DocumentEvent event) {
                setDrawingDirty();
            }
        });
    }

    public void setShowSelection(boolean showSelection) {
        this.showSelection = showSelection;
    }

    public void showHSnap(double y) {
        hsnap = y;
        hsnapVisible = true;
    }
    
    public void showVSnap(double y2) {
        vsnap = y2;
        vsnapVisible = true;
    }

    public void hideHSnap() {
        hsnapVisible = false;
    }

    public void hideVSnap() {
        vsnapVisible = false;
    }

}
