package com.appsforprogress.android.disqus.helpers;

/**
 * Created by oswal on 2/16/2018.
 */

import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;

public class UserRequest
{
    private static final String ME_ENDPOINT = "/me";

    public static void makeUserRequest(GraphRequest.Callback callback)
    {
        Bundle params = new Bundle();
        params.putString("fields", "picture,name,id,email,permissions,user_likes");

        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                ME_ENDPOINT,
                params,
                HttpMethod.GET,
                callback
        );
        request.executeAsync();
    }
}