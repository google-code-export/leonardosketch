package org.joshy.sketch.test;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.uploader.UploadMetaData;
import com.aetrion.flickr.uploader.Uploader;
import org.joshy.gfx.util.u;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Nov 26, 2010
 * Time: 10:34:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class FlickrTest {
    private static String frob;

    public static void main(String ... args) throws IOException, SAXException, FlickrException, ParserConfigurationException {
        String key = "6e100c62b489b3232f9f90427d8f862f";
        String shared = "767ad9363cc90a7d";


        //turn on debugging
        Flickr.debugStream = true;
        Flickr.debugRequest = true;
        Flickr flickr = new Flickr(
                key,shared,
                new REST()
        );

        /*
        //
        RequestContext context = RequestContext.getRequestContext();
        //ask user to authenticate on the web
        AuthInterface authInterface = flickr.getAuthInterface();
        try {
            frob = authInterface.getFrob();
        } catch (FlickrException e) {
            e.printStackTrace();
        }
        System.out.println("frob: " + frob);
        URL url = authInterface.buildAuthenticationUrl(Permission.DELETE, frob);
        System.out.println("Press return after you granted access at this URL:");
        System.out.println(url.toExternalForm());

        //wait for return
        BufferedReader infile = new BufferedReader ( new InputStreamReader(System.in) );
        String line = infile.readLine();


        try {
            Auth auth = authInterface.getToken(frob);
            System.out.println("Authentication success");
            // This token can be used until the user revokes it.
            System.out.println("Token: " + auth.getToken());
            System.out.println("nsid: " + auth.getUser().getId());
            System.out.println("Realname: " + auth.getUser().getRealName());
            System.out.println("Username: " + auth.getUser().getUsername());
            System.out.println("Permission: " + auth.getPermission().getType());
        } catch (FlickrException e) {
            System.out.println("Authentication failed");
            e.printStackTrace();
        }
*/
        
        AuthInterface authInterface = flickr.getAuthInterface();
        Auth auth = authInterface.checkToken("72157625348978315-7c9ef5a03dc17410");
        RequestContext context = RequestContext.getRequestContext();
        context.setAuth(auth);

        Uploader up = new Uploader(key, shared);
        File file = new File("test.png");
        UploadMetaData meta = new UploadMetaData();
        meta.setAsync(false);
        meta.setContentType(Flickr.CONTENTTYPE_SCREENSHOT);
        meta.setDescription("my screenshot description");
        meta.setHidden(false);
        meta.setPublicFlag(true);
        meta.setSafetyLevel(Flickr.SAFETYLEVEL_SAFE);

        List<String> tags = new ArrayList<String>();
        tags.add("leosketch");
        tags.add("webos");
        meta.setTags(tags);
        meta.setTitle("webos screenshot");
        u.p("starting to upload");
        up.upload(new FileInputStream(file),meta);
        u.p("done uploading");
        /*
</rsp>
Authentication success
Token: 72157625348978315-7c9ef5a03dc17410
nsid: 31706743@N00
Realname: Joshua Marinacci
Username: Joshua Marinacci
Permission: 3
        
         */
    }
}
