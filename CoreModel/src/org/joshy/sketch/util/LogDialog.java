package org.joshy.sketch.util;

import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.control.ScrollPane;
import org.joshy.gfx.node.control.Textarea;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.u;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Dec 7, 2010
 * Time: 5:45:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogDialog {

    public static void show(String s, Log.LogCollector col) {
        StringBuffer buf = new StringBuffer();
        for(Log.LogEvent evt: col.getEvents()) {
            buf.append("event\n");
            buf.append(evt.getLevel()+"===\n");
            buf.append(evt.getReportingClass()+"\n");

            for(Object o : evt.getValues()) {
                buf.append(""+o);
                if(o instanceof Throwable) {
                    buf.append("\n");
                    for(StackTraceElement e : ((Throwable)o).getStackTrace()) {
                        buf.append(e.getClassName()+""+e.getMethodName()+","+e.getFileName()+":"+e.getLineNumber()+"\n");
                    }
                }
            }
            u.p("");

        }

        Textarea log = new Textarea();
        log.setText(buf.toString());
        final Stage stage = Stage.createStage();
        stage.setContent(new VFlexBox()
                .setBoxAlign(VFlexBox.Align.Stretch)
                .add(new Label(s))
                .add(new ScrollPane(log),1)
                .add(new Button("Close").onClicked(new Callback<ActionEvent>(){
            public void call(ActionEvent actionEvent) throws Exception {
                stage.hide();
            }
        }))
        );
        stage.centerOnScreen();
    }
}
