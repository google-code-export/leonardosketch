package org.joshy.sketch.actions;

import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.control.Linkbutton;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.Spacer;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.Main;


/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Nov 1, 2010
* Time: 3:40:18 PM
* To change this template use File | Settings | File Templates.
*/
public class AboutAction extends SAction {
    private Main manager;

    public AboutAction(Main main) {
        this.manager = main;
    }

    @Override
    public String getDisplayName() {
        return "About Leonardo";
    }

    @Override
    public void execute() throws Exception {
        final Stage stage = Stage.createStage();
        stage.setTitle("About Leonardo");
        Callback openLink = new org.joshy.gfx.event.Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                OSUtil.openBrowser("http://leonardosketch.org/");
            }
        };
        Callback closeStage = new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                stage.hide();
            }
        };
        
        //leonardo sketch
        stage.setContent(new VFlexBox().setBoxAlign(VFlexBox.Align.Stretch)
                .add(new HFlexBox().add(new Label("Leonardo")).setId("aboutHeader"))
                .add(new Linkbutton("http://leonardosketch.org/").onClicked(openLink))
                .add(new HFlexBox()
                        .add(new Label("Version"))
                        .add(new Label(Main.releaseProperties.getProperty("org.joshy.sketch.build.version"))))
                .add(new HFlexBox()
                        .add(new Label("Build number"))
                        .add(new Label(Main.releaseProperties.getProperty("org.joshy.sketch.build.number"))))
                .add(new HFlexBox()
                        .add(new Label("Build date"))
                        .add(new Label(Main.releaseProperties.getProperty("org.joshy.sketch.build.date"))))
                .add(new Spacer(),1)
                .add(new HFlexBox().add(new Spacer(),1).add(new Button("Close").onClicked(closeStage)))
        );
        stage.setWidth(400);
        stage.setHeight(300);
    }
}
