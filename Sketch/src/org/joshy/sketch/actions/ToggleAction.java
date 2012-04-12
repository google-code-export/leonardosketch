package org.joshy.sketch.actions;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 18, 2010
 * Time: 12:56:16 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ToggleAction extends SAction {

    public abstract boolean getToggleState();
    public abstract void setToggleState(boolean toggleState);

    /**
     * Subclasses should override get/setToggleState instead of execute
     */
    @Override
    public void execute() {
        //no op
    }
}
