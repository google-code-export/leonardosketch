package org.joshy.sketch.test;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Aug 6, 2010
 * Time: 6:20:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterTest {

    public static void main(String ... args) throws TwitterException, IOException {
        String consumerKey = "Di1ZjpaZBqBTuCNBMmQ0g";
        String consumerSecret = "Qsa832hZ0z6Di2AY1umfhAgPowf2YtjCxQvooRzXTM";

        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(consumerKey,consumerSecret);
        RequestToken requestToken = twitter.getOAuthRequestToken();
        AccessToken accessToken = null;

        p("go to this url: " + requestToken.getAuthorizationURL());
        p("then press enter");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String pin = br.readLine();
        if(pin.length() > 0){
            accessToken = twitter.getOAuthAccessToken(requestToken, pin);
        } else {
            accessToken = twitter.getOAuthAccessToken();
        }
        p("got the access token: " + accessToken);
        storeAccessToken(twitter.verifyCredentials().getId() , accessToken);

        AccessToken toke = new AccessToken(accessToken.getToken(), accessToken.getTokenSecret());
        twitter.setOAuthAccessToken(accessToken);
        Status status = twitter.updateStatus("quick test of twitter with oauth. hope this works! :)");
        p("updated the status to : " + status.getText());
    }

    private static void storeAccessToken(int useId, AccessToken accessToken){
        p("id = " + useId);
        p("access token = " + accessToken);
        p("token = " + accessToken.getToken());
        p("secret = " + accessToken.getTokenSecret());
    }
    private static void p(String s) {
        System.out.println(s);
    }
}
