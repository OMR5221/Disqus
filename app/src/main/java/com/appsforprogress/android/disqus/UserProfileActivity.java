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
        userProfileIntent.putExtra(EXTRA_USER_PROFILE, userProfile);
        /*
        userProfile.putExtra(EXTRA_FIRST_NAME, firstName);
        userProfile.putExtra(EXTRA_LAST_NAME, lastName);
        userProfile.putExtra(EXTRA_IMAGE_LINK, profileImg);
        */

        return userProfileIntent;
    }

    @Override
    protected Fragment createFragment()
    {
        // return new UserProfileFragment();

        // UUID userId = (UUID) getIntent().getSerializableExtra(EXTRA_USER_ID);

        return new UserProfileFragment();
    }
}
