package org.joshy.sketch.actions;

import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.EventBus;

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
    private boolean enabled = true;

    public CharSequence getDisplayName() {
        return "unknown saction";
    }
    public abstract void execute() throws Exception;
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        boolean old = isEnabled();
        this.enabled = enabled;
        if(this.enabled != old) {
            fireChange(old,this.enabled);
        }
    }

    private void fireChange(boolean old, boolean enabled) {
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.BooleanChanged,enabled,this));
    }
}
