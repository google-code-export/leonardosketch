package org.joshy.sketch.modes;

import org.joshy.gfx.node.control.Button;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.controls.Menu;
import org.joshy.sketch.model.CanvasDocument;
import org.joshy.sketch.tools.CanvasTool;
import org.joshy.sketch.util.BiList;

/**
 * The base class for all editing modes. You will always use a doc type specific subclassn
 */
public abstract class DocModeHelper<D extends DocContext> {
    protected Main main;

    protected DocModeHelper(Main main) {
        this.main = main;
    }

    public abstract void setupToolbar(BiList<Button, CanvasTool> tools, Main main, D ctx) throws Exception;

    public abstract boolean isPageListVisible();

    public abstract Menu buildPageMenu(D context);

    public abstract CharSequence getModeName();

    public abstract D createDocContext(Main main);

    public abstract CanvasDocument createNewDoc();

    public abstract SAction getNewDocAction(Main main);

    public abstract void addCustomExportMenus(Menu exportMenu, DocContext context);
}
