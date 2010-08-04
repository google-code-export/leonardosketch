package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.node.control.Button;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.NewPixelDocAction;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.model.CanvasDocument;
import org.joshy.sketch.model.PixelDocument;
import org.joshy.sketch.modes.DocModeHelper;
import org.joshy.sketch.tools.CanvasTool;
import org.joshy.sketch.util.BiList;

import javax.swing.*;

/**
 * The mode helper for pixel documents.
 */
public class PixelModeHelper extends DocModeHelper<PixelDocContext> {

    public PixelModeHelper(Main main) {
        super(main);
    }

    @Override
    public void setupToolbar(BiList<Button, CanvasTool> tools, Main main, PixelDocContext ctx) throws Exception {
    }

    @Override
    public boolean isPageListVisible() {
        return false;
    }

    @Override
    public JMenu buildPageMenu(PixelDocContext context) {
        return null;
    }

    @Override
    public String getModeName() {
        return "Bitmap";
    }

    @Override
    public PixelDocContext createDocContext(Main main) {
        return new PixelDocContext(main, this);
    }

    @Override
    public CanvasDocument createNewDoc() {
        return new PixelDocument(640,480);
    }

    @Override
    public SAction getNewDocAction(Main main) {
        return new NewPixelDocAction(main);
    }
}