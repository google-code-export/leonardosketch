package org.joshy.sketch.actions;

import org.joshy.gfx.Core;
import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.GridBox;
import org.joshy.gfx.node.layout.Spacer;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.actions.io.SavePNGAction;
import org.joshy.sketch.controls.StandardDialog;
import org.joshy.sketch.controls.ToggleGroup;
import org.joshy.sketch.model.CanvasDocument;
import org.joshy.sketch.model.PixelDocument;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.modes.vector.VectorDocContext;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;
import twitter4j.http.AccessToken;
import twitter4j.http.OAuthAuthorization;
import twitter4j.http.RequestToken;
import twitter4j.util.ImageUpload;

import java.io.File;
import java.util.Properties;

import static org.joshy.gfx.util.localization.Localization.getString;

/**
 * Uploads a snapshot of the current document to twitter
 */
public class TwitPicAction extends SAction {
    private static final String consumerKey = "Di1ZjpaZBqBTuCNBMmQ0g";
    private static final String consumerSecret = "Qsa832hZ0z6Di2AY1umfhAgPowf2YtjCxQvooRzXTM";
    private static final String TWITPIC_API = "143ccfc261d48a81a1094f18019b7a82";

    private static final String TWITTER_TOKEN = "org.joshy.sketch.actions.TwitPicAction.token";
    private static final String TWITTER_TOKEN_SECRET = "org.joshy.sketch.actions.TwitPicAction.tokenSecret";

    private DocContext context;
    private Twitter twitter;
    private AccessToken token;

    public TwitPicAction(DocContext context) {
        super();
        this.context = context;
    }

    @Override
    public void execute() {
        try {

            ChangeSettingsAction csa = new ChangeSettingsAction(context, false);
            csa.execute();
            if(!context.getSettings().containsKey(TWITTER_TOKEN)) return;

            twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(consumerKey,consumerSecret);
            token = new AccessToken(context.getSettings().getProperty(TWITTER_TOKEN), context.getSettings().getProperty(TWITTER_TOKEN_SECRET));
            twitter.setOAuthAccessToken(token);
            //u.p("my id = " + twitter.getId());

            showConfigDialog();
            if(true) return;


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showConfigDialog() {
        final Stage stage = Stage.createStage();
        stage.setTitle("Share with Twitter");
        GridBox grid = new GridBox()
                .createColumn(30, GridBox.Align.Left)
                .createColumn(300, GridBox.Align.Left)
                ;
        stage.setWidth(400);
        stage.setHeight(280);
        final Radiobutton selected = new Radiobutton("Just selected shapes");
        final Radiobutton contents = new Radiobutton("Page contents (just big enough for the content)");
        final Radiobutton page = new Radiobutton("Entire page (clipped at page edges)");
        final Checkbox includeStamp = new Checkbox("include 'handcrafted with LeonardoSketch.org' in lower left");
        includeStamp.setSelected(true);

        final ToggleGroup tg = new ToggleGroup();
        tg.add(selected)
                .add(contents)
                .add(page);
        tg.setSelectedButton(page);

        grid.addControl(new Label("Share:"));
        grid.addControl(selected);
        grid.nextRow();
        grid.addControl(new Spacer());
        grid.addControl(contents);
        grid.nextRow();
        grid.addControl(new Spacer());
        grid.addControl(page);
        grid.nextRow();
        grid.nextRow();
        grid.addControl(includeStamp);
        grid.nextRow();
        Button cancelButton = new Button("cancel");
        cancelButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                stage.hide();
            }
        });
        grid.addControl(cancelButton);
        Button continueButton = new Button("continue");
        continueButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                stage.hide();
                final File file = File.createTempFile("foo", ".png");
                file.deleteOnExit();

