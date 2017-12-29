package com.appsforprogress.android.disqus;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.appsforprogress.android.disqus.database.DisqusDBHelper;
import com.appsforprogress.android.disqus.database.UserLikeDBSchema;
import com.appsforprogress.android.disqus.objects.FBLike;
import com.appsforprogress.android.disqus.objects.User;
import com.appsforprogress.android.disqus.objects.UserLike;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by ORamirez on 12/29/2017.
 */
// Holds all user liked pages: Facebook orientated implementation
public class UserLikes
{
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private UserLikes(Context context)
    {
        mContext = context.getApplicationContext();
        // Construct DB for writing/reading uses: Want to do this for the USer class only?
        mDatabase = new DisqusDBHelper(mContext).getWritableDatabase();

    }

    // Return the DB Table instance:
    public static UserLikes getInstance(Context context)
    {
        return;
    }

    public void setUserLike(UserLike userLike)
    {

    }

    public List<UserLike> getAllUserLikes()
    {
        return new ArrayList<>();
    }

    // Read a specific like's properties: Lookup to FBLIke Table?
    public static UserLike getAUserLike(UUID dqLikeId)
    {
        return null;
    }

    // Read a specific like's properties: Lookup to FBLIke Table?
    public static FBLike getAFBLike(UUID fbLikeId)
    {
        // Get from the FBLike Table
        return null;
    }

        // Create hash to associate key (column name in DB) to th value form the object:
    private static ContentValues getContentValues(UserLike userLike)
    {
        ContentValues cVals = new ContentValues();
        cVals.put(UserLikeDBSchema.UserLikeTable.Cols.DQID, userLike.getDQId());
        cVals.put(UserLikeDBSchema.UserLikeTable.Cols.FBID, userLike.getFBId());

        return cVals;
    }

}
