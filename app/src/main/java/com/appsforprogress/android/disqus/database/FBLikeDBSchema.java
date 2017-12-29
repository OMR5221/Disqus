package com.appsforprogress.android.disqus.database;

/**
 * Created by ORamirez on 6/3/2017.
 */

public class FBLikeDBSchema
{
    public static final class FBLikeTable
    {
        // Table Name
        public static final String NAME = "fb_like";

        public static final class Cols
        {
            // Table Col Names:

            // Disqus App Like ID:
            public static final String DQID = "like_id";
            // ID from Facebook itself:
            public static final String FBID = "fb_like_id";
            public static final String NAME = "fb_like_name";
            public static final String CATEGORY = "fb_like_category";
            public static final String PAGE_URL = "fb_like_url";
        }
    }
}
