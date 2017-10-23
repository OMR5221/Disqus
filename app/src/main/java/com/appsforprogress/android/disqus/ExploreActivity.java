package com.appsforprogress.android.disqus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.appsforprogress.android.disqus.abstracts.SingleFragmentActivity;

public class ExploreActivity extends SingleFragmentActivity
{
    public static Intent exploreIntent(Context packageContext, String userProfile)
    {
        Intent expIntent = new Intent(packageContext, ExploreActivity.class);
        return expIntent;
    }

    @Override
    protected Fragment createFragment()
    {
        return new ExploreFragment();
    }
}
