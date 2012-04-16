package org.joshy.sketch.modes.powerup;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.BackgroundTask;
import org.joshy.gfx.util.GraphicsUtil;
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

        //radio button
        GenericFXComponent.DrawDelegate radio_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.GRAY);
                g.fillCircle(c.getX() + c.getHeight() / 2, c.getY() + c.getHeight() / 2, c.getHeight() / 3);
                g.setPaint(FlatColor.BLACK);
                Font.drawCenteredVertically(g, "radiobutton", Font.DEFAULT, c.getX() + c.getHeight() + 5, c.getY(), c.getWidth(), c.getHeight(), true);
            }
        };

        Map<String,Object> radio_props = new HashMap<String, Object>();
        radio_props.put("text", "radiobutton");
        main.symbolManager.addVirtual(new GenericFXComponent(radio_delegate,radio_props,100, 30, "RadioButton"));

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


        //label
        GenericFXComponent.DrawDelegate label_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.BLACK);
                Font.drawCenteredVertically(g, "label", Font.DEFAULT, c.getX() + 5, c.getY(), c.getWidth(), c.getHeight(), true);
            }
        };

        Map<String,Object> label_props = new HashMap<String, Object>();
        label_props.put("text", "label");
        main.symbolManager.addVirtual(new GenericFXComponent(label_delegate,label_props, 100, 30, "Label"));



        //hyperlink
        GenericFXComponent.DrawDelegate hyperlink_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.BLACK);
                Font.drawCenteredVertically(g, "hyperlink", Font.DEFAULT, c.getX()+ 5, c.getY(), c.getWidth(), c.getHeight(), true);
            }
        };

        Map<String,Object> hyperlink_props = new HashMap<String, Object>();
        hyperlink_props.put("text", "hyperlink");
        main.symbolManager.addVirtual(new GenericFXComponent(hyperlink_delegate,hyperlink_props, 100, 30, "Hyperlink"));

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




        //list
        GenericFXComponent.DrawDelegate list_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.WHITE);
                g.fillRect(c.getX(), c.getY(), c.getWidth(), c.getHeight());
                g.setPaint(FlatColor.BLACK);
                g.drawRect(c.getX(), c.getY(), c.getWidth(), c.getHeight());
                g.setPaint(FlatColor.BLACK);
                String[] lines = new String[]{"this is a list","this is a list","this is a list","this is a list","this is a list","this is a list"};
                Font.drawLines(g,Font.DEFAULT, lines);
                double y = 0;
                while(y < c.getHeight()) {
                    g.drawLine(c.getX(),c.getY()+y,c.getX()+c.getWidth(),c.getY()+y);
                    y+=18;
                }
            }
        };

        Map<String,Object> list_props = new HashMap<String, Object>();
        main.symbolManager.addVirtual(new GenericFXComponent(list_delegate,list_props, 200, 120, "ListView"));





        //table
        GenericFXComponent.DrawDelegate table_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.WHITE);
                g.fillRect(c.getX(), c.getY(), c.getWidth(), c.getHeight());
                g.setPaint(FlatColor.BLACK);
                g.drawRect(c.getX(), c.getY(), c.getWidth(), c.getHeight());
                g.setPaint(FlatColor.BLACK);
                String[] lines = new String[]{"this is a table","this is a table"};
                Font.drawLines(g,Font.DEFAULT, lines);
                double y = 0;
                while(y < c.getHeight()) {
                    g.drawLine(c.getX(),c.getY()+y,c.getX()+c.getWidth(),c.getY()+y);
                    y+=18;
                }
                double x = 0;
                while(x < c.getWidth()) {
                    g.drawLine(c.getX()+x,c.getY(),c.getX()+x,c.getY()+c.getHeight());
                    x+=40;
                }
            }
        };

        Map<String,Object> table_props = new HashMap<String, Object>();
        main.symbolManager.addVirtual(new GenericFXComponent(table_delegate,table_props, 200, 120, "TableView"));





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


        //toggle button
        GenericFXComponent.DrawDelegate togglebutton_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.GRAY);
                g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 10, 10);
                g.setPaint(FlatColor.BLACK);
                Font.drawCentered(g, "toggle button", Font.DEFAULT, c.getX(), c.getY(), c.getWidth(), c.getHeight(), true);
            }
        };

        Map<String,Object> togglebutton_props = new HashMap<String, Object>();
        togglebutton_props.put("text", "toggle button");
        main.symbolManager.addVirtual(new GenericFXComponent(togglebutton_delegate,togglebutton_props, 100, 30, "ToggleButton"));


        //slider
        GenericFXComponent.DrawDelegate slider_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.GRAY);
                g.fillRoundRect(c.getX(), c.getY() + 3, c.getWidth(), c.getHeight()-6, 10, 10);
                g.setPaint(FlatColor.BLACK);
                g.drawRoundRect(c.getX(), c.getY() + 3, c.getWidth(), c.getHeight()-6, 10, 10);
                g.fillRoundRect(c.getX() + c.getWidth() / 2 - 10,c.getY(),20,c.getHeight(),4,4);
            }
        };

        Map<String,Object> slider_props = new HashMap<String, Object>();
        main.symbolManager.addVirtual(new GenericFXComponent(slider_delegate,slider_props, 100, 30, "Slider"));



        //progbar
        GenericFXComponent.DrawDelegate progbar_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.WHITE);
                g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 10, 10);
                g.setPaint(FlatColor.BLUE);
                g.fillRoundRect(c.getX(), c.getY(), c.getWidth()/2, c.getHeight(), 10, 10);
                g.setPaint(FlatColor.BLACK);
                g.drawRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 10, 10);
            }
        };

        Map<String,Object> progbar_props = new HashMap<String, Object>();
        main.symbolManager.addVirtual(new GenericFXComponent(progbar_delegate,progbar_props, 100, 30, "ProgressBar"));


        //progindicator
        GenericFXComponent.DrawDelegate progind_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.BLUE);
                g.fillOval(c.getX(), c.getY(), c.getHeight(), c.getHeight());
                g.setPaint(FlatColor.BLACK);
                g.drawOval(c.getX(), c.getY(), c.getHeight(), c.getHeight());
                g.setPaint(FlatColor.WHITE);
                Font.drawCentered(g,"100%",Font.DEFAULT,c.getX(),c.getY(),c.getHeight(),c.getHeight(),true);
            }
        };

        Map<String,Object> progind_props = new HashMap<String, Object>();
        main.symbolManager.addVirtual(new GenericFXComponent(progind_delegate,progind_props, 30, 30, "ProgressIndicator"));



        //password field
        GenericFXComponent.DrawDelegate password_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.WHITE);
                g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
                g.setPaint(FlatColor.BLACK);
                g.drawRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
                g.setPaint(FlatColor.BLACK);
                Font.drawCenteredVertically(g, "*******", Font.DEFAULT, c.getX() + 5, c.getY(), c.getWidth(), c.getHeight(), true);
            }
        };

        Map<String,Object> password_props = new HashMap<String, Object>();
        password_props.put("text", "*********");
        main.symbolManager.addVirtual(new GenericFXComponent(password_delegate,password_props, 100, 30, "PasswordField"));



        //choice box
        GenericFXComponent.DrawDelegate choicebox_delegate = new GenericFXComponent.DrawDelegate() {
            public void draw(GFX g, GenericFXComponent c) {
                g.setPaint(FlatColor.WHITE);
                g.fillRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
                g.setPaint(FlatColor.BLACK);
                g.drawRoundRect(c.getX(), c.getY(), c.getWidth(), c.getHeight(), 3, 3);
                g.setPaint(FlatColor.BLACK);
                GraphicsUtil.fillDownArrow(g,c.getX()+c.getWidth()-15-5,c.getY()+5,15);
                Font.drawCenteredVertically(g, "choice box", Font.DEFAULT, c.getX() +  5, c.getY(), c.getWidth(), c.getHeight(), true);
            }
        };

        Map<String,Object> choicebox_props = new HashMap<String, Object>();
        //choicebox_props.put("text", "*********");
        main.symbolManager.addVirtual(new GenericFXComponent(choicebox_delegate,choicebox_props, 100, 30, "ChoiceBox"));





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

