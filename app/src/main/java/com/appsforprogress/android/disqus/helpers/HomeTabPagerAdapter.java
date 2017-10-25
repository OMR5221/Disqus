package com.appsforprogress.android.disqus.helpers;

import com.appsforprogress.android.disqus.ExploreFragment;
import com.appsforprogress.android.disqus.UserProfileFragment;
import android.content.res.Resources;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.appsforprogress.android.disqus.ExploreFragment;
import com.appsforprogress.android.disqus.R;
import com.appsforprogress.android.disqus.UserProfileFragment;

/**
 * Created by Oswald on 3/12/2016.
 */

// We will define the tabs displayed for the user when they select the Attributes Menu
public class HomeTabPagerAdapter extends FragmentStatePagerAdapter
{
    public enum HomeTab
    {
        PROFILE(R.string.user_menu_label),
        EXPLORE(R.string.explore_menu_label),
        CONNECT(R.string.connect_menu_label);

        // Tells us the tab selected
        private final int mStringResource;

        HomeTab(@StringRes int stringResource)
        {
            mStringResource = stringResource;
        }

        public int getStringResource()
        {
            return mStringResource;
        }
    }

    private final HomeTab[] mHomeTabs = HomeTab.values();
    private final CharSequence[] mHomeTitles = new CharSequence[mHomeTabs.length];

    // Used if we are passing a value selected from Menu into the Tab to be displayed:
    /*
        private final MainMenuOption mMainMenuType;
        private final MainMenuOption[] mMainMenuTypes = MainMenuOption.values();
    */

    public HomeTabPagerAdapter(FragmentManager fm, Resources res) //, MainMenuOption menuType
    {
        // Have the Fragment Manager handle the fragments to load into this pager
        super(fm);

        // mMainMenuType = menuType;

        // Get the title strings for the Attribute tabs:
        for (int i = 0; i < mHomeTabs.length; i++)
        {
            mHomeTitles[i] = res.getString(mHomeTabs[i].getStringResource());
        }
    }

    // Set the correct AttributeListFragment based on position:
    public Fragment getItem(int position)
    {
        Fragment tabFragment = null;

        // Automatically creates the AttributeTabFragment with the intended AttributeType:
        switch (position)
        {
            case 0:
                // Create a new Attribute List fragment for each tab position:
                tabFragment = UserProfileFragment.newInstance();
                break;
            case 1:
                // Create a new Attribute List fragment for each tab position:
                tabFragment = ExploreFragment.newInstance();
                break;
            case 2:
                //
                tabFragment = UserProfileFragment.newInstance();
                break;
            default:
                // User Profile is the default
                tabFragment = UserProfileFragment.newInstance();
                break;
        }

        return tabFragment;
    }

    public int getCount()
    {
        return mHomeTabs.length;
    }

    public CharSequence getPageTitle(int position)
    {
        return mHomeTitles[position];
    }

    // Generate a simple UUID for the tab selected from the Attribute Menu and the tab position index:
    public long getTabPosition(int position)
    {

        switch (position)
        {
            case 0: // EXPLORE
            case 1: // CONNECT
            case 2: // USER
                return position;
        }
        throw new IllegalArgumentException("Unhandled tab position selected: " + position + " or Menu Option");
    }
}
