package org.joshy.sketch.modes.powerup;

import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.io.SavePNGAction;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.util.Util;

import java.io.File;
import java.io.IOException;
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
    public void enable(DocContext context) {
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
        File tempdir = makeTempDir();
        //make temp subdir
        File appdir = new File(tempdir,"testapp");
        appdir.mkdirs();

        //copy template to temp subdir
        File templatedir = new File("/Users/josh/projects/Roku/ShowPNGApp/");
        Map<String,String> keys = new HashMap<String, String>();
        copyTemplate(templatedir,appdir,keys);

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

    private void copyTemplate(File srcDir, File destDir, Map<String, String> keys) throws IOException {
        u.p("copying template to temp dir: ");
        u.p(destDir);
        if(!destDir.exists()) {
            destDir.mkdir();
        }
        for(File f : srcDir.listFiles()) {
            copyFileToDir(f, destDir, keys);
        }
    }

    private void copyFileToDir(File srcfile, File destdir, Map<String, String> keys) throws IOException {
        if(!srcfile.isDirectory()) {
            u.p("copying file : " + srcfile.getAbsolutePath());
            Util.copyToFile(srcfile, new File(destdir, srcfile.getName()));
        } else {
            u.p("isdir: " + srcfile.getAbsolutePath());
            u.indent();
            copyTemplate(srcfile, new File(destdir,srcfile.getName()), keys);
            u.outdent();
        }
    }

    private File makeTempDir() throws IOException {
        File file = File.createTempFile("leosketch","roku_app");
        if(file.exists() && !file.isDirectory()) {
            file.delete();
        }
        file.mkdirs();
        return file;
    }
}
