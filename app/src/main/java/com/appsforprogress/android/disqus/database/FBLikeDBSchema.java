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
            public static final String FBLIKE_ID = "fb_like_id";
            public static final String FBLIKE_NAME = "fb_like_name";
            public static final String FBLIKE_CATEGORY = "fb_like_category";
            public static final String FBLIKE_PAGE_URL = "fb_like_url";
        }
    }
}
