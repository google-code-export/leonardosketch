package org.joshy.sketch.model;

import org.joshy.gfx.event.Event;
import org.joshy.gfx.event.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CanvasDocument<P extends Page> {
    protected List<P> pages = new ArrayList<P>();
    protected int index;

    public static enum LengthUnits {
        Pixels(1.0),
        Inches(96.0),
        Centimeters(96.0*0.393700787);

        private double pixelConversionFactor;

        LengthUnits(double factor) {
            this.pixelConversionFactor = factor;
        }

        public double toPixels(double width) {
            return width* pixelConversionFactor;
        }
    }

    private String title = "Untitled";
    private File file;
    protected boolean dirty;

    private double width = 100;
    private double height = 100;
    private LengthUnits units = LengthUnits.Pixels;


    public CanvasDocument() {
        dirty = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        boolean old = this.dirty;
        this.dirty = dirty;
        if(old != dirty) {
            fireDocDirty();
        }
        fireViewDirty();
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
        fireDocDirty();
        fireViewDirty();
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
        fireDocDirty();
        fireViewDirty();
    }
    
    public LengthUnits getUnits() {
        return units;
    }

    public void setUnits(LengthUnits units) {
        this.units = units;
    }

    private void fireDocDirty() {
        EventBus.getSystem().publish(new DocumentEvent(this,isDirty()));
    }

    protected void fireViewDirty() {
        EventBus.getSystem().publish(new DocumentEvent(this,DocumentEvent.ViewDirty));
    }


    public static class DocumentEvent extends Event {
        public static final EventType Dirty = new EventType("DocumentDirty");
        public static final EventType ViewDirty = new EventType("ViewDirty");
        public static final EventType PageChanged = new EventType("PageChanged");
        private boolean isDirty;
        private CanvasDocument document;

        public DocumentEvent(CanvasDocument document, boolean isDirty) {
            super(Dirty,document);
            this.document = document;
            this.isDirty = isDirty;
        }

        public DocumentEvent(CanvasDocument document, EventType type) {
            super(type);
            this.document = document;
        }

        public CanvasDocument getDocument() {
            return document;
        }
    }

    public List<P> getPages() {
        return pages;
    }

    public P getCurrentPage() {
        return pages.get(index);
    }

    public void setCurrentPage(int i) {
        index = i;
        EventBus.getSystem().publish(new DocumentEvent(this,DocumentEvent.PageChanged));
    }

}
