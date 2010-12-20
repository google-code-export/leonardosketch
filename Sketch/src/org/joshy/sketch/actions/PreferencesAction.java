package org.joshy.sketch.actions;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.Spacer;
import org.joshy.gfx.node.layout.TabPanel;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.ArrayListModel;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.localization.Localization;
import org.joshy.sketch.Main;

import java.util.Collections;

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
        return getString("preferences.settings").toString();
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

        ArrayListModel<String> locale = new ArrayListModel<String>();
        locale.addAll(Localization.getSupportedLocales());
        Collections.sort(locale);
        final PopupMenuButton<String> localeChoice = new PopupMenuButton<String>();
        localeChoice.setModel(locale);
        if(Main.settings.containsKey(Main.DEFAULT_LOCALE)) {
            int n = locale.indexOf(Main.settings.getProperty(Main.DEFAULT_LOCALE));
            if(n >= 0) {
                localeChoice.setSelectedIndex(n);
            }
        }
        localeChoice.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
            }
        });

        Callback<ActionEvent> closeAction = new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                stage.hide();
                String l = localeChoice.getModel().get(localeChoice.getSelectedIndex());
                Main.settings.setProperty(Main.DEFAULT_LOCALE, l);
                Main.saveSettings();
                Localization.setCurrentLocale(l);
                Core.getShared().reloadSkins();
            }
        };


        TabPanel tab = new TabPanel();
        tab.add(getString("preferences.generalTab"),new VFlexBox().setBoxAlign(VFlexBox.Align.Left)
            .add(new HFlexBox()
                    .setBoxAlign(HFlexBox.Align.Baseline)
                    .add(trackingCheckbox)
                    .add(new Linkbutton(getString("misc.whatsthis")).onClicked(new Callback<ActionEvent>() {
                public void call(ActionEvent actionEvent) throws Exception {
                    OSUtil.openBrowser("http://code.google.com/p/leonardosketch/wiki/Tracking");
                }
            })))
            .add(new Label(getString("preferences.flickrCache")))
            .add(new Label(Main.FlickrSearchCache.getCacheDir().getAbsolutePath()).setColor(new FlatColor(0x606060)))
            .add(new Button(getString("preferences.deleteFlickrCache")).onClicked(clearFlickrCache))
        );
        tab.add(getString("preferences.advancedTab"), new VFlexBox().setBoxAlign(VFlexBox.Align.Stretch)
                .add(debugMenuCheckbox)
                .add(new Label(getString("preferences.preferredLocale")))
                .add(localeChoice)
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
