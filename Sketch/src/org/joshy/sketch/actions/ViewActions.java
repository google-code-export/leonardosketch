package org.joshy.sketch.actions;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Transform;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.stage.Stage;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.modes.vector.VectorDocContext;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 8, 2010
 * Time: 6:30:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewActions {
    private static Callback undoFullscreen;

    public static class ZoomInAction extends SAction {
        private DocContext context;

        public ZoomInAction(DocContext context) {
            this.context = context;
        }

        @Override
        public void execute() {
            context.getCanvas().setScale(context.getCanvas().getScale()*2.0);
        }
    }

    public static class ZoomOutAction extends SAction {
        private DocContext context;

        public ZoomOutAction(DocContext context) {
            this.context = context;
        }

        @Override
        public void execute() {
            context.getCanvas().setScale(context.getCanvas().getScale()/2.0);
        }
    }

    public static class ZoomResetAction extends SAction {
        private DocContext context;

        public ZoomResetAction(DocContext context) {
            this.context = context;
        }

        @Override
        public void execute() {
            context.getCanvas().setScale(1);
//            context.getCanvas().setPanX(0);
//            context.getCanvas().setPanY(0);
        }
    }

    public static class ShowGridAction extends ToggleAction {
        private VectorDocContext context;

        public ShowGridAction(VectorDocContext context) {
            this.context = context;
        }

        @Override
        public boolean getToggleState() {
            return context.getDocument().isGridActive();
        }

        @Override
        public void setToggleState(boolean toggleState) {
            context.getDocument().setGridActive(toggleState);
            context.redraw();
        }
    }

    public static class SnapGridAction extends ToggleAction {
        private VectorDocContext context;

        public SnapGridAction(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public boolean getToggleState() {
            return context.getDocument().isSnapGrid();
        }

        @Override
        public void setToggleState(boolean toggleState) {
            context.getDocument().setSnapGrid(toggleState);
            context.redraw();
        }
    }

    public static class SnapDocBoundsAction extends ToggleAction {
        private VectorDocContext context;

        public SnapDocBoundsAction(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public boolean getToggleState() {
            return context.getDocument().isSnapDocBounds();
        }

        @Override
        public void setToggleState(boolean toggleState) {
            context.getDocument().setSnapDocBounds(toggleState);
            context.redraw();
        }
    }

    public static class SnapNodeBoundsAction extends ToggleAction {
        private VectorDocContext context;

        public SnapNodeBoundsAction(VectorDocContext context) {
            super();
            this.context = context;
        }

        @Override
        public boolean getToggleState() {
            return context.getDocument().isSnapNodeBounds();
        }

        @Override
        public void setToggleState(boolean toggleState) {
            context.getDocument().setSnapNodeBounds(toggleState);
            context.redraw();
        }
    }

    public static class ShowDocumentBounds extends ToggleAction {
        private VectorDocContext context;

        public ShowDocumentBounds(VectorDocContext context) {
            this.context = context;
        }

        @Override
        public boolean getToggleState() {
            return context.getDocument().isDocBoundsActive();
        }

        @Override
        public void setToggleState(boolean toggleState) {
            context.getDocument().setDocBoundsActive(toggleState);
            context.redraw();
        }
    }

    public static class ToggleFullScreen extends SAction {
        private DocContext context;

        public ToggleFullScreen(DocContext context) {
            this.context = context;
        }

        @Override
        public void execute() {
            if(undoFullscreen != null) {
                undoFullscreen.call(null);
                undoFullscreen = null;
                return;
            }
            
            final Stage stage = context.getStage();
            GraphicsDevice device = GraphicsEnvironment.
                    getLocalGraphicsEnvironment().
                    getDefaultScreenDevice();
            if (device.isFullScreenSupported()) {
                stage.setUndecorated(true);
                JFrame frame = (JFrame) stage.getNativeWindow();
                device.setFullScreenWindow(frame);
            } else {
                System.err.println("Full screen not supported");
            }
            undoFullscreen = new Callback(){
                public void call(Object event) {
                    GraphicsDevice device = GraphicsEnvironment.
                            getLocalGraphicsEnvironment().
                            getDefaultScreenDevice();
                    device.setFullScreenWindow(null);
                    stage.setUndecorated(false);
                }
            };
        }
    }

    public static class ToggleFullScreenMenubar extends SAction {
        private DocContext context;

        public ToggleFullScreenMenubar(DocContext context) {
            this.context = context;
        }

        @Override
        public void execute() {
            if(undoFullscreen != null) {
                undoFullscreen.call(null);
                undoFullscreen = null;
                return;
            }

            final Stage stage = context.getStage();
            
            final JFrame f2 = (JFrame) stage.getNativeWindow();
            final Point oldLocation = f2.getLocation();
            final Dimension oldSize = f2.getSize();

            Toolkit tk = Toolkit.getDefaultToolkit();
            Insets insets = tk.getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
            Dimension size = tk.getScreenSize();
            stage.setUndecorated(true);
            final JFrame frame = (JFrame) stage.getNativeWindow();
            undoFullscreen = new Callback() {
                public void call(Object event) {
                    stage.setUndecorated(false);
                    frame.setLocation(oldLocation);
                    frame.setSize(oldSize);
                }
            };

            frame.setLocation(0+insets.left,0+insets.top);
            frame.setSize(size.width-insets.left-insets.right,size.height-insets.top-insets.bottom);
        }
    }

    public static class NewView extends SAction {
        private DocContext context;

        public NewView(DocContext context) {
            this.context = context;
        }

        @Override
        public void execute() {
            Stage stage = Stage.createStage();
            stage.setTitle("View 1");
            stage.setContent(new ViewPanel());
            stage.setWidth(300);
            stage.setHeight(300);
        }
        private class ViewPanel extends Panel {
            private ViewPanel() {
                EventBus.getSystem().addListener(CanvasDocument.DocumentEvent.ViewDirty, new Callback<CanvasDocument.DocumentEvent>() {
                    public void call(CanvasDocument.DocumentEvent event) {
                        setDrawingDirty();
                    }
                });
            }

            @Override
            protected void drawSelf(GFX gfx) {
                gfx.setPaint(FlatColor.WHITE);
                gfx.fillRect(0,0,getWidth(),getHeight());
                double w = context.getDocument().getWidth();
                double h = context.getDocument().getHeight();
                double s1 = getWidth()/w;
                double s2 = getHeight()/h;
                double s = Math.min(s1,s2);
                gfx.scale(s,s);
                //draw all nodes
                SketchDocument doc = (SketchDocument) context.getDocument();
                for(SNode node : doc.getCurrentPage().getNodes()) {
                    NewView.this.draw(gfx,node);
                }
                //draw border
                gfx.setPaint(FlatColor.BLACK);
                gfx.drawRect(0,0,doc.getWidth(),doc.getHeight());
                gfx.scale(1/s,1/s);

            }
        }
        private void draw(GFX g, SNode node) {
            g.translate(node.getTranslateX(),node.getTranslateY());
            g.scale(node.getScaleX(),node.getScaleY());
            g.rotate(node.getRotate(), Transform.Z_AXIS);
            if(node instanceof SelfDrawable) {
                ((SelfDrawable)node).draw(g);
            }
            if(node instanceof Button9) {
                draw(g,(Button9)node);
            }
            g.rotate(-node.getRotate(), Transform.Z_AXIS);
            g.scale(1/node.getScaleX(),1/node.getScaleY());
            g.translate(-node.getTranslateX(),-node.getTranslateY());
        }
    }
}
