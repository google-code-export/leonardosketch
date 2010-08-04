package org.joshy.sketch.actions;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Apr 6, 2010
 * Time: 7:45:20 PM
 * To change this template use File | Settings | File Templates.
 *
 * The base class for all actions, which are encapsulated behavior that can be
 * attached to UI controls.
 */
public abstract class SAction {
    public String getDisplayName() {
        return "unknown saction";
    }
    public abstract void execute();
}
