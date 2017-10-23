package com.appsforprogress.android.disqus;

import android.net.Uri;
import android.util.Log;

import com.appsforprogress.android.disqus.objects.FBLike;
import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ORamirez on 10/21/2017.
 */

public class FBPageFetcher
{
    private static final String TAG = "FBPageFetcher";
    private static final String SEARCH_METHOD = "facebook.pages.search";
    /*private static final Uri ENDPOINT = Uri
            .parse("https://graph.facebook.com/search?")
            .buildUpon()
            .appendQueryParameter("type", "page")
            .appendQueryParameter("access_token", AccessToken.getCurrentAccessToken().toString())
            .build();*/


    public List<FBLike> searchFBPages(String query)
    {
        String url = buildUrl(query);
        return fetchFBPages(url);
    }

    private String buildUrl(String query)
    {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("graph.facebook.com")
                .appendPath("search")
                .appendQueryParameter("type", "page")
                .appendQueryParameter("access_token", AccessToken.getCurrentAccessToken().toString())
                .appendQueryParameter("q", query)
                .build();

        Uri uriBuilder = builder.build();

        // Uri pictureUri = builder.build();
        // Uri.Builder uriBuilder = ENDPOINT.buildUpon()
        //.appendQueryParameter("q", query);

        return uriBuilder.toString();
    }

    private List<FBLike> fetchFBPages(String url)
    {
        List <FBLike> fbPages = new ArrayList<>();

        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseFBPages(fbPages, jsonBody);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return fbPages;
    }


    public String getUrlString(String urlSpec) throws IOException
    {
        return new String(getUrlBytes(urlSpec));
    }

    // Create a URL object from a String:
    public byte[] getUrlBytes(String urlSpec) throws IOException
    {
        URL url = new URL(urlSpec);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            // connection failed:
            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK)
            {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            // While bytes to read continue:
            while ( (bytesRead = in.read(buffer)) > 0 )
            {
                out.write(buffer, 0, bytesRead);
            }

            out.close();

            return out.toByteArray();

        } finally {
            connection.disconnect();
        }
    }

    private void parseFBPages(List<FBLike> fbPages, JSONObject jsonBody)
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


