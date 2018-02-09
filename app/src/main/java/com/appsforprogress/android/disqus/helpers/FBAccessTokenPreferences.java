package com.appsforprogress.android.disqus.helpers;

import android.content.Context;
import android.preference.PreferenceManager;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

/**
 * Created by ORamirez on 10/29/2017.
 */

public class FBAccessTokenPreferences
{
    // Key for HASH:
    private static final String PREF_FB_ACCESS_TOKEN = "fbAccessToken";

    // Return search query in SharedPref Hash
    public static String getStoredAccessToken(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_FB_ACCESS_TOKEN, NULL);
    }

    // Save last search Query in SharedPref
    public static void setStoredAccessToken(Context context, String query)
    {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_FB_ACCESS_TOKEN, query)
                .apply();
    }

    public static void deleteAccessToken(Context context)
    {
        context.getSharedPreferences(PREF_FB_ACCESS_TOKEN, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
