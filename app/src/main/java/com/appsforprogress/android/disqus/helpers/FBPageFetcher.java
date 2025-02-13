package com.appsforprogress.android.disqus.helpers;

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
    private List<FBLike> mFBSearchItems = new ArrayList<>();
    private String mSearchQuery;

    public List<FBLike> prepareSearch(String query)
    {
        mSearchQuery = query;

        try {

            GraphRequest request = GraphRequest.newGraphPathRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/search",
                    new GraphRequest.Callback()
                    {
                        @Override
                        public void onCompleted(GraphResponse response)
                        {
                            performSearch(mFBSearchItems, mSearchQuery, response);
                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("q", query);
            parameters.putString("type", "page");
            parameters.putString("fields", "is_verified,name,id");
            request.setParameters(parameters);
            request.executeAsync();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return mFBSearchItems;
    }

    private void performSearch(List<FBLike> fbLikes, String query, GraphResponse response)
    {
        // Insert your code here:
        try {
            JSONArray rawSearchResults = response.getJSONObject().getJSONArray("data");

            for (int i = 0; i <= rawSearchResults.length(); i++) {
                // Get FB Page Item
                JSONObject fbPageObject = rawSearchResults.getJSONObject(i);

                if (fbPageObject.getString("is_verified") == "true") {
                    // Set FB Like Object settings:
                    FBLike fbLikeItem = new FBLike();
                    fbLikeItem.setFBID(fbPageObject.getString("id"));
                    fbLikeItem.setName(fbPageObject.getString("name"));

                    try {
                        URL imageURL = new URL("https://graph.facebook.com/" + fbPageObject.getString("id") + "/picture?type=large");
                        fbLikeItem.setPicURL(imageURL.toString());
                    } catch (MalformedURLException me) {
                        me.printStackTrace();
                    }

                    fbLikes.add(fbLikeItem);
                }
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }
}



