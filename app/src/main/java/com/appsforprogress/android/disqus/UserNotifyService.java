package com.appsforprogress.android.disqus;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.appsforprogress.android.disqus.objects.FBLike;

import java.util.List;

/**
 * Created by ORamirez on 11/18/2017.
 */

public class UserNotifyService extends IntentService
{
    private static final String TAG = "UserNotifyService";

    private static final int SEARCH_INTERVAL = 1000 * 60; // 60 seconds

    // Used by other activities to start this notification:
    public static Intent newIntent(Context context)
    {
        return new Intent(context,  UserNotifyService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn)
    {
        Intent i = UserNotifyService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn)
        {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), SEARCH_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public UserNotifyService()
    {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (!isNetworkAvailableAndConnected())
        {
            return;
        }

        // Log.i(TAG, "Received an Intent: " + intent);

        // Get last search query:
        String query = QueryPreferences.getStoredQuery(this);

        // gte last saved search result id:
        String lastSearchResultId = QueryPreferences.getLastSearchId(this);

        List<FBLike> fbLikeItems;

        // Rerun the last search:
        fbLikeItems = new FBPageFetcher().search(query);

        if (fbLikeItems.size() == 0)
        {
            return;
        }

        // Get id of the first returned item
        String resultId = fbLikeItems.get(0).getId();

        // If new id then more results have been returned:
        if (resultId.equals(lastSearchResultId))
        {
            Log.i(TAG, "Got an old result: " + resultId);
        }
        else {
            Log.i(TAG, "Got a new result id: " + resultId);
        }

        // Update the last id saved:
        QueryPreferences.setLastSearchId(this, resultId);
    }

    // Check if the device is connected to wireless:
    private boolean isNetworkAvailableAndConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;

    }
}
