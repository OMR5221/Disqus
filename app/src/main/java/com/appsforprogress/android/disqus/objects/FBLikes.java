package com.appsforprogress.android.disqus.objects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.appsforprogress.android.disqus.database.DisqusDBHelper;
import com.appsforprogress.android.disqus.database.FBLikeCursorWrapper;
import com.appsforprogress.android.disqus.database.FBLikeDBSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by ORamirez on 12/29/2017.
 */
// Holds all user liked pages: Facebook orientated implementation
public class FBLikes
{
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private static FBLikes sFBLikes;


    private FBLikes(Context context)
    {
        mContext = context.getApplicationContext();
        // Construct DB for writing/reading uses: Want to do this for the USer class only?
        mDatabase = new DisqusDBHelper(mContext).getWritableDatabase();

    }

    // Return the DB Table instance:
    public static FBLikes getInstance(Context context)
    {
        if (sFBLikes == null) {
            sFBLikes = new FBLikes(context);
        }

        return sFBLikes;
    }

    public void setFBLike(FBLike fbLike)
    {
        // Get row record:
        ContentValues cVals = getContentValues(fbLike);

        // Insert record into FBLike Table:
        mDatabase.insert(FBLikeDBSchema.FBLikeTable.NAME, null, cVals);
    }


    public void setFBLikes(List<FBLike> fbLikes)
    {
        for (int i = 0; i < fbLikes.size(); i++)
        {
            FBLike fbLike = fbLikes.get(i);

            // Get row record:
            ContentValues cVals = getContentValues(fbLike);

            // Insert record into FBLike Table:
            mDatabase.insert(FBLikeDBSchema.FBLikeTable.NAME, null, cVals);
        }
    }

    public void delFBLikes()
    {
        mDatabase.delete(FBLikeDBSchema.FBLikeTable.NAME, null,null);
    }

    public List<FBLike> getAllFBLikes()
    {
        List<FBLike> fbLikes = new ArrayList<>();

        // Get everything form the DB:
        FBLikeCursorWrapper fbCursor = queryFBLikes(null, null);

        try {
            fbCursor.moveToFirst();

            // Continue pulling rows and create FBLike Object until complete:
            while (!fbCursor.isAfterLast())
            {
                fbLikes.add(fbCursor.createFBLike());
                fbCursor.moveToNext();
            }
        } finally {
            fbCursor.close();
        }

        return fbLikes;
    }

    // Read a specific like's properties: Lookup to FBLIke Table?
    public static FBLike getAFBLike(String fbLikeId)
    {
        // Get from the FBLike Table
        return null;
    }

    // Update a FBLIKE record in the DB:
    public void updateFBLike(FBLike fbLike)
    {
        String fbLikeID = fbLike.getFBID().toString();
        ContentValues cVals = getContentValues(fbLike);

        // Change the FBLike ID:
        mDatabase.update(FBLikeDBSchema.FBLikeTable.NAME, cVals,
                FBLikeDBSchema.FBLikeTable.Cols.FBLIKE_ID + " = ?",
                new String[]{fbLikeID});
    }


    // Update all FBLIKE records in the DB:
    public void updateFBLikes(List<FBLike> fbLikes)
    {
        for (int i = 0; i < fbLikes.size(); i++)
        {
            FBLike fbLike = fbLikes.get(i);
            String fbLikeID = fbLike.getFBID().toString();
            ContentValues cVals = getContentValues(fbLike);

            // Change the FBLike ID:
            mDatabase.update(FBLikeDBSchema.FBLikeTable.NAME, cVals,
                    FBLikeDBSchema.FBLikeTable.Cols.FBLIKE_ID + " = ?",
                    new String[]{fbLikeID});
        }
    }


    // Create hash to associate key (column name in DB) to th value form the object:
    private static ContentValues getContentValues(FBLike fbLike) {
        ContentValues cVals = new ContentValues();
        cVals.put(FBLikeDBSchema.FBLikeTable.Cols.FBLIKE_ID, fbLike.getFBID());
        cVals.put(FBLikeDBSchema.FBLikeTable.Cols.FBLIKE_NAME, fbLike.getName());
        cVals.put(FBLikeDBSchema.FBLikeTable.Cols.FBLIKE_URL, fbLike.getPicURL().toString());

        return cVals;
    }

    // Get records from DB Table:
    private FBLikeCursorWrapper queryFBLikes(String whereClause, String[] whereArgs)
    {
        Cursor cursor = mDatabase.query(
                FBLikeDBSchema.FBLikeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        // Use FBLikeCursor Wrapper to pull from DB and return FBLike Object(s)
        return new FBLikeCursorWrapper(cursor);
    }


}
