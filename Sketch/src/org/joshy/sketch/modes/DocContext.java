package org.joshy.sketch.modes;

import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.DisclosurePanel;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.node.layout.StackPanel;
import org.joshy.gfx.node.layout.TabPanel;
import org.joshy.gfx.stage.Stage;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.UndoManager;
import org.joshy.sketch.actions.io.BaseExportAction;
import org.joshy.sketch.actions.pages.PageListPanel;
import org.joshy.sketch.canvas.DocumentCanvas;
import org.joshy.sketch.controls.Menu;
import org.joshy.sketch.controls.Menubar;
import org.joshy.sketch.controls.NotificationIndicator;
import org.joshy.sketch.model.CanvasDocument;

import java.io.IOException;
import java.util.Properties;

/**
 * Encapsulates all state and context of an open document being edited. It has references
 * to the doc, all parts of the UI, the window, and any doc type specific state.
 *
 * You will never use this directly but instead use a doc type specific subclass
 */
public abstract class DocContext<C extends DocumentCanvas, D extends CanvasDocument> {
    public Main main;
    public StackPanel stackPanel;
    public PageListPanel pageList;
    private UndoManager undoManager;
    public Menu symbolMenu;
    public Menu pageMenu;
    public Menubar menubar;
    public Panel mainPanel;
    private Stage stage;
    protected DocModeHelper modeHelper;
    public Menu windowJMenu;
    public DisclosurePanel sidebarContainer;
    private NotificationIndicator notificationIndicator;
    private BaseExportAction lastExportAction;
    private Menu fileMenu;

    public DocContext(Main main, DocModeHelper mode) {
        this.main = main;
        this.modeHelper = mode;
        stage = Stage.createFullscreenEnabledStage();
        undoManager = new UndoManager(this);
        notificationIndicator = new NotificationIndicator();
    }


    public void addNotification(String notification) {
        this.notificationIndicator.addNotification(notification);
    }

    public Properties getSettings() {
        return getMain().settings;
    }

    public Stage getStage() {
        return stage;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public abstract D getDocument();

    public abstract void redraw();


    public abstract C getCanvas();

    public abstract void setupTools() throws Exception;

    public abstract void setupSidebar();

    public abstract void setupActions();

    public abstract void setupPalettes() throws IOException;

    public abstract void setupPopupLayer();

    public abstract void createAfterEditMenu(Menubar menubar);

    public abstract Control getToolbar();

    public abstract TabPanel getSidebar();

    public Main getMain() {
        return main;
    }

    public abstract void setDocument(D doc);

    public Node getNotificationIndicator() {
        return this.notificationIndicator;
    }

    public void setLastExportAction(BaseExportAction action) {
        this.lastExportAction = action;
    }

    public BaseExportAction getLastExportAction() {
        return lastExportAction;
    }

    public void setFileMenu(Menu fileMenu) {
        this.fileMenu = fileMenu;
    }


    public Menu getFileMenu() {
        return fileMenu;
    }
}
