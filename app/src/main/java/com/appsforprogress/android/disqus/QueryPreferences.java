package com.appsforprogress.android.disqus;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by ORamirez on 10/29/2017.
 */

public class QueryPreferences
{
    // Key for HASH:
    private static final String PREF_SEARCH_QUERY = "searchQuery";

    // Return search query in SharedPref Hash
    public static String getStoredQuery(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, "Google");
    }

    // Save last search Query in SharedPref
    public static void setStoredQuery(Context context, String query)
    {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }
}
