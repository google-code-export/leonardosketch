package org.joshy.sketch.actions;

import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
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
        u.p("quitting");

        try {
            saveRecentFiles(this.main.recentFiles, Main.RECENT_FILES);
            saveSettings(this.main.settings,Main.SETTINGS_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Main.tracker.trackAsynchronously(new FocusPoint("exit", Main.mainApp));

        System.exit(0);
    }

    private void saveSettings(Properties settings, File settingsFile) throws IOException {
        settings.store(new FileWriter(settingsFile),"sketchy settings");
    }

    private void saveRecentFiles(List<File> recentFiles, File file) {
        try {
            XMLWriter out = new XMLWriter(file);
            out.start("files");
            for(File f : recentFiles) {
                out.start("file","filepath",f.getAbsolutePath());
                out.end();
            }
            out.end();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
