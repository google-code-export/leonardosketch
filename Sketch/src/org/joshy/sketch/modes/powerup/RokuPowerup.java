package org.joshy.sketch.modes.powerup;

import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.io.SavePNGAction;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 3/31/12
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class RokuPowerup extends Powerup {

    // modifies current doc size
    // add new export action

    @Override
    public CharSequence getMenuName() {
        return "Roku App";
    }

    @Override
    public void enable(DocContext context, Main main) {
        u.p("enabling the roku power up");
        // modify doc size to fit a TV
        context.getDocument().setWidth(1280);
        context.getDocument().setHeight(720);

        // add launch roku app to file menu
        context.getFileMenu().addItem("Run on Roku", new RunOnRoku(context));
        context.redraw();
    }
}

class RunOnRoku extends SAction {

    private DocContext context;

    public RunOnRoku(DocContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws Exception {
        u.p("running on the roku");

        //make temp dir
        File tempdir = Util.makeTempDir();
        //make temp subdir
        File appdir = new File(tempdir,"testapp");
        appdir.mkdirs();

        //copy template to temp subdir
        File templatedir = new File("/Users/josh/projects/Roku/ShowPNGApp/");
        Map<String,String> keys = new HashMap<String, String>();
        Util.copyTemplate(templatedir,appdir,keys);

        //generate PNG
        File png = new File(appdir,"sample.png");
        u.p("exporting to");
        u.p(png.getAbsolutePath());
        SavePNGAction save = new SavePNGAction(null);
        save.includeBackground = true;
        save.includeDocumentBounds = true;
        save.export(png, (SketchDocument) context.getDocument());



        //execute ant script to run on the roku, passing in the IP addr on the commandline
        List<String> args = new ArrayList<String>();
        args.add("ant");
        args.add("-f");
        args.add(new File(appdir,"build.xml").getAbsolutePath());
        args.add("build");
        args.add("install");
        args.add("-Dipaddress=192.168.0.5");
        Process proc = Runtime.getRuntime().exec(
                args.toArray(new String[0]),
                new String[0],
                appdir
                );
        Util.streamToSTDERR(proc.getInputStream());
    }

}
