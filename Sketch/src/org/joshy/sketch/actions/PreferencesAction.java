package org.joshy.sketch.actions;

import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.stage.Stage;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Nov 1, 2010
* Time: 3:39:53 PM
* To change this template use File | Settings | File Templates.
*/
public class PreferencesAction extends SAction {
    @Override
    public String getDisplayName() {
        return "Settings";
    }

    @Override
    public void execute() throws Exception {
        Stage stage = Stage.createStage();
        stage.setContent(new Label("Preferences"));
    }
}
