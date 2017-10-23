<<<<<<< HEAD:app/src/main/java/com/appsforprogress/android/disqus/objects/FBLike.java
package com.appsforprogress.android.disqus.objects;
=======
package com.appsforprogress.android.disqus;
>>>>>>> fe270979a4433fe2b5bac53b5063147d0f33e260:app/src/main/java/com/appsforprogress/android/careerpath/FBLike.java

import java.net.URL;

/**
 * Created by ORamirez on 7/4/2016.
 */
public class FBLike
{
    private String mId;
    private String mCategory;
    private String mName;
    private URL mPicURL;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
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

    public URL getPicURL() {
        return mPicURL;
    }

    public void setPicURL(URL picURL) {
        mPicURL = picURL;
    }
}
