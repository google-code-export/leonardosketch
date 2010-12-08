package org.joshy.sketch.actions;

import com.boxysystems.jgoogleanalytics.FocusPoint;
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 4, 2010
 * Time: 7:43:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class QuitAction extends SAction {
    private Main main;

    public QuitAction(Main main) {
        super();
        this.main = main;
    }

    @Override
    public void execute() {

        try {
            saveSettings(this.main.settings,Main.SETTINGS_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(Main.trackingEnabled) {
            Main.tracker.trackSynchronously(new FocusPoint("exit", Main.mainApp));
        }

        System.exit(0);
    }

    private void saveSettings(Properties settings, File settingsFile) throws IOException {
        settings.store(new FileWriter(settingsFile),"sketchy settings");
    }

}
