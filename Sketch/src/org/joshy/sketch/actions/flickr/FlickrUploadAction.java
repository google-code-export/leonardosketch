package org.joshy.sketch.actions.flickr;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.uploader.UploadMetaData;
import com.aetrion.flickr.uploader.Uploader;
import org.joshy.gfx.Core;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Label;
import org.joshy.gfx.node.layout.GridBox;
import org.joshy.gfx.node.layout.Spacer;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.io.SavePNGAction;
import org.joshy.sketch.controls.StandardDialog;
import org.joshy.sketch.modes.DocContext;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.joshy.gfx.util.localization.Localization.getString;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Nov 26, 2010
 * Time: 2:27:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class FlickrUploadAction extends SAction {
    private static String key = "6e100c62b489b3232f9f90427d8f862f";
    private static String shared = "767ad9363cc90a7d";
    private DocContext context;
    private static final String FLICKR_USER_TOKEN = "org.joshy.sketch.actions.FlickrUploadAction.token";
    private static final String FLICKR_USER_ID = "org.joshy.sketch.actions.FlickrUploadAction.id";

    public FlickrUploadAction(DocContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws Exception {
        ChangeFlickrSettingsAction csa = new ChangeFlickrSettingsAction(context, false);
        csa.execute();
        if(!context.getSettings().containsKey(FLICKR_USER_TOKEN)) {
            u.p("WARNING: the user still doesn't have an auth token");
            return;
        }
        
        //write to a file
        final File file = File.createTempFile("foo", ".png");
        file.deleteOnExit();
        SavePNGAction.export(file, this.context.getDocument());

        final String message = StandardDialog.showEditText("Message with image","");
        if(message == null || message.trim().equals("")) {
            u.p("the user said no!");
            return;
        }
        this.context.getUndoOverlay().showIndicator("Uploading to Flickr");
        new Thread(new Runnable(){
            public void run() {
                try {
                    Flickr flickr = new Flickr(
                            key,shared,
                            new REST()
                    );

                    AuthInterface authInterface = flickr.getAuthInterface();
                    Auth auth = authInterface.checkToken(context.getSettings().getProperty(FLICKR_USER_TOKEN));
                    RequestContext context = RequestContext.getRequestContext();
                    context.setAuth(auth);

                    Uploader up = new Uploader(key, shared);
                    UploadMetaData meta = new UploadMetaData();
                    meta.setAsync(false);
                    //meta.setContentType(Flickr.CONTENTTYPE_SCREENSHOT);
                    //meta.setDescription("my screenshot description");
                    meta.setHidden(false);
                    meta.setPublicFlag(true);
                    meta.setSafetyLevel(Flickr.SAFETYLEVEL_SAFE);

                    List<String> tags = new ArrayList<String>();
                    tags.add("leosketch");
                    meta.setTags(tags);
                    meta.setTitle(message);
                    u.p("starting to upload");
                    final String str = up.upload(new FileInputStream(file),meta);
                    u.p("done uploading: " + str);

                    Core.getShared().defer(new Runnable(){
                        public void run() {
                            FlickrUploadAction.this.context.getUndoOverlay().showIndicator("Done uploading to Flickr");
                            OSUtil.openBrowser("http://www.flickr.com/photos/joshyx/"+str+"/");
                        }
                    });
                } catch (SAXException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (FileNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (FlickrException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }).start();

    }

    public static class ChangeFlickrSettingsAction extends SAction {
        private DocContext context;
        private boolean force;
        private String frob;

        public ChangeFlickrSettingsAction(DocContext context, boolean force) {
            this.context = context;
            this.force = force;
        }

        @Override
        public void execute() throws Exception {
            if(!context.getSettings().containsKey(FLICKR_USER_TOKEN) || force) {
                authenticate();
            }
        }

        private void authenticate() throws ParserConfigurationException, IOException, SAXException {
            Flickr.debugStream = true;
            Flickr.debugRequest = true;
            Flickr flickr = new Flickr(
                    key,shared,
                    new REST()
            );
            final RequestContext context = RequestContext.getRequestContext();
            //ask user to authenticate on the web
            final AuthInterface authInterface = flickr.getAuthInterface();
            try {
                frob = authInterface.getFrob();
            } catch (FlickrException e) {
                e.printStackTrace();
            }
            System.out.println("frob: " + frob);
            final URL url = authInterface.buildAuthenticationUrl(Permission.DELETE, frob);
            System.out.println("Press return after you granted access at this URL:");
            System.out.println(url.toExternalForm());

            final Stage stage = Stage.createStage();
            stage.setContent(
                new GridBox()
                    .createColumn(50,GridBox.Align.Right)
                    .createColumn(50,GridBox.Align.Fill)
                    .addControl(new Label("Please visit Flickr and grant Leo access to your account"))
                    .nextRow()
                    .addControl(new Spacer())
                    .addControl(new Button("Goto Flickr").onClicked(new Callback<ActionEvent>() {
                        public void call(ActionEvent event) {
                            OSUtil.openBrowser(url.toExternalForm());
                        }
                    }))
                    .nextRow()
                    .addControl(new Button(getString("dialog.cancel")).onClicked(new Callback<ActionEvent>(){
                        public void call(ActionEvent event) {
                            stage.hide();
                        }
                    }))
                    .addControl(new Button("Continue >").onClicked(new Callback<ActionEvent>(){
                        public void call(ActionEvent event) {
                            stage.hide();
                            try {
                                Auth auth = authInterface.getToken(frob);
                                System.out.println("Authentication success");
                                // This token can be used until the user revokes it.
                                System.out.println("Token: " + auth.getToken());
                                String user_token = auth.getToken();
                                ChangeFlickrSettingsAction.this.context.getSettings().setProperty(FLICKR_USER_TOKEN,user_token);
                                String user_id = auth.getUser().getId();
                                ChangeFlickrSettingsAction.this.context.getSettings().setProperty(FLICKR_USER_ID,user_id);
                                System.out.println("nsid: " + auth.getUser().getId());
                                System.out.println("Realname: " + auth.getUser().getRealName());
                                System.out.println("Username: " + auth.getUser().getUsername());
                                System.out.println("Permission: " + auth.getPermission().getType());
                            } catch (FlickrException e) {
                                System.out.println("Authentication failed");
                                e.printStackTrace();
                            } catch (SAXException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    }))
            );
            stage.setWidth(400);
            stage.setHeight(200);
        }
    }
}
