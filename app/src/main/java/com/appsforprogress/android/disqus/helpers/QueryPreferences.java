package com.appsforprogress.android.disqus.helpers;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by ORamirez on 10/29/2017.
 */

public class QueryPreferences
{
    // Key for HASH:
    private static final String PREF_SEARCH_QUERY = "searchQuery";
    private static final String PREF_USER_PROFILE = "userProfile";
    private static final String PREF_SEARCH_RESULT_ID = "searchResultId";
    private static final String PREF_LIKE_COUNT = "likeCount";

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


    // Return search query in SharedPref Hash
    public static String getStoredProfile(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_USER_PROFILE, null);
    }

    // Save last search Query in SharedPref
    public static void setStoredProfile(Context context, String userProfile)
    {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_USER_PROFILE, userProfile)
                .apply();
    }

    // Return search query in SharedPref Hash
    public static String getLastSearchId(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_RESULT_ID, null);
    }

    // Save last search Query in SharedPref
    public static void setLastSearchId(Context context, String query)
    {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_RESULT_ID, query)
                .apply();
    }

    public static Integer getLastLikeCount(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_LIKE_COUNT, 0);
    }

    public static void setLastLikeCount(Context context, Integer lastLikeCount)
    {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_LIKE_COUNT, lastLikeCount)
                .apply();
    }

    public static void deleteAllSharePrefs(Context context)
    {
        context.getSharedPreferences(PREF_SEARCH_QUERY, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
