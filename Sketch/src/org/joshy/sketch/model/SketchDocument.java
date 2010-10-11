package org.joshy.sketch.model;

import org.joshy.gfx.draw.FlatColor;

import java.util.ArrayList;
import java.util.List;

public class SketchDocument extends CanvasDocument<SketchDocument.SketchPage> {
    private boolean gridActive = true;
    private double gridWidth = 20;
    private double gridHeight = 20;
    private boolean snapGrid;
    private boolean docBoundsActive = true;
    private FlatColor backgroundFill = FlatColor.WHITE;
    private boolean presentation;
    private boolean snapDocBounds;
    private boolean snapNodeBounds;

    public SketchDocument() {
        setWidth(600);
        setHeight(800);
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
        for(SNode n : dragItem.model) {
            dupe.add(n.duplicate(null));
        }
        return dupe;
    }

    public void setBackgroundFill(FlatColor backgroundFill) {
        this.backgroundFill = backgroundFill;
    }

    public FlatColor getBackgroundFill() {
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
        public List<SNode> model;
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
            model.add(node);
            doc.setDirty(true);
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
    }
}
