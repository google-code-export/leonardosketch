package org.joshy.sketch.actions;

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

    public UndoableAction getLastAction() {
        return actionStack.getLast();
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
        public CharSequence getName();
    }

    public void stepBackwards() {
//        u.p("backwards: getting index: " + index);
        UndoableAction action = actionStack.get(index);
        action.executeUndo();
        index--;
        //dump();
        context.addNotification("Undoing: " + action.getName());
    }

    private void stepForwards() {
        index++;
//        u.p("forwards: getting index:  " + index);
        UndoableAction action = actionStack.get(index);
        action.executeRedo();
        dump();
        context.addNotification("Redoing: " + action.getName());
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
}
