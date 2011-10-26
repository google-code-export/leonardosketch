package org.joshy.sketch.actions;

import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SText;
import org.joshy.sketch.modes.vector.VectorDocContext;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 4/7/11
 * Time: 8:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class TextActions {
    public static class ResetTextSize extends NodeActions.NodeAction {
        public ResetTextSize(VectorDocContext context) {
            super(context);
        }
        @Override
        public CharSequence getDisplayName() {
            return "Reset Text Size";
        }

        @Override
        public void execute() throws Exception {
            if(context.getSelection().isEmpty()) return;
            SNode first = context.getSelection().firstItem();
            if(first instanceof SText) {
                ((SText)first).setAutoSize(true);
                ((SText)first).refresh();
            }

        }
    }
}
