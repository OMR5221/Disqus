package com.appsforprogress.android.disqus.database;

/**
 * Created by ORamirez on 6/3/2017.
 */

public class UserLikeDBSchema
{
    public static final class UserTable
    {
        // Table Name
        public static final String NAME = "likes";

        public static final class Cols
        {
            // Table Col Names:
            public static final String UUID = "user_id";
            public static final String LIKEID = "like_id";
        }
    }
}
