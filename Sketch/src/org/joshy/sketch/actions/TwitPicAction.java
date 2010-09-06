package org.joshy.sketch.actions;

import org.joshy.gfx.Core;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.io.SavePNGAction;
import org.joshy.sketch.controls.StandardDialog;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
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

            u.p("we already have auth info");
            final Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(consumerKey,consumerSecret);
            final AccessToken token = new AccessToken(context.getSettings().getProperty(TWITTER_TOKEN), context.getSettings().getProperty(TWITTER_TOKEN_SECRET));
            twitter.setOAuthAccessToken(token);
            //u.p("my id = " + twitter.getId());



            final File file = File.createTempFile("foo", ".png");
            file.deleteOnExit();
            SavePNGAction.export(file, (SketchDocument) context.getDocument());
            //u.p("wrote image to "+file.getAbsolutePath());


            final String message = StandardDialog.showEditText("Message with image","");
            context.getUndoOverlay().showIndicator("Uploading to TwitPic");

            new Thread(new Runnable(){
                public void run() {
                    try {
                        Configuration conf = new PropertyConfiguration(new Properties());
                        OAuthAuthorization oauth = new OAuthAuthorization(conf,consumerKey,consumerSecret,token);
                        ImageUpload upload = ImageUpload.getTwitpicUploader(TWITPIC_API,oauth);
                        final String resultUrl = upload.upload(file,"foo bar baz");
                        //u.p("finished uploading to twitpic: " + resultUrl);
                        twitter.updateStatus(message + " " + resultUrl);
                        Core.getShared().defer(new Runnable(){
                            public void run() {
                                context.getUndoOverlay().showIndicator("Done uploading to twitter");
                            }
                        });
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                Twitter twitter = new TwitterFactory().getInstance();
                twitter.setOAuthConsumer(consumerKey,consumerSecret);

                if(!context.getSettings().containsKey(TWITTER_TOKEN) || force) {
                    u.p("no auth info already");
                    RequestToken requestToken = twitter.getOAuthRequestToken();
                    String url = requestToken.getAuthorizationURL();
                    String pin = StandardDialog.showEditText("Please visit this url then paste in the PIN",url);
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,pin);
                    context.getSettings().setProperty(TWITTER_TOKEN,accessToken.getToken());
                    context.getSettings().setProperty(TWITTER_TOKEN_SECRET,accessToken.getTokenSecret());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
