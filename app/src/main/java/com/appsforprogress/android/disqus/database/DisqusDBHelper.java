package com.appsforprogress.android.disqus.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.appsforprogress.android.disqus.objects.FBLike;


/**
 * Created by ORamirez on 6/3/2017.
 */

public class DisqusDBHelper extends SQLiteOpenHelper
{
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "disqus.db";

    public DisqusDBHelper(Context context)
    {
         super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // Create User Table:
        db.execSQL( "create table " + UserDBSchema.UserTable.NAME +
                    "(" +
                        " _id integer primary key autoincrement, " +
                        UserDBSchema.UserTable.Cols.UUID + ", " +
                        UserDBSchema.UserTable.Cols.FNAME + ", " +
                        UserDBSchema.UserTable.Cols.LNAME + ", " +
                        UserDBSchema.UserTable.Cols.EMAIL +
                    ")"
        );

        // Create FB Likes Table:
        db.execSQL( "create table " + FBLikeDBSchema.FBLikeTable.NAME +
                    "(" +
                        " _id integer primary key autoincrement, " +
                        FBLikeDBSchema.FBLikeTable.Cols.FBLIKE_ID + ", " +
                        FBLikeDBSchema.FBLikeTable.Cols.FBLIKE_NAME + ", " +
                        FBLikeDBSchema.FBLikeTable.Cols.FBLIKE_URL +
                    ")"
        );

        // Create User Like Table:
        db.execSQL( "create table " + UserLikeDBSchema.UserLikeTable.NAME +
                    "(" +
                        " _id integer primary key autoincrement, " +
                        UserLikeDBSchema.UserLikeTable.Cols.DQID + ", " +
                        UserLikeDBSchema.UserLikeTable.Cols.FBID +
                    ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
