package com.extensions.Twishort;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.Authorization;
import twitter4j.http.HttpRequest;
import twitter4j.http.RequestMethod;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.pahans.kichibichiya.PrivateConstants;
import com.pahans.kichibichiya.activity.ComposeActivity;
import com.pahans.kichibichiya.util.ServiceInterface;

public class TwishortHelper {
	public static void sendPost(String text,String token,String token_secret, final ComposeActivity composeActivity) throws Exception{
		AsyncTask<String,Void,String> task = new AsyncTask<String,Void,String>() {
			@Override
			protected String doInBackground(String... params) {				
				String text = params[0];
				String url = "http://api.twishort.com/1.1/post.json";
				String consumerKey = PrivateConstants.TWITTER_CONSUMER_KEY;
				String consumerSecret = PrivateConstants.TWITTER_CONSUMER_SECRET;
				String accessToken = params[1];
				String tokenSecret = params[2];
				
				TwitterFactory factory = new TwitterFactory();
			    AccessToken Token = new AccessToken(accessToken,tokenSecret);
			    Twitter twitter = factory.getInstance();
			    twitter.setOAuthConsumer(consumerKey, consumerSecret);
			    twitter.setOAuthAccessToken(Token);
			    Authorization auth = twitter.getAuthorization();
			    HttpRequest request = new HttpRequest(RequestMethod.GET, "https://api.twitter.com/1.1/account/verify_credentials.json",null,null,null,null);
			    String header = auth.getAuthorizationHeader(request);
			    HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(url);
				post.addHeader("X-Auth-Service-Provider","https://api.twitter.com/1.1/account/verify_credentials.json");
			    post.addHeader("X-Verify-Credentials-Authorization", header);
			    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
				urlParameters.add(new BasicNameValuePair("api_key", "5963033d7991e1627a072e5380d862aa"));
				urlParameters.add(new BasicNameValuePair("text", text));
				try{
				post.setEntity(new UrlEncodedFormEntity(urlParameters,"UTF-8"));
				HttpResponse response = client.execute(post);
				System.out.println("\nSending 'POST' request to URL : " + url);
				System.out.println("Post parameters : " + post.getEntity());
				System.out.println("Response Code : " + 
		                                    response.getStatusLine().getStatusCode());
		 
				BufferedReader rd = new BufferedReader(
		                        new InputStreamReader(response.getEntity().getContent()));
		 
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
				JSONObject reply = new JSONObject(result.toString());
				composeActivity.sendTweet((String) reply.get("text_to_tweet"));
				return result.toString();
				}catch(Exception e){
					e.printStackTrace();
					return "failed";
				}
			}
			protected void onPostExecute(String result) {
				System.out.println(result);
		    }
		};
		System.out.println(token_secret);
		task.execute(text,token,token_secret);
	}
	
}
