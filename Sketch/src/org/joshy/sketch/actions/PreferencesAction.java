package org.joshy.sketch.actions;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Checkbox;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.control.Linkbutton;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.Spacer;
import org.joshy.gfx.node.layout.TabPanel;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.Main;

import static org.joshy.gfx.util.localization.Localization.getString;

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
        
        //tracking checkbox
        boolean trackingEnabled = "true".equals(manager.settings.getProperty(Main.TRACKING_PERMISSIONS));
        Checkbox trackingCheckbox = new Checkbox(getString("preferences.enable.analytics.tracking"));
        trackingCheckbox.setSelected(trackingEnabled);
        trackingCheckbox.onClicked(new Callback<ActionEvent>(){
                public void call(ActionEvent actionEvent) throws Exception {
                    Checkbox checkbox = (Checkbox) actionEvent.getSource();
                    Main.settings.setProperty(Main.TRACKING_PERMISSIONS,""+checkbox.isSelected());
                }
            });

        //starting on translations
        //debug menu
        Checkbox debugMenuCheckbox = new Checkbox(getString("preferences.enable.debug.menu"));
        boolean debugMenuEnabled = "true".equals(manager.settings.getProperty(Main.DEBUG_MENU));
        debugMenuCheckbox.setSelected(debugMenuEnabled);
        debugMenuCheckbox.onClicked(new Callback<ActionEvent>(){
            public void call(ActionEvent actionEvent) throws Exception {
                Checkbox checkbox = (Checkbox) actionEvent.getSource();
                Main.settings.setProperty(Main.DEBUG_MENU,""+checkbox.isSelected());
            }
        });

        Callback<ActionEvent> closeAction = new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                stage.hide();
                Main.saveSettings();
            }
        };

        TabPanel tab = new TabPanel();
        tab.add("General",new VFlexBox().setBoxAlign(VFlexBox.Align.Left)
            .add(new HFlexBox()
                    .setBoxAlign(HFlexBox.Align.Baseline)
                    .add(trackingCheckbox)
                    .add(new Linkbutton(getString("misc.whatsthis")).onClicked(new Callback<ActionEvent>() {
                public void call(ActionEvent actionEvent) throws Exception {
                    OSUtil.openBrowser("http://code.google.com/p/leonardosketch/wiki/Tracking");
                }
            })))
            .add(new Label("Flickr Cache"))
            .add(new Label(Main.FlickrSearchCache.getCacheDir().getAbsolutePath()).setColor(new FlatColor(0x606060)))
            .add(new Button("Delete Cache").onClicked(clearFlickrCache))
        );
        tab.add("Advanced", new VFlexBox().setBoxAlign(VFlexBox.Align.Stretch)
            .add(debugMenuCheckbox)
            .add(new Label(getString("misc.changesAppliedLater").toString()))
        );

        stage.setContent(new VFlexBox().setBoxAlign(VFlexBox.Align.Stretch)
                .add(tab,1)
                .add(new HFlexBox().add(new Spacer(),1).add(new Button(getString("misc.close")).onClicked(closeAction)))
        );
    }

    Callback<ActionEvent> clearFlickrCache = new Callback<ActionEvent>() {
        public void call(ActionEvent actionEvent) throws Exception {
            Main.FlickrSearchCache.cleanCache();
        }
    };
}
