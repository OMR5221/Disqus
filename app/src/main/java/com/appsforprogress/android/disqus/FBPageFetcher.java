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
import java.net.MalformedURLException;
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
    List<FBLike> mFBSearchItems = new ArrayList<>();


    public List<FBLike> search(String query)
    {
        List<FBLike> fbSearchItems = new ArrayList<>();

        try {
            GraphRequest request = GraphRequest.newGraphPathRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/search",
                    new GraphRequest.Callback()
                    {
                        @Override
                        public void onCompleted(GraphResponse response)
                        {
                            // Insert your code here:
                            parseSearchResults(response);
                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("q", query);
            parameters.putString("type", "page");
            request.setParameters(parameters);
            request.executeAsync();

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return fbSearchItems;
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
                        parseSearchResults(response);
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("q", query);
        parameters.putString("type", "page");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void parseSearchResults(GraphResponse response)
    {
        try {
            JSONArray rawSearchResults = response.getJSONObject().getJSONArray("data");

            for (int i = 0; i <= 9; i++) {
                // Get FB Page Item
                JSONObject fbPageObject = rawSearchResults.getJSONObject(i);

                // Set FB Like Object settings:
                FBLike fbLikeItem = new FBLike();
                fbLikeItem.setId(fbPageObject.getString("id"));
                fbLikeItem.setName(fbPageObject.getString("name"));
                try {
                    URL imageURL = new URL("https://graph.facebook.com/" + fbPageObject.getString("id") + "/picture?type=large");
                    fbLikeItem.setPicURL(imageURL);
                } catch (MalformedURLException me) {
                    me.printStackTrace();
                }

                mFBSearchItems.add(fbLikeItem);
            }
        }
        catch (JSONException je)
        {
            je.printStackTrace();
        }
    }

}


