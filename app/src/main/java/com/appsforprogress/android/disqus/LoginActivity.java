package com.appsforprogress.android.disqus;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.appsforprogress.android.disqus.abstracts.SingleFragmentActivity;

public class LoginActivity extends SingleFragmentActivity
{
    @Override
    protected Fragment createFragment()
    {
        // return new UserProfileFragment();

        // UUID userId = (UUID) getIntent().getSerializableExtra(EXTRA_USER_ID);

        return LoginFragment.newInstance();
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            //System.out.println("@#@");
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}

