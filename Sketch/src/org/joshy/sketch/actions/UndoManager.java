package org.joshy.sketch.actions;

import org.joshy.gfx.animation.AnimationDriver;
import org.joshy.gfx.animation.CompoundAnimation;
import org.joshy.gfx.animation.KeyFrameAnimator;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.util.u;
import org.joshy.sketch.modes.DocContext;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: May 17, 2010
 * Time: 3:33:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class UndoManager {
    private LinkedList<UndoableAction> actionStack;
    private int index;
    private DocContext context;

    public UndoManager(DocContext context) {
        this.context = context;
        actionStack = new LinkedList<UndoableAction>();
        index = -1;
    }

    public void pushAction(UndoableAction act) {
        index++;
//        u.p("adding at index: " + index);
        actionStack.add(index,act);
        int start = (index+1);
        int end = actionStack.size();
//        u.p("removing "+start+" to "+end);
        if(start <= end) {
            actionStack.subList(start,end).clear();
        }
        index = actionStack.size()-1;
        //dump();
    }

    public void dump() {
        u.p("---- dump -----");
        for(UndoableAction a : actionStack) {
            u.p("   undoable: " + a.getName());
        }
    }

    public static interface UndoableAction {
        public void executeUndo();
        public void executeRedo();
        public String getName();
    }

    public void stepBackwards() {
//        u.p("backwards: getting index: " + index);
        UndoableAction action = actionStack.get(index);
        action.executeUndo();
        index--;
        //dump();
        context.getUndoOverlay().showIndicator("Undoing: " + action.getName());
    }

    private void stepForwards() {
        index++;
//        u.p("forwards: getting index:  " + index);
        UndoableAction action = actionStack.get(index);
        action.executeRedo();
        dump();
        context.getUndoOverlay().showIndicator("Redoing: " + action.getName());
    }

    private boolean canUndo() {
        return (index >= 0);
    }

    private boolean canRedo() {
        return (index < actionStack.size()-1);
    }

    public static class UndoAction extends SAction {
        private DocContext context;

        public UndoAction(DocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            if(context.getUndoManager().canUndo()) {
                context.getUndoManager().stepBackwards();
            }
        }
    }

    public static class RedoAction extends SAction {
        private DocContext context;

        public RedoAction(DocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            if(context.getUndoManager().canRedo()) {
                context.getUndoManager().stepForwards();
            }
        }
    }

    public static class UndoOverlay extends Control {
        public String indicationString;
        public double indicatorStringOpacity;
        private CompoundAnimation indicatorStringAnim;

        @Override
        public void doLayout() {
        }

        @Override
        public void doPrefLayout() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void doSkins() {
        }

        @Override
        public void draw(GFX g) {
            if(indicationString != null) {
                Font font = Font.name("Arial").size(30).resolve();
                g.setPaint(new FlatColor(0.5,0.5,0.5,indicatorStringOpacity/2.0));
                double y = 30;
                double h = font.calculateHeight(indicationString);
                g.fillRoundRect(20-10, getHeight()-y-h-100, font.calculateWidth(indicationString)+20, h+20, 10,10);
                g.setPaint(new FlatColor(0,0,0,indicatorStringOpacity));
                g.drawText(indicationString, font ,20, getHeight()-y-100);
            }
        }

        public void showIndicator(String indicationString) {
            this.indicationString = indicationString;
            if(indicatorStringAnim != null && indicatorStringAnim.isRunning()) {
                indicatorStringAnim.stop();
            }

            setIndicatorStringOpacity(1.0);
            AnimationDriver.start(
                KeyFrameAnimator.create(this,"indicatorStringOpacity")
                    .keyFrame(0,1.0)
                    .keyFrame(1,1.0)
                    .keyFrame(3,0.0)
                    .doAfter(new Callback(){
                        public void call(Object event) {
                            showIndicator(null);
                        }
                    })
            );
            //indicatorStringAnim = new SequentialAnimation().add(kf);
            //indicatorStringAnim.start();
            setDrawingDirty();
        }

        public double getIndicatorStringOpacity() {
            return this.indicatorStringOpacity;
        }

        public void setIndicatorStringOpacity(double indicatorStringOpacity) {
            this.indicatorStringOpacity = indicatorStringOpacity;
            setDrawingDirty();
        }

        @Override
        public Bounds getInputBounds() {
            return new Bounds(0,0,-1,-1);
        }
    }
}
