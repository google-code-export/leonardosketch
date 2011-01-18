package org.joshy.sketch.modes.pixel;

import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.TabPanel;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.NewPixelDocAction;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.canvas.PixelCanvas;
import org.joshy.sketch.controls.Menubar;
import org.joshy.sketch.controls.ToggleGroup;
import org.joshy.sketch.controls.ToolbarButton;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.pixel.model.PixelDoc;
import org.joshy.sketch.tools.PixelSetTool;
import org.joshy.sketch.util.BiList;

import java.io.IOException;

/**
 * A context for bitmap/pixel documents.
 */
public class PixelDocContext extends DocContext<PixelCanvas, PixelDoc> {
    private PixelSetTool brush;
    private VFlexBox toolbar;
    private PixelCanvas canvas;
    public PixelTool selectedTool;
    protected BiList<Button, PixelTool> tools = new BiList<Button, PixelTool>();
    private PixelTool pencilTool;
    private PixelTool selectionTool;
    private ToggleGroup group;

    public PixelDocContext(Main main, PixelModeHelper pixelModeHelper) {
        super(main,pixelModeHelper);
        canvas =  new PixelCanvas(this);
    }

    @Override
    public PixelDoc getDocument() {
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
        toolbar = new VFlexBox();
        //toolbar = new PixelToolbar(this);
        //brush = new PixelSetTool(this);
        //selectedTool = brush;
        pencilTool = new PencilTool(this);
        selectionTool = new SelectionTool(this);
        tools.add(new ToolbarButton(Main.getIcon("cr22-action-14_pencil.png")),pencilTool);
        tools.add(new ToolbarButton(Main.getIcon("cr22-action-tool_rect_selection.png")),selectionTool);

        group = new ToggleGroup();
        for(Button button : tools.keys()) {
            group.add(button);
            toolbar.add(button);
        }
        EventBus.getSystem().addListener(ActionEvent.Action, new Callback<ActionEvent>(){
            public void call(ActionEvent event) {
                if(event.getSource() == group) {
                    Button btn = group.getSelectedButton();
                    PixelTool tool = tools.getValue(btn);
                    setSelectedTool(tool);
                }
            }
        });
        selectedTool = pencilTool;
        pencilTool.enable();
        selectButtonForTool(pencilTool);

    }
    private void selectButtonForTool(PixelTool tool) {
        group.setSelectedButton(tools.getKey(tool));
    }
    private void setSelectedTool(PixelTool tool) {
        selectedTool.disable();
        selectedTool = tool;
        selectedTool.enable();
    }

    public PixelTool getSelectedTool() {
        return this.selectedTool;
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
    public void createAfterEditMenu(Menubar menubar) {
        
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
    public void setDocument(PixelDoc doc) {
        this.canvas.setDocument(doc);
    }

    public PixelToolbar getPixelToolbar() {
        return null;
    }
}
