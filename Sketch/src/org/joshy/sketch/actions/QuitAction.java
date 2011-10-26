package org.joshy.sketch.actions;

import com.boxysystems.jgoogleanalytics.FocusPoint;
import org.joshy.sketch.Main;

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
            Main.saveSettings();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(Main.trackingEnabled) {
            Main.tracker.trackSynchronously(new FocusPoint("exit", Main.mainApp));
        }

        System.exit(0);
    }

}
