<<<<<<< HEAD:app/src/main/java/com/appsforprogress/android/disqus/helpers/DownloadImage.java
package com.appsforprogress.android.disqus.helpers;
=======
package com.appsforprogress.android.disqus;
>>>>>>> fe270979a4433fe2b5bac53b5063147d0f33e260:app/src/main/java/com/appsforprogress/android/careerpath/DownloadImage.java

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by ORamirez on 6/20/2016.
 */
public class DownloadImage extends AsyncTask<String, Void, Bitmap>
{
    ImageView bmImage;

    public DownloadImage(ImageView bmImage)
    {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        }
        catch (Exception e)
        {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}