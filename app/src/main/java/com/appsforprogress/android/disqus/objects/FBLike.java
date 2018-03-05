package com.appsforprogress.android.disqus.objects;

import java.net.URL;

/**
 * Created by ORamirez on 7/4/2016.
 */
public class FBLike
{
    private String mCategory;
    private String mName;
    private String mFBPageURL;
    private String mFBPageID;
    private String index;

    public FBLike()
    {
    }

    public FBLike(String category, String fbID, String name, String pageURL)
    {
        this.mName = name;
        this.mCategory = category;
        this.mFBPageID = fbID;
        this.index = "not_specified";
        this.mFBPageURL = pageURL;
    }

    public String getFBID() {
        return mFBPageID;
    }

    public void setFBID(String id) {
        mFBPageID = id;
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
        return mFBPageURL;
    }

    public void setPicURL(String picURLString) {
        mFBPageURL = picURLString;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
