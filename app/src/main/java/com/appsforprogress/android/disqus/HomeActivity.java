package com.appsforprogress.android.disqus;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.appsforprogress.android.disqus.helpers.HomeOptions;
import com.appsforprogress.android.disqus.helpers.HomeTabPagerAdapter;
import com.appsforprogress.android.disqus.helpers.NoSwipeViewPager;
import com.appsforprogress.android.disqus.helpers.QueryPreferences;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;

/**
 * Created by Oswald on 3/12/2016.
 */
public class HomeActivity extends AppCompatActivity
{
    private static final String TAG = "MainMenuActivity";
    private static final String LAST_TAB_POSITION = "last_tab_position";
    public static final String EXTRA_USER_PROFILE = "com.appsforprogress.android.disqus.user_profile";
    public final static int USER_TAB = 0;
    public final static int EXPLORE_TAB = 1;
    public final static int CONNECT_TAB = 2;

    private int mCurrentNavPosition;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private NoSwipeViewPager mViewPager;
    private Boolean exit = false;

    private Toolbar mMainMenuToolbar;
    private HomeOptions[] mHomeOptions = HomeOptions.values();

    // For Database Usage:
    private Context mContext;
    private SQLiteDatabase mAttributesDatabase;

    // For FaceBook Login:
    CallbackManager mCallbackManager;


    public static Intent logInIntent(Context packageContext, String userProfile)
    {
        Intent logInIntent = new Intent(packageContext, HomeActivity.class);
        logInIntent.putExtra(EXTRA_USER_PROFILE, userProfile);
        logInIntent.putExtra(LAST_TAB_POSITION, 0);

        QueryPreferences.setStoredProfile(packageContext, userProfile);

        return logInIntent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_tabs);

        // If MainActivity is reached without the user being logged in, redirect to the Login
        // Activity
        if (AccessToken.getCurrentAccessToken() == null)
        {
            Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
        else
        {
            mContext = getApplicationContext();

            FragmentManager fm = getSupportFragmentManager();

            mViewPager = (NoSwipeViewPager) findViewById(R.id.viewpager);
            // Disable Swiping on main screens:
            mViewPager.setPagingEnabled(false);

            final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

            // Fill each DB table and create Attribute Lists per Type
            final HomeTabPagerAdapter homeTabPagerAdapter = new HomeTabPagerAdapter(fm, getResources()); // ,mMainMenuOptions[position]

            tabLayout.removeAllTabs();

            mViewPager.setAdapter(homeTabPagerAdapter);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mCurrentNavPosition = position;
                    mViewPager.setCurrentItem(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });


            // Want to create DB here:
            // Create the db and its empty tables and load data into tables
            // mAttributesDatabase = new AttributeDBHelper(mContext).getWritableDatabase();

            // Set the selected tab
            setSelectedTab();
        }
    }


    // Reads selected tab from launching intent and
    // sets page accordingly
    public void setSelectedTab()
    {
        // Fetch the selected tab index with default
        int selectedTabIndex = getIntent().getIntExtra(LAST_TAB_POSITION, USER_TAB);
        // Switch to page based on index
        mViewPager.setCurrentItem(selectedTabIndex);
    }

    // Used to restore the Bundle Hash
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        mCurrentNavPosition = savedInstanceState.getInt(LAST_TAB_POSITION, 0);

        setSelectedTab();
    }

    // Save key/value entries to Bundle upon Pause
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // Save our last tab position.
        outState.putInt(LAST_TAB_POSITION, mCurrentNavPosition);
    }

    @Override
    public void onBackPressed()
    {
        if (exit)
        {
            finish(); // finish activity
            LoginManager.getInstance().logOut();
        }
        else
        {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        /*
        if (isLoggedIn())
        {
            // use active session:
            // Session session = Session.getActiveSession();

        }
        else
        {
            LoginManager.getInstance().logOut();
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
            this.finish();
        }
        */
    }

    public boolean isLoggedIn()
    {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if(accessToken.isExpired())
        {
            return false;
        }
        else
        {
            return true;
        }

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // Save Profile info:

    }

    /*
    public void onValidLogIn(Profile userProfile)
    {
        // User logged in: Call the fragment displaying their logged profile
        // new UserProfileLogoutFragment.newInstance();

        // Otherwise, we're in the one-pane layout and must swap frags...

        // Create fragment and give it an argument for the selected article
        UserProfileLogOutFragment logoutFragment = new UserProfileLogOutFragment();
        Bundle args = new Bundle();
        args.putInt(UserProfileLogOutFragment.ARG_PROFILE, userProfile);
        logoutFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, logoutFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
    */
}
