package com.appsforprogress.android.disqus.objects;

import java.net.URL;

/**
 * Created by ORamirez on 7/4/2016.
 */
public class FBLike
{
    private String mCategory;
    private String mName;
    private String mPicURL;
    private String mFBID;

    public FBLike()
    {
    }

    public String getFBID() {
        return mFBID;
    }

    public void setFBID(String id) {
        mFBID = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public String getPicURL() {
        return mPicURL;
    }

    public void setPicURL(String picURLString) {
        mPicURL = picURLString;
    }
}
