package com.appsforprogress.android.disqus.helpers;

/**
 * Created by oswal on 2/16/2018.
 */

/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 * <p>
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 * <p>
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import android.net.Uri;
import android.os.Bundle;
import com.appsforprogress.android.disqus.objects.FBLike;
import com.appsforprogress.android.disqus.objects.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class GetUserCallback
{

    public interface IGetUserResponse
    {
        void onCompleted(User user);
    }

    private IGetUserResponse mGetUserResponse;
    private CallbackManager callbackManager;
    private AccessToken mAccessToken;
    private Profile mProfile;
    private ArrayList<String> mPermissions;

    FacebookCallback<LoginResult> loginResultFacebookCallback = new FacebookCallback<LoginResult>()
    {

        @Override
        public void onSuccess(final LoginResult loginResult)
        {
            // Save Access Taken in SharedPreference:
            // FBAccessTokenPreferences.setStoredAccessToken(get, loginResult.getAccessToken().toString());

            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback()
                    {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response)
                        {
                            try
                            {
                                // Successful Login: Start HomeActivity with User Profile selected
                                jsonToUser(object);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id, name, email, picture.width(120).height(120), likes");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel()
        {
            // LoginManager.getInstance().logOut();
        }

        @Override
        public void onError(FacebookException e)
        {
            // Toast.makeText(getActivity(), "Something went wrong, please try again later", Toast.LENGTH_LONG).show();
        }
    };

    public GetUserCallback(final IGetUserResponse getUserResponse)
    {

        mGetUserResponse = getUserResponse;
        mPermissions = new ArrayList<>(Arrays.asList("public_profile", "email", "user_friends", "user_likes"));
        callbackManager = CallbackManager.Factory.create();

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null)
        {
            if (accessToken.isExpired())
            {
                LoginManager.getInstance().logOut();
            }
            else
            {
                /// This is code responsible for re-login
                // LoginManager.getInstance().logInWithReadPermissions(HomeActivity.class, mPermissions);
                LoginManager.getInstance().registerCallback(callbackManager, loginResultFacebookCallback);
            }
        }
    }

    private User jsonToUser(JSONObject user) throws JSONException
    {
        Uri picture = Uri.parse(user.getJSONObject("picture").getJSONObject("data").getString("url"));
        String name = user.getString("name");
        String id = user.getString("id");
        String email = null;

        if (user.has("email"))
        {
            email = user.getString("email");
        }

        // Build permissions display string
        StringBuilder builder = new StringBuilder();
        JSONArray perms = user.getJSONObject("permissions").getJSONArray("data");
        builder.append("Permissions:\n");

        for (int i = 0; i < perms.length(); i++)
        {
            builder.append(perms.getJSONObject(i).get("permission")).append(": ")
                    .append(perms.getJSONObject(i).get("status")).append("\n");
        }

        String permissions = builder.toString();

        // Get User Likes:

        // convert Json object into Json array
        JSONArray likes = user.getJSONObject("likes").optJSONArray("data");

        // Reloads list to prevent dupes upon rotation:
        // How to call refresh from network?
        ArrayList userLikes = new ArrayList<FBLike>();

        try
        {
            // LOOP through retrieved JSON posts:
            for (int i = 0; i <= 11; i++)
            {
                JSONObject like = likes.optJSONObject(i);

                String likeID = like.optString("id");
                // String category = post.optString("category");
                String likeName = like.optString("name");

                int count = like.optInt("likes");
                // print id, page name and number of like of facebook page
                // Log.e("id: ", id + " (name: " + name + " , category: "+ category + " likes count - " + count);

                FBLike fbLike = new FBLike();
                fbLike.setFBID(id);
                // fbLike.setCategory(category);
                fbLike.setName(name);

                URL imageURL = new URL("https://graph.facebook.com/" + id + "/picture?type=large");
                // Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                fbLike.setPicURL(imageURL.toString());

                // Add each like to a List
                userLikes.add(fbLike);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


        return new User(picture, name, id, email, permissions, userLikes);
    }

    public FacebookCallback<LoginResult> getCallback() {
        return loginResultFacebookCallback;
    }
}