package org.joshy.sketch.util;

import com.joshondesign.xml.Doc;
import com.joshondesign.xml.Elem;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.layout.FlexBox;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.Spacer;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.u;
import org.joshy.gfx.util.xml.XMLRequest;
import org.joshy.sketch.Main;

import javax.xml.xpath.XPathExpressionException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Nov 1, 2010
 * Time: 3:30:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateChecker {
    private static Main manager;

    public static void setup(Main main) throws MalformedURLException, InterruptedException {
        manager = main;
        //ping the url & parse the result
        //ignore errors
        //if verify update
        try {
            u.p("checking for updates at: " + Main.UPDATE_URL);
            new XMLRequest()
                    .setURL(Main.UPDATE_URL)
                    .setMethod(XMLRequest.METHOD.GET)
                    .onComplete(new Callback<Doc>(){
                        public void call(Doc doc) throws Exception {
                            if(doc != null) {
                                verifyUpdate(doc);
                            }
                        }
                    })
                    .onError(new Callback<Throwable>() {
                        public void call(Throwable e) throws Exception {
                            u.p("there was an error: " + e);
                            u.dumpStack();
                        }
                    })
                    .start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void verifyUpdate(Doc doc) throws XPathExpressionException {
        u.p("callback: " + doc);
        u.p("current build number = " + Main.CURRENT_BUILD_NUMBER);
        List<Elem> newReleases = new ArrayList<Elem>();
        for(Elem release : doc.xpath("/updates/release")) {
            u.p("build number = " + release.attr("buildNumber"));
            if(Integer.parseInt(release.attr("buildNumber")) > Main.CURRENT_BUILD_NUMBER) {
                newReleases.add(release);
            }
        }
        if(newReleases.isEmpty()) {
            u.p("no new releases");
        } else {
            u.p("a new release!");
            final Stage stage = Stage.createStage();
            Callback<ActionEvent> dismiss = new Callback<ActionEvent>() {
                public void call(ActionEvent actionEvent) throws Exception {
                    stage.hide();
                }
            };
            Callback<ActionEvent> skipVersion = new Callback<ActionEvent>() {
                public void call(ActionEvent actionEvent) throws Exception {
                    stage.hide();
                }
            };
            Callback<ActionEvent> getUpdate = new Callback<ActionEvent>() {
                public void call(ActionEvent actionEvent) throws Exception {
                    stage.hide();
                    OSUtil.openBrowser(Main.DOWNLOAD_URL);
                }
            };
            FlexBox box = new VFlexBox().setBoxAlign(VFlexBox.Align.Stretch);
            box.add(new Label("New Version Available!").setId("updates-header"));

            for(Elem release : newReleases) {
                u.p("build = " + release.attr("buildNumber"));
                u.p("date = " + release.attr("buildDate"));
                u.p("version = " + release.attr("version"));
                u.p("description = " + release.text());
                box.add(new Label("Version: " + release.attr("version")).setPrefWidth(200));
                box.add(new Label(release.text()).setPrefWidth(200));
            }
            box.add(new Spacer(),1);
            box.add(new HFlexBox()
                    .add(new Button("Get the Update").onClicked(getUpdate))
                    .add(new Button("Skip This Version").onClicked(skipVersion))
                    .add(new Button("Remind Me Later").onClicked(dismiss))
            );
            stage.setContent(box);
        }
    }

}
