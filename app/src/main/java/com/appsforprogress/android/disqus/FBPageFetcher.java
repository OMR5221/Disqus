package com.appsforprogress.android.disqus;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.appsforprogress.android.disqus.objects.FBLike;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

/**
 * Created by ORamirez on 10/21/2017.
 */

public class FBPageFetcher
{
    private static final String TAG = "FBPageFetcher";

    public List<FBLike> searchFBPages(String query)
    {
        String url = buildUrl(query);
        return fetchFBPages(url);
    }


    private List<FBLike> fetchFBPages(String url)
    {
        List <FBLike> fbPages = new ArrayList<>();
        JSONObject json;

        try {
            // String jsonString = getUrlString(url);
            InputStream is = new URL(url.toString()).openStream();

            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = readAll(rd);
                json = new JSONObject(jsonText);
                //return json;
            } finally {
                is.close();
            }
            //Log.i(TAG, "Received JSON: " + jsonString);
            //JSONObject jsonBody = new JSONObject(jsonString);
            parseFBPages(fbPages, json);

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return fbPages;
    }


    private void getSearchResults(String query)
    {
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/search",
                new GraphRequest.Callback()
                {
                    @Override
                    public void onCompleted(GraphResponse response)
                    {
                        // Insert your code here:
                        // Log.e(TAG,response.toString());
                        parseSearchResults();
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("q", "Google");
        parameters.putString("type", "page");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void parseSearchResults(List<FBLike> fbPages, JSONObject jsonBody)
            throws IOException, JSONException
    {
        JSONArray fbPageJSONArray = jsonBody.getJSONObject("").getJSONArray("data");

        for (int i = 0; i < fbPageJSONArray.length(); i++)
        {
            // Get FB Page Item
            JSONObject fbPageObject = fbPageJSONArray.getJSONObject(i);

            // Set FB Like Object settings:
            FBLike fbLikeItem = new FBLike();
            fbLikeItem.setId(fbPageObject.getString("id"));
            fbLikeItem.setName(fbPageObject.getString("name"));
            URL imageURL = new URL("https://graph.facebook.com/" + fbPageObject.getString("id") + "/picture?type=large");
            fbLikeItem.setPicURL(imageURL);

            fbPages.add(fbLikeItem);
        }
    }

}


