package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.TabPanel;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.NewPixelDocAction;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.canvas.PixelCanvas;
import org.joshy.sketch.model.PixelDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.tools.PixelSetTool;

import javax.swing.*;
import java.io.IOException;

/**
 * A context for bitmap/pixel documents.
 */
public class PixelDocContext extends DocContext<PixelCanvas, PixelDocument> {
    private PixelSetTool brush;
    private PixelToolbar toolbar;
    private PixelCanvas canvas;
    public PixelSetTool selectedTool;

    public PixelDocContext(Main main, PixelModeHelper pixelModeHelper) {
        super(main,pixelModeHelper);
        canvas =  new PixelCanvas(this);
    }

    @Override
    public PixelDocument getDocument() {
        return canvas.getDocument();
    }

    @Override
    public void redraw() {
        canvas.redraw();
    }

    @Override
    public PixelCanvas getCanvas() {
        return canvas;
    }

    @Override
    public void setupTools() throws Exception {
        toolbar = new PixelToolbar(this);
        brush = new PixelSetTool(this);
        selectedTool = brush;
    }

    public SAction getNewDocAction() {
        return new NewPixelDocAction(main);
    }

    @Override
    public void setupSidebar() {

    }

    @Override
    public void setupActions() {

    }

    @Override
    public void setupPalettes() throws IOException {

    }

    @Override
    public void setupPopupLayer() {

    }

    @Override
    public void createAfterEditMenu(JMenuBar menubar) {
        
    }

    @Override
    public Control getToolbar() {
        return toolbar;
    }

    @Override
    public TabPanel getSidebar() {
        return null;
    }

    @Override
    public void setDocument(PixelDocument doc) {
        this.canvas.setDocument(doc);
    }

    public PixelToolbar getPixelToolbar() {
        return toolbar;
    }
}
