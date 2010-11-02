package org.joshy.sketch.actions;

import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Checkbox;
import org.joshy.gfx.node.control.Linkbutton;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.Spacer;
import org.joshy.gfx.node.layout.TabPanel;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.Main;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Nov 1, 2010
* Time: 3:39:53 PM
* To change this template use File | Settings | File Templates.
*/
public class PreferencesAction extends SAction {
    private Main manager;

    public PreferencesAction(Main main) {
        this.manager = main;
    }

    @Override
    public String getDisplayName() {
        return "Settings";
    }

    @Override
    public void execute() throws Exception {
        final Stage stage = Stage.createStage();
        boolean trackingEnabled = "true".equals(manager.settings.getProperty(Main.TRACKING_PERMISSIONS));
        Checkbox trackingCheckbox = new Checkbox("Enable Launch Tracking");
        trackingCheckbox.setSelected(trackingEnabled);
        trackingCheckbox.onClicked(new Callback<ActionEvent>(){
                public void call(ActionEvent actionEvent) throws Exception {
                    Checkbox checkbox = (Checkbox) actionEvent.getSource();
                    Main.settings.setProperty(Main.TRACKING_PERMISSIONS,""+checkbox.isSelected());
                }
            });

        Callback<ActionEvent> closeAction = new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                stage.hide();
            }
        };

        TabPanel tab = new TabPanel();
        tab.add("tracking",new VFlexBox().setBoxAlign(VFlexBox.Align.Stretch)
            .add(trackingCheckbox)
            .add(new Linkbutton("what's this?").onClicked(new Callback<ActionEvent>() {
                public void call(ActionEvent actionEvent) throws Exception {
                    OSUtil.openBrowser("http://code.google.com/p/leonardosketch/wiki/Tracking");
                }
            }))
        );

        stage.setContent(new VFlexBox().setBoxAlign(VFlexBox.Align.Stretch)
                .add(tab,1)
                .add(new HFlexBox().add(new Spacer(),1).add(new Button("close").onClicked(closeAction)))
        );
    }
}
