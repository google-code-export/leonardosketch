package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Paint;
import org.joshy.gfx.event.EventBus;
import org.joshy.sketch.util.Util;

import java.util.ArrayList;
import java.util.List;

public class SketchDocument extends CanvasDocument<SketchDocument.SketchPage> {
    private boolean gridActive = true;
    private double gridWidth = 25;
    private double gridHeight = 25;
    private boolean snapGrid;
    private boolean docBoundsActive = true;
    private Paint backgroundFill = FlatColor.WHITE;
    private boolean presentation;
    private boolean snapDocBounds;
    private boolean snapNodeBounds;

    public SketchDocument() {
        setWidth(800);
        setHeight(600);
        this.pages.add(new SketchPage(this));
        setCurrentPage(0);
    }

    public boolean isGridActive() {
        return gridActive;
    }

    public double getGridWidth() {
        return gridWidth;
    }

    public double getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(double gridHeight) {
        this.gridHeight = gridHeight;
    }

    public void setGridActive(boolean gridActive) {
        this.gridActive = gridActive;
    }

    public boolean isSnapGrid() {
        return snapGrid;
    }

    public void setSnapGrid(boolean snapGrid) {
        this.snapGrid = snapGrid;
    }

    public boolean isDocBoundsActive() {
        return docBoundsActive;
    }

    public void setDocBoundsActive(boolean docBoundsActive) {
        this.docBoundsActive = docBoundsActive;
    }

    public SketchPage addPage() {
        SketchPage page = new SketchPage(this);
        this.pages.add(page);
        setCurrentPage(index+1);
        return page;
    }

    public int getCurrentPageIndex() {
        return index;
    }

    public void removePage(SketchPage page) {
        this.pages.remove(page);
    }

    public void insertPage(int index, SketchPage page) {
        this.pages.add(index,page);
    }

    public SketchPage duplicate(SketchPage dragItem) {
        SketchPage dupe = new SketchPage(this);
        for(SNode n : dragItem.getNodes()) {
            dupe.add(n.duplicate(null));
        }
        return dupe;
    }

    public void setBackgroundFill(Paint backgroundFill) {
        this.backgroundFill = backgroundFill;
    }

    public Paint getBackgroundFill() {
        return backgroundFill;
    }

    public boolean isPresentation() {
        return presentation;
    }

    public void setPresentation(boolean presentation) {
        this.presentation = presentation;
    }

    public boolean isSnapDocBounds() {
        return snapDocBounds;
    }

    public void setSnapDocBounds(boolean snapDocBounds) {
        this.snapDocBounds = snapDocBounds;
    }

    public boolean isSnapNodeBounds() {
        return snapNodeBounds;
    }

    public void setSnapNodeBounds(boolean snapNodeBounds) {
        this.snapNodeBounds = snapNodeBounds;
    }

    public static class SketchPage extends Page {
        private List<Guideline> guidelines = new ArrayList<Guideline>();
        private List<SNode> model;
        private SketchDocument doc;

        private SketchPage(SketchDocument doc) {
            this.doc = doc;
            model = new ArrayList<SNode>();
        }

        public void remove(SNode node) {
            model.remove(node);
            doc.setDirty(true);
        }

        public void add(SNode node) {
            Util.assertNotNull(node);
            model.add(node);
            doc.setDirty(true);
        }

        public List<SNode> getModel() {
            return model;
        }

        public Iterable<SNode> getNodes() {
            return model;
        }

        public SketchDocument getDocument() {
            return doc;
        }

        public void clear() {
            model.clear();
            doc.setDirty(true);
        }
        public void removeGuideline(Guideline guideline) {
            this.guidelines.remove(guideline);
            getDocument().fireViewDirty();
            EventBus.getSystem().publish(new DocumentEvent(this.getDocument(),DocumentEvent.PageGuidelineRemoved,guideline));
        }

        public Iterable<? extends Guideline> getGuidelines() {
            return guidelines;
        }

        public Guideline createGuideline(double pos, boolean vertical) {
            Guideline g = new Guideline(this,vertical,pos);
            this.guidelines.add(g);
            getDocument().fireDocDirty();
            getDocument().fireViewDirty();
            EventBus.getSystem().publish(new DocumentEvent(this.getDocument(),DocumentEvent.PageGuidelineAdded,g));
            return g;
        }
    }

    public static class Guideline {
        private SketchPage page;
        private boolean vertical;
        private double position;

        public Guideline(SketchPage page, boolean vertical, double position) {
            this.page = page;
            this.vertical = vertical;
            this.position = position;
        }

        public void draw(GFX g) {
            if(vertical) {
                g.setPaint(FlatColor.RED.deriveWithAlpha(0.5));
                g.drawLine(position,0,position, page.getDocument().getHeight());
            } else {
                g.setPaint(FlatColor.RED.deriveWithAlpha(0.5));
                g.drawLine(0,position, page.getDocument().getWidth(),position);
            }
        }

        public boolean isVertical() {
            return vertical;
        }

        public double getPosition() {
            return position;
        }

        public void setPosition(double position) {
            this.position = position;
            EventBus.getSystem().publish(new DocumentEvent(page.getDocument(),DocumentEvent.PageGuidelineMoved,this));
        }
    }
}