                CanvasDocument doc = context.getDocument();
                if(doc instanceof SketchDocument) {
                    SketchDocument sdoc = (SketchDocument) doc;
                    SavePNGAction save = new SavePNGAction(null);
                    VectorDocContext vdc = (VectorDocContext) context;
                    save.includeBackground = true;
                    if(includeStamp.isSelected()) {
                        save.includeStamp = true;
                    }
                    if(tg.getSelectedButton() == page) {
                        save.includeDocumentBounds = true;
                    }
                    if(tg.getSelectedButton() == selected) {
                        SavePNGAction.exportFragment(file, (Iterable<SNode>) vdc.getSelection().items());
                    } else {
                        save.export(file, sdoc);
                    }
                }
                if(doc instanceof PixelDocument) {
                    SavePNGAction save = new SavePNGAction(null);
                    save.exportStatic(file, (PixelDocument) doc);
                }

                requestMessage(file);
            }
        });
        grid.addControl(continueButton);
        stage.setContent(grid);
    }

    private void requestMessage(File file) {
        final String message = StandardDialog.showEditText("Message with image","");
        if(message == null) return;
        uploadPhoto(message,file);
    }

    private void uploadPhoto(final String message, final File file) {
        context.addNotification("Uploading to TwitPic");

        new Thread(new Runnable(){
            public void run() {
                try {
                    Configuration conf = new PropertyConfiguration(new Properties());
                    OAuthAuthorization oauth = new OAuthAuthorization(conf,consumerKey,consumerSecret,token);
                    ImageUpload upload = ImageUpload.getTwitpicUploader(TWITPIC_API,oauth);
                    final String resultUrl = upload.upload(file,message);
                    Status s = twitter.updateStatus(message + " " + resultUrl);
                    final String tweetUrl = "http://twitter.com/"+s.getUser().getScreenName()+"/status/"+s.getId();
                    Core.getShared().defer(new Runnable(){
                        public void run() {
                            context.addNotification("Done uploading to Twitter");
                            OSUtil.openBrowser(tweetUrl);
                        }
                    });
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static class ChangeSettingsAction extends SAction {
        private boolean force;
        private DocContext context;

        public ChangeSettingsAction(DocContext context, boolean force) {
            super();
            this.force = force;
            this.context = context;
        }

        @Override
        public void execute() {
            try {
                final Twitter twitter = new TwitterFactory().getInstance();
                twitter.setOAuthConsumer(consumerKey,consumerSecret);

                if(!context.getSettings().containsKey(TWITTER_TOKEN) || force) {
                    //u.p("no auth info already");
                    final RequestToken requestToken = twitter.getOAuthRequestToken();
                    final String url = requestToken.getAuthorizationURL();

                    final Textbox pin = new Textbox("");
                    pin.setPrefWidth(50);
                    pin.setWidth(50);
                    final Stage stage = Stage.createStage();
                    stage.setContent(
                            new GridBox()
                                    .createColumn(50,GridBox.Align.Right)
                                    .createColumn(50,GridBox.Align.Fill)
                                    .addControl(new Label(getString("twitterAuthDialog.text1")))
                                    .addControl(new Label(getString("twitterAuthDialog.text2")))
                                    .nextRow()
                                    .addControl(new Spacer())
                                    .addControl(new Button("Goto Twitter.com").onClicked(new Callback<ActionEvent>() {
                                        public void call(ActionEvent event) {
                                            OSUtil.openBrowser(url);
                                        }
                                    }))
                                    .nextRow()
                                    .addControl(new Label("PIN"))
                                    .addControl(pin)
                                    .nextRow()
                                    .addControl(new Button(getString("dialog.cancel")).onClicked(new Callback<ActionEvent>(){
                                        public void call(ActionEvent event) {
                                            stage.hide();
                                        }
                                    }))
                                    .addControl(new Button(getString("twitterAuthDialog.authenticate")).onClicked(new Callback<ActionEvent>(){
                                        public void call(ActionEvent event) {
                                            stage.hide();
                                            try {
                                                String pinText = pin.getText();
                                                //u.p("using pin text " + pinText);
                                                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,pinText);
                                                context.getSettings().setProperty(TWITTER_TOKEN,accessToken.getToken());
                                                context.getSettings().setProperty(TWITTER_TOKEN_SECRET,accessToken.getTokenSecret());
                                            } catch (TwitterException e) {
                                                e.printStackTrace();
                                                StandardDialog.showError("Twitter authentication failed.\nPlease try again.");
                                            }
                                        }
                                    }))
                    );
                    stage.setWidth(400);
                    stage.setHeight(200);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
