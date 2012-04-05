package org.joshy.sketch.modes.powerup;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.BackgroundTask;
import org.joshy.gfx.util.u;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.model.*;
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
 * Time: 4:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class FXMLPowerup extends Powerup {
    @Override
    public CharSequence getMenuName() {
        return "FXML App";
    }

    @Override
    public void enable(DocContext context, Main main) {
        context.getFileMenu().addItem("Run as JavaFX", new RunAsJavaFX(context, main));


        //checkbox
        GenericFXComponent.DrawDelegate checkbox_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.GRAY);
                g.fillRoundRect(c.getX(), c.getY(), c.getHeight(), c.getHeight(), 3, 3);
                g.setPaint(FlatColor.BLACK);
                Font.drawCenteredVertically(g, "checkbox", Font.DEFAULT, c.getX() + c.getHeight() + 5, c.getY(), c.getWidth(), c.getHeight(), true);
            }
        };

        Map<String,Object> checkbox_props = new HashMap<String, Object>();
        checkbox_props.put("text", "checkbox");
        main.symbolManager.addVirtual(new GenericFXComponent(checkbox_delegate,checkbox_props,100, 30, "CheckBox"));

        //textfield
        GenericFXComponent.DrawDelegate textfield_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.WHITE);
                g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
                g.setPaint(FlatColor.BLACK);
                g.drawRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
                g.setPaint(FlatColor.BLACK);
                Font.drawCenteredVertically(g, "text field", Font.DEFAULT, c.getX() + c.getHeight() + 5, c.getY(), c.getWidth(), c.getHeight(), true);
            }
        };

        Map<String,Object> textfield_props = new HashMap<String, Object>();
        textfield_props.put("text", "text field");
        main.symbolManager.addVirtual(new GenericFXComponent(textfield_delegate,textfield_props, 100, 30, "TextField"));


        //text area
        GenericFXComponent.DrawDelegate textarea_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.WHITE);
                g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
                g.setPaint(FlatColor.BLACK);
                g.drawRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
                g.setPaint(FlatColor.BLACK);
                String[] lines = new String[]{"this is a text area","with multiple lines of text"};
                Font.drawLines(g,Font.DEFAULT, lines);
            }
        };

        Map<String,Object> textarea_props = new HashMap<String, Object>();
        textarea_props.put("text", "this is a text area\nwith multiple lines of text"   );
        main.symbolManager.addVirtual(new GenericFXComponent(textarea_delegate,textarea_props, 200, 120, "TextArea"));





        //button
        GenericFXComponent.DrawDelegate button_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.GRAY);
                g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 10, 10);
                g.setPaint(FlatColor.BLACK);
                Font.drawCentered(g, "button", Font.DEFAULT, c.getX(), c.getY(), c.getWidth(), c.getHeight(), true);
            }
        };

        Map<String,Object> button_props = new HashMap<String, Object>();
        button_props.put("text", "button");
        main.symbolManager.addVirtual(new GenericFXComponent(button_delegate,button_props, 100, 30, "Button"));



        //textfield
        GenericFXComponent.DrawDelegate password_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.WHITE);
                g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
                g.setPaint(FlatColor.BLACK);
                g.drawRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
                g.setPaint(FlatColor.BLACK);
                Font.drawCenteredVertically(g, "*******", Font.DEFAULT, c.getX() + c.getHeight() + 5, c.getY(), c.getWidth(), c.getHeight(), true);
            }
        };

        Map<String,Object> password_props = new HashMap<String, Object>();
        password_props.put("text", "*********");
        main.symbolManager.addVirtual(new GenericFXComponent(password_delegate,password_props, 100, 30, "PasswordField"));





        context.redraw();
    }

}

class RunAsJavaFX extends SAction {
    private DocContext context;
    private Main main;
    private BackgroundTask<DocContext, String> task;

    public RunAsJavaFX(DocContext context, Main main) {
        this.context = context;
        this.main = main;

    }
    @Override
    public void execute() throws Exception {
        task = new BackgroundTask<DocContext, String>() {
            @Override
            protected void onStart(DocContext data) {
                super.onStart(data);
                context.addNotification("Generating JavaFX App");
            }

            @Override
            protected String onWork(DocContext data) {
                try {
                    dobg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "done";
            }
        };

        task.setData(context);
        task.start();
    }

    public void dobg() throws Exception {
        u.p("running as JavaFX FXML App");

        //make temp dir
        File tempdir = Util.makeTempDir();
        //make temp subdir
        File appdir = new File(tempdir,"testapp");
        appdir.mkdirs();

        //copy template to temp subdir
        File templatedir = new File("/Users/josh/projects/javafx/FXMLTemplate/");
        Map<String,String> keys = new HashMap<String, String>();
        Util.copyTemplate(templatedir, appdir, keys);

        File fxmlfile = new File(appdir,"src/fxmltemplate/Generated.fxml");
        File outdir = new File(appdir,"src/fxmltemplate/");
        try {
            u.p("generating: " + fxmlfile);
            XMLWriter out = new XMLWriter(fxmlfile);
            ExportProcessor.process(new FXMLExport(outdir), out, (SketchDocument) context.getDocument());
            out.close();
        } catch (Exception ex) {
            u.p(ex);
        }
        
        //execute ant script to run on the roku, passing in the IP addr on the commandline
        List<String> args = new ArrayList<String>();
        args.add("ant");
        args.add("-f");
        args.add(new File(appdir,"build.xml").getAbsolutePath());
        args.add("clean");
        args.add("run");
        Process proc = Runtime.getRuntime().exec(
                args.toArray(new String[0]),
                new String[0],
                appdir
        );
        Util.streamToSTDERR(proc.getInputStream());
    }

}

