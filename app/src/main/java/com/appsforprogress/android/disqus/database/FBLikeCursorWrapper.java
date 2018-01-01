package com.appsforprogress.android.disqus.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.appsforprogress.android.disqus.objects.FBLike;

/**
 * Created by ORamirez on 12/30/2017.
 */

public class FBLikeCursorWrapper extends CursorWrapper
{
    public FBLikeCursorWrapper(Cursor cursor)
    {
        super(cursor);
    }

    // Get the FBLike data and format for the creation of a FBLike Object:
    public FBLike createFBLike()
    {
        String fbIDString = getString(getColumnIndex(FBLikeDBSchema.FBLikeTable.Cols.FBLIKE_ID));
        String name = getString(getColumnIndex(FBLikeDBSchema.FBLikeTable.Cols.FBLIKE_NAME));
        String url = getString(getColumnIndex(FBLikeDBSchema.FBLikeTable.Cols.FBLIKE_URL));

        FBLike fbLike = new FBLike();
        fbLike.setFBID(fbIDString);
        fbLike.setName(name);
        fbLike.setPicURL(url);

        return fbLike;
    }
}
