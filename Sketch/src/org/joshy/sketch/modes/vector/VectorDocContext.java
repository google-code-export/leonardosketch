package org.joshy.sketch.modes.vector;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.node.layout.TabPanel;
import org.joshy.gfx.node.layout.VBox;
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.*;
import org.joshy.sketch.actions.flickr.FlickrPanel;
import org.joshy.sketch.actions.symbols.CreateSymbol;
import org.joshy.sketch.actions.symbols.SymbolManager;
import org.joshy.sketch.actions.symbols.SymbolPanel;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.canvas.SketchCanvas;
import org.joshy.sketch.controls.*;
import org.joshy.sketch.model.CanvasDocument;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SPath;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.tools.*;
import org.joshy.sketch.util.BiList;

import javax.swing.*;
import java.io.IOException;

/**
 * A doc context for all vector based doc types, including drawing and presentation
 */
public class VectorDocContext extends DocContext<SketchCanvas, SketchDocument> {
    private DrawTextTool drawTextTool;
    private DrawPathTool drawPathTool;
    private VBox toolbar;
    private TabPanel sideBar;
    private Panel flickrPanel;
    private SketchCanvas canvas;
    protected ToggleGroup group;
    protected BiList<Button, CanvasTool> tools = new BiList<Button, CanvasTool>();
    protected EditResizableShapeTool editResizableShapeTool;

    public RectPropsPalette propsPalette;
    public CanvasTool selectedTool;
    public SelectMoveTool moveRectTool;
    public FloatingPropertiesPanel propPanel;
    public DeleteSelectedNodeAction deleteSelectedNodeAction;
    public NodeActions.DuplicateNodesAction duplicateNodeAction;

    public VectorDocContext(Main main, VectorModeHelper helper) {
        super(main,helper);
        canvas =  new SketchCanvas(this);
    }

    public Selection getSelection() {
        return canvas.selection;
    }

    public SAction getDeleteSelectedNodeAction() {
        return deleteSelectedNodeAction;
    }

    public FloatingPropertiesPanel getPropPanel() {
        return propPanel;
    }

    public SAction getDuplicateNodeAction() {
        return duplicateNodeAction;
    }

    public SymbolManager getSymbolManager() {
        return getMain().symbolManager;
    }

    public CanvasTool getEditResizableShapeTool() {
        return this.editResizableShapeTool;
    }

    public void switchToTextEdit(SNode text) {
        drawTextTool.startEditing(text);
        selectButtonForTool(drawTextTool);
    }

    public void switchToPathEdit(SPath path) {
        selectButtonForTool(drawPathTool);
        drawPathTool.startEditing(path);
    }
    public void setSelectedTool(CanvasTool tool) {
        selectedTool.disable();
        selectedTool = tool;
        selectedTool.enable();
    }

    public void selectButtonForTool(CanvasTool tool) {
        group.setSelectedButton(tools.getKey(tool));
    }

    public void releaseControl() {
        selectButtonForTool(moveRectTool);
    }


    @Override
    public SketchDocument getDocument() {
        return canvas.getDocument();
    }

    @Override
    public void redraw() {
        canvas.redraw();
    }

    @Override
    public SketchCanvas getCanvas() {
        return canvas;
    }

    public SketchCanvas getSketchCanvas() {
        return canvas;
    }

    public void setupTools() throws Exception {
        toolbar = new VBox();
        moveRectTool = new SelectMoveTool(this);
        drawTextTool = new DrawTextTool(this);
        drawPathTool = new DrawPathTool(this);
        editResizableShapeTool = new EditResizableShapeTool(this);
        tools.add(new ToolbarButton(Main.getIcon("cr22-action-14_select.png")),moveRectTool);
        tools.add(new ToolbarButton(Main.getIcon("cr22-action-14_text.png")),drawTextTool);
        tools.add(new ToolbarButton(Main.getIcon("cr22-action-14_insertknots.png")),drawPathTool);

        modeHelper.setupToolbar(tools, getMain(),this);

        group = new ToggleGroup();
        for(Button button : tools.keys()) {
            group.add(button);
            toolbar.add(button);
        }
        EventBus.getSystem().addListener(ActionEvent.Action, new Callback<ActionEvent>(){
            public void call(ActionEvent event) {
                if(event.getSource() == group) {
                    Button btn = group.getSelectedButton();
                    CanvasTool tool = tools.getValue(btn);
                    setSelectedTool(tool);
                }
            }
        });
        selectedTool = moveRectTool;
        moveRectTool.enable();
        selectButtonForTool(moveRectTool);

    }

    public void setupSidebar() {
        this.sideBar = new TabPanel();
        this.symbolPanel = new SymbolPanel(getMain().symbolManager, this);
        this.sideBar.add("Symbols",this.symbolPanel);
        this.flickrPanel = new FlickrPanel(this);
        this.flickrPanel.setFill(FlatColor.GREEN);
        this.sideBar.add("Flickr Search", this.flickrPanel);
        this.flickrPanel.setVisible(false);
    }

    @Override
    public void setupActions() {
        duplicateNodeAction = new NodeActions.DuplicateNodesAction(this,false);
        deleteSelectedNodeAction = new DeleteSelectedNodeAction(this);
    }

