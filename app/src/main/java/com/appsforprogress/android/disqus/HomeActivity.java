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
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;

/**
 * Created by Oswald on 3/12/2016.
 */
public class HomeActivity extends AppCompatActivity
{
    private static final String TAG = "MainMenuActivity";
    private static final String EXTRA_FIRST_NAME = "last_tab_position";
    private static final String EXTRA_LAST_NAME = "last_tab_position";
    private static final String EXTRA_IMAGE_LINK = "last_tab_position";
    private static final String LAST_TAB_POSITION = "last_tab_position";
    public static final String EXTRA_USER_PROFILE = "com.appsforprogress.android.disqus.user_profile";
    public final static int USER_TAB = 0;
    public final static int EXPLORE_TAB = 1;
    public final static int CONNECT_TAB = 2;

    private int mCurrentNavPosition;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ViewPager mViewPager;
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

        /*
        userProfile.putExtra(EXTRA_FIRST_NAME, firstName);
        userProfile.putExtra(EXTRA_LAST_NAME, lastName);
        userProfile.putExtra(EXTRA_IMAGE_LINK, profileImg);
        */

        return logInIntent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_tabs);

        mContext = getApplicationContext();

        FragmentManager fm = getSupportFragmentManager();

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Fill each DB table and create Attribute Lists per Type
        final HomeTabPagerAdapter homeTabPagerAdapter = new HomeTabPagerAdapter(fm, getResources()); // ,mMainMenuOptions[position]

        tabLayout.removeAllTabs();

        mViewPager.setAdapter(homeTabPagerAdapter);

        // Retrieve the tabs for the layout from the pagerAdapter
        // tabLayout.setTabsFromPagerAdapter(attrPagerAdapter);

        // Have the viewpager listen for tab changes:
        // mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position)
            {
                mCurrentNavPosition = position;
                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        /*Define what to do at different times when a tab is selected:
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {

            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        */


        // Want to create DB here:
        // Create the db and its empty tables and load data into tables
        // mAttributesDatabase = new AttributeDBHelper(mContext).getWritableDatabase();

        // Set the selected tab
        setSelectedTab();

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

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    public void displayView(int viewId)
    {
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        // Set the position for the elements in the Main Menu:
        switch (viewId)
        {
            case R.id.nav_menu_user:
                mCurrentNavPosition = 0;
                fragment = launchProfiler();
                break;
            // Review the Users Likes for career possibilities
            case R.id.nav_menu_explore:
                mCurrentNavPosition = 1;
                fragment = launchExplorer();
                break;
            case R.id.nav_menu_connect:
                mCurrentNavPosition = 2;
                break;
            default:
                Log.w(TAG, "Unknown navigation drawer item selected.");
                break;
        }

        if (fragment != null)
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();
        }
    }
    */

    // Used to restore the Bundle Hash
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        mCurrentNavPosition = savedInstanceState.getInt(LAST_TAB_POSITION, 0);

        final Menu menu = mNavigationView.getMenu();

        // Set the checked it to the last position saved in the bundle hash
        final MenuItem menuItem = menu.getItem(mCurrentNavPosition);

        menuItem.setChecked(true);

        setSelectedTab();

        // displayView(menuItem.getItemId());
    }

    // Save key/value entries to Bundle upon Pause
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // Save our last tab position.
        outState.putInt(LAST_TAB_POSITION, mCurrentNavPosition);
    }


    private Fragment launchProfiler()
    {
        // Intent intent = UserProfileActivity.newIntent(this);
        // startActivity(intent);
        return new UserProfileFragment();
    }

    private Fragment launchExplorer()
    {
        // Intent intent = UserProfileActivity.newIntent(this);
        // startActivity(intent);
        return new ExploreFragment();
    }


    @Override
    public void onBackPressed()
    {
        if (exit)
        {
            finish(); // finish activity
            LoginManager.getInstance().logOut();

        } else {
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

    /*
    private Fragment launchLikeGallery()
    {
        return ProfilerListFragment.newInstance();
    }
    */


    /*
    private Fragment setupAttrTabs()
    {
        return AttrTabsFragment.newInstance();
    }
    */

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
