package org.joshy.sketch.modes.preso;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Transform;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.control.Focusable;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.model.Button9;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SelfDrawable;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

import javax.swing.*;
import java.awt.*;

/**
 * Start viewing a slideshow in full screen.
 * Begins with the currently selected page.
 */
public class ViewSlideshowAction extends SAction {
    private Stage stage;
    private VectorDocContext context;

    public ViewSlideshowAction(VectorDocContext context) {
        super();
        this.context = context;
    }

    @Override
    public void execute() {
        stage = Stage.createStage();
        stage.setContent(new SlideshowView(context.getDocument()));
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
    }

    private class SlideshowView extends Control implements Focusable {
        private SketchDocument document;

        public SlideshowView(final SketchDocument document) {
            super();
            this.document = document;
            EventBus.getSystem().addListener(this, KeyEvent.KeyPressed, new Callback<KeyEvent>(){
                public void call(KeyEvent event) {
                    if(event.getKeyCode() == KeyEvent.KeyCode.KEY_RIGHT_ARROW) {
                        u.p("right");
                        if(document.getCurrentPageIndex() < document.getPages().size()-1) {
                            document.setCurrentPage(document.getCurrentPageIndex()+1);
                            setDrawingDirty();
                        }
                    }
                    if(event.getKeyCode() == KeyEvent.KeyCode.KEY_LEFT_ARROW) {
                        u.p("left");
                        if(document.getCurrentPageIndex() > 0) {
                            document.setCurrentPage(document.getCurrentPageIndex()-1);
                            setDrawingDirty();
                        }
                    }
                    if(event.getKeyCode() == KeyEvent.KeyCode.KEY_ESCAPE) {
                        u.p("escape");
                        GraphicsDevice device = GraphicsEnvironment.
                                getLocalGraphicsEnvironment().
                                getDefaultScreenDevice();
                        device.setFullScreenWindow(null);
                        stage.setUndecorated(false);
                        stage.hide();
                    }
                }
            });
            Core.getShared().getFocusManager().setFocusedNode(this);
        }

        @Override
        public void doLayout() {

        }

        @Override
        public void doSkins() {

        }

        @Override
        public void draw(GFX gfx) {
            gfx.setPaint(document.getBackgroundFill());
            gfx.fillRect(0,0,getWidth(),getHeight());
            double w = document.getWidth();
            double h = document.getHeight();
            double s = getWidth()/w;
            for(SNode node : document.getCurrentPage().getNodes()) {
                gfx.scale(s,s);
                draw(gfx,node);
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

        public boolean isFocused() {
            return true;
        }
    }
}
