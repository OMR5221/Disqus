package com.appsforprogress.android.disqus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.appsforprogress.android.disqus.abstracts.SingleFragmentActivity;

public class UserProfileActivity extends SingleFragmentActivity
{
    public static final String EXTRA_USER_PROFILE = "com.appsforprogress.android.disqus.user_profile";


    public static Intent upIntent(Context packageContext, String userProfile)
    {
        Intent userProfileIntent = new Intent(packageContext, UserProfileActivity.class);

        // Sending entire userProfile from login to fragment:
        userProfileIntent.putExtra(EXTRA_USER_PROFILE, userProfile);

        return userProfileIntent;
    }

    @Override
    protected Fragment createFragment()
    {
        return new UserProfileFragment();
    }
}
