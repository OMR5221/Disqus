<<<<<<< HEAD:app/src/main/java/com/appsforprogress/android/disqus/abstracts/ToolbarFragmentActivity.java
package com.appsforprogress.android.disqus.abstracts;
=======
package com.appsforprogress.android.disqus;
>>>>>>> fe270979a4433fe2b5bac53b5063147d0f33e260:app/src/main/java/com/appsforprogress/android/careerpath/ToolbarFragmentActivity.java

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.appsforprogress.android.disqus.R;

/**
 * Created by ORamirez on 6/14/2017.
 */

// Abstract Activity that can hold any fragment instance:
public abstract class ToolbarFragmentActivity extends AppCompatActivity
{
    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        // Get the Fragment Manager:
        FragmentManager fm = getSupportFragmentManager();

        // Give a fragment to Fragment Manager:
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null)
        {
            fragment = createFragment();

            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
