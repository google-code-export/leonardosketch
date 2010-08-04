package org.joshy.sketch.actions;

import com.joshondesign.xml.Doc;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.u;
import org.joshy.gfx.util.xml.XMLRequest;
import org.joshy.sketch.controls.StandardDialog;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Uploads a snapshot of the current document to twitter
 */
public class TwitPicAction extends SAction {
    private static final String USERNAME = "org.joshy.sketch.actions.TwitPicAction.username";
    private static final String PASSWORD = "org.joshy.sketch.actions.TwitPicAction.password";
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
            if(csa.username == null || csa.password == null) return;
            
            File file = File.createTempFile("foo", ".png");
            file.deleteOnExit();
            SavePNGAction.export(file, (SketchDocument) context.getDocument());
            u.p("wrote image to "+file.getAbsolutePath());
            URL url = new URL("http://twitpic.com/api/uploadAndPost");
            String message = StandardDialog.showEditText("Status message with image","");
            if(message == null) return;

            new XMLRequest()
                    .setMethod(XMLRequest.METHOD.POST)
                    .setURL("http://twitpic.com/api/uploadAndPost")
                    .setMultiPart(true)
                    .setParameter("username", csa.username)
                    .setParameter("password", csa.password)
                    .setParameter("message", message)
                    .setFile(file)
                    .onComplete(new Callback<Doc>() {
                        public void call(Doc doc) {
                            doc.dump();
                            context.getUndoOverlay().showIndicator("Uploaded");
                            try {
                                OSUtil.openBrowser(doc.xpathString("rsp/mediaurl/text()").trim());
                            } catch (XPathExpressionException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .start();

            u.p("started request");
            context.getUndoOverlay().showIndicator("Uploading to TwitPic");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//                var rsp = doc.elements("rsp")[0];
//                println("rsp = {rsp}");
//                println("status = {rsp.attr('status')}");
//                println("stat = {rsp.attr('stat')}");
//                if(rsp.attr('status') == 'ok') {
//                    var url = rsp.elements("mediaurl")[0].text();
//                    println("url = {url}");
//                    if(url != null) {
//                        OSUtil.openBrowser(url);
//                    }
//                }
//                if(rsp.attr('stat') == 'fail') {
//                    var err = rsp.elements("err")[0];
//                    showError(err.attr("msg"));
//                }

    public static class ChangeSettingsAction extends SAction {
        private String username;
        private String password;
        private boolean force;
        private DocContext context;

        public ChangeSettingsAction(DocContext context, boolean force) {
            super();
            this.force = force;
            this.context = context;
        }

        @Override
        public void execute() {
            username = null;
            if(!context.getSettings().containsKey(USERNAME) || force) {
                username = StandardDialog.showEditText("Twitter username","");
                context.getSettings().setProperty(USERNAME,username);
            } else {
                username = context.getSettings().getProperty(USERNAME);
            }
            if(username == null)return;

            password = null;
            if(!context.getSettings().containsKey(PASSWORD) || force) {
                password = StandardDialog.showEditText("Twitter password","");
                context.getSettings().setProperty(PASSWORD, password);
            } else {
                password = context.getSettings().getProperty(PASSWORD);
            }
            if(password == null)return;
        }
    }
}
