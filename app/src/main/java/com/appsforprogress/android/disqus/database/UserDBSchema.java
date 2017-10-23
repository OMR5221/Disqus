package com.appsforprogress.android.disqus.database;

/**
 * Created by ORamirez on 6/3/2017.
 */

public class UserDBSchema
{
    public static final class UserTable
    {
        // Table Name
        public static final String NAME = "users";

        public static final class Cols
        {
            // Table Col Names:
            public static final String UUID = "user_id";
            public static final String HASHED_PASSWORD = "password";
            public static final String EMAIL = "email";
            public static final String FNAME = "first_name";
            public static final String LNAME = "last_name";
        }
    }
}
