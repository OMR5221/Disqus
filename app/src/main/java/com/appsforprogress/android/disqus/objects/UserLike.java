package com.appsforprogress.android.disqus.objects;

import java.net.URL;

/**
 * Created by ORamirez on 7/4/2016.
 */
public class UserLike
{
    private String mDQId;
    private String mFBId;

    public String getDQId() {
        return mDQId;
    }

    public String getFBId() {
        return mFBId;
    }

    public void setDQId(String id) {
        mDQId = id;
    }

    public void setFBId(String id) {
        mFBId = id;
    }

}
