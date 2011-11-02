package org.joshy.sketch.model;

import org.joshy.gfx.event.Event;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.util.u;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CanvasDocument<P extends Page> {
    protected List<P> pages = new ArrayList<P>();
    protected int index;
    private Map<String,String> props = new HashMap<String,String>();
    private boolean rulersVisible = true;
    private boolean pagesVisible = false;

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
    
    public boolean isRulersVisible() {
        return this.rulersVisible;
    }

    public void setRulersVisible(boolean visible) {
        this.rulersVisible = visible;
        fireViewDirty();
    }

    public void setPagesVisible(boolean pagesVisible) {
        this.pagesVisible = pagesVisible;
    }

    public boolean isPagesVisible() {
        return pagesVisible;
    }

    public LengthUnits getUnits() {
        return units;
    }

    public void setUnits(LengthUnits units) {
        this.units = units;
    }

    public String getStringProperty(String key) {
        return props.get(key);
    }

    public void setStringProperty(String key, String value) {
        props.put(key,value);
    }

    public Map getProperties() {
        return props;
    }

    public void close() {
        u.p("publishing a closing event");
        EventBus.getSystem().publish(new DocumentEvent(this,DocumentEvent.Closing));
    }

    protected void fireDocDirty() {
        EventBus.getSystem().publish(new DocumentEvent(this,isDirty()));
    }

    protected void fireViewDirty() {
        EventBus.getSystem().publish(new DocumentEvent(this,DocumentEvent.ViewDirty));
    }


    public static class DocumentEvent extends Event {
        public static final EventType Dirty = new EventType("DocumentDirty");
        public static final EventType Closing = new EventType("DocumentClosing");
        public static final EventType ViewDirty = new EventType("ViewDirty");
        public static final EventType PageChanged = new EventType("PageChanged");
        public static final EventType PageGuidelineAdded = new EventType("PageGuidelineAdded");
        public static final EventType PageGuidelineMoved = new EventType("PageGuidelineMoved");
        public static final EventType PageGuidelineRemoved = new EventType("PageGuidelineRemoved");
        private boolean isDirty;
        private CanvasDocument document;
        private Object target;

        public DocumentEvent(CanvasDocument document, boolean isDirty) {
            super(Dirty,document);
            this.document = document;
            this.isDirty = isDirty;
        }

        public DocumentEvent(CanvasDocument document, EventType type) {
            super(type,document);
            this.document = document;
        }

        public DocumentEvent(CanvasDocument document, EventType type, Object target) {
            super(type,document);
            this.document = document;
            this.target = target;
        }

        public CanvasDocument getDocument() {
            return document;
        }

        public Object getTarget() {
            return target;
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