    @Override
    public void setupPalettes() throws IOException {
        propPanel = new FloatingPropertiesPanel(getMain(),this);
        propsPalette = new RectPropsPalette(canvas);
        propsPalette.setVisible(false);
        propsPalette.setTranslateX(400);
        propsPalette.setTranslateY(50);
    }

    @Override
    public void setupPopupLayer() {
        getStage().getPopupLayer().add(propPanel);
        getStage().getPopupLayer().add(propsPalette);        
    }

    @Override
    public void createAfterEditMenu(JMenuBar menubar) {
        //node menu
        VectorDocContext context = this;
        menubar.add(new org.joshy.sketch.controls.Menu().setTitle("Node")
                .addItem("Raise to Top",      "shift CLOSE_BRACKET", new NodeActions.RaiseTopSelectedNodeAction(this))
                .addItem("Raise Node",        "CLOSE_BRACKET", new NodeActions.RaiseSelectedNodeAction(this))
                .addItem("Lower Node",        "OPEN_BRACKET",        new NodeActions.LowerSelectedNodeAction(this))
                .addItem("Lower to Bottom",   "shift OPEN_BRACKET",  new NodeActions.LowerBottomSelectedNodeAction(this))
                .separator()
                .addItem("Align Top",               new NodeActions.AlignTop(context))
                .addItem("Align Bottom",            new NodeActions.AlignBottom(context))
                .addItem("Align Left",              new NodeActions.AlignLeft(context))
                .addItem("Align Right",             new NodeActions.AlignRight(context))
                .addItem("Align Center Horizontal", new NodeActions.AlignCenterH(context))
                .addItem("Align Center Vertical",   new NodeActions.AlignCenterV(context))
                .separator()
                .addItem("Same Width",              new NodeActions.SameWidth(context,true))
                .addItem("Same Height",             new NodeActions.SameWidth(context,false))
                .separator()
                .addItem("Group Selection",   "G",       new NodeActions.GroupSelection(context))
                .addItem("Ungroup Selection", "shift G", new NodeActions.UngroupSelection(context))
                .separator()
                .addItem("Create Symbol from Selection", new CreateSymbol(context))
                .addItem("Create Resizable Shape from Selection", new CreateResizableShape(context))
                .addItem("Edit Resizable Shape", new CreateResizableShape.Edit(context))
                .addItem("Duplicate", new NodeActions.DuplicateNodesAction(context,true))
                .separator()
                .addItem("Reset Transforms", new NodeActions.ResetTransforms(context))
                .createJMenu());
        menubar.add(new Menu().setTitle("Path")
                .addItem("Flip Horizontal", new PathActions.Flip(context,true))
                .addItem("Flip Vertical",   new PathActions.Flip(context,false))
                .addItem("Rotate 90¿ Clockwise", new PathActions.RotateClockwise(context,Math.PI/2))
                .addItem("Rotate 90¿ Counter Clockwise", new PathActions.RotateClockwise(context,-Math.PI/2))
                .addItem("Free Rotate", new PathActions.Rotate(context))
                .addItem("Scale Trasnsform", new PathActions.Scale(context))
                .createJMenu());
        menubar.add(new Menu().setTitle("Document")
                .addItem("Set Document Size", new DocumentActions.SetDocumentSize(this))
                .createJMenu());

        rebuildSymbolMenu(menubar);

    }

    @Override
    public Control getToolbar() {
        return toolbar;
    }

    @Override
    public TabPanel getSidebar() {
        return this.sideBar;
    }

    @Override
    public void setDocument(SketchDocument doc) {
        this.canvas.setDocument(doc);
        final JFrame frame = (JFrame) getStage().getNativeWindow();
        EventBus.getSystem().addListener(getDocument(), CanvasDocument.DocumentEvent.Dirty, new Callback<CanvasDocument.DocumentEvent>() {
            public void call(CanvasDocument.DocumentEvent event) {
                frame.getRootPane().putClientProperty("Window.documentModified", event.getDocument().isDirty());
            }
        });
        frame.getRootPane().putClientProperty("Window.documentModified", getDocument().isDirty());
    }


    private void rebuildSymbolMenu(final JMenuBar menubar) {
        VectorDocContext context = this;
        if(context.symbolMenuJMenu != null) {
            menubar.remove(context.symbolMenuJMenu);
        }
        context.symbolMenu = new Menu().setTitle("Symbol Sets");
        context.symbolMenu.addItem("Create New Set", new SAction(){
            @Override
            public void execute() {
                String value = StandardDialog.showEditText("Name for new symbol set","untitled");
                if(value != null) {
                    SymbolManager.SymbolSet set = getMain().symbolManager.createNewSet(value);
                    getMain().symbolManager.setCurrentSet(set);
                    redraw();
                    rebuildSymbolMenu(menubar);
                }
            }
        });
        context.symbolMenu.separator();
        for(final SymbolManager.SymbolSet set : getMain().symbolManager.sets.values()) {
            context.symbolMenu.addItem(set.file.getName(), new SAction(){
                @Override
                public void execute() {
                    getMain().symbolManager.setCurrentSet(set);
                    redraw();
                }
            });
        }
        context.symbolMenuJMenu = context.symbolMenu.createJMenu();
        menubar.add(context.symbolMenuJMenu);
    }

    public Panel getFlickrPanel() {
        return flickrPanel;
    }
    
}
