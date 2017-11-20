package com.appsforprogress.android.disqus;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.appsforprogress.android.disqus.helpers.DownloadImage;
import com.appsforprogress.android.disqus.objects.FBLike;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oswald on 3/5/2016.
 */
public class ExploreFragment extends Fragment
{
    private final static String TAG = "ExploreFragment";
    private RecyclerView mFBSearchRecyclerView;
    private List<FBLike> mFBSearchItems = new ArrayList<>();
    private FBLikeAdapter mFBLikeAdapter;

    public static ExploreFragment newInstance()
    {
        Bundle args = new Bundle();
        ExploreFragment fragment = new ExploreFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ExploreFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // Register fragment to receive menu callbacks:
        setHasOptionsMenu(true);

        // Run last user search:
        updateSearchResults();

        // Set auto Search run:
        UserNotifyService.setServiceAlarm(getActivity(), true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_explore_search, container, false);
        mFBSearchRecyclerView = (RecyclerView) v.findViewById(R.id.fb_search_results_recycler);
        mFBSearchRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        updateUI();

        return v;
    }


    private void updateSearchResults()
    {
        // Get query stored:
        String query = QueryPreferences.getStoredQuery(getActivity());
        // Run the search:
        new SearchFBPagesTask(query).execute();
    }

    @Override
    // Add Search Bar:
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_explore_search, menu);

        //  Allow user to perform a custom search:

        // Get search box reference:
        MenuItem searchItem = menu.findItem(R.id.menu_page_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        // Listen for text submissions
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            // Executed when the user submits the search entry:
            public boolean onQueryTextSubmit(String searchQuery)
            {
                Log.d(TAG, "Query Text Submit: " + searchQuery);

                hideSoftKeyboard(getActivity());

                // Updated SharedPref to hold reference to query:
                QueryPreferences.setStoredQuery(getActivity(), searchQuery);

                // Run the search:
                updateSearchResults();

                updateUI();

                return true;
            }

            @Override
            // Runs every time a single character is edited in the search bar:
            public boolean onQueryTextChange(String newSearchText)
            {
                Log.d(TAG, "Query Text Change: " + newSearchText);
                return false;
            }
        });

        // OnClickListener for when the user clicks on spyglass to begin a search:
        searchView.setOnSearchClickListener(new View.OnClickListener()
        {
            @Override
            // Pre-populate the search bar with last saved query
            public void onClick(View v)
            {
                String lastSearch = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(lastSearch, false);
            }
        });
    }

    @Override
    // Used for menu actions
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // Using to clear last query saved in SharedPref
            case R.id.menu_search_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                // Update results to be null
                updateSearchResults();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    /*
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //You can change menuitem property
        //menu.findItem(R.id.notification).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }
    */


    public class SearchFBPagesTask extends AsyncTask<Void, Void, List<FBLike>>
    {
        private String mSearchQuery;
        private List<FBLike> mSearchResults = new ArrayList<>();

        // Create SearchFBPagesTask with Search String defined
        public SearchFBPagesTask(String searchQuery)
        {
            mSearchQuery = searchQuery;
        }

        @Override
        protected List<FBLike> doInBackground(Void... params)
        {
            // return new FBPageFetcher().search(query);

            try {
                GraphRequest request = GraphRequest.newGraphPathRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/search",
                        new GraphRequest.Callback()
                        {
                            @Override
                            public void onCompleted(GraphResponse response)
                            {
                                // Insert your code here:
                                try {
                                    JSONArray rawSearchResults = response.getJSONObject().getJSONArray("data");

                                    for (int i = 0; i <= rawSearchResults.length(); i++)
                                    {
                                        // Get FB Page Item
                                        JSONObject fbPageObject = rawSearchResults.getJSONObject(i);

                                        if (fbPageObject.getString("is_verified") == "true")
                                        {
                                            // Set FB Like Object settings:
                                            FBLike fbLikeItem = new FBLike();
                                            fbLikeItem.setId(fbPageObject.getString("id"));
                                            fbLikeItem.setName(fbPageObject.getString("name"));
                                            try {
                                                URL imageURL = new URL("https://graph.facebook.com/" + fbPageObject.getString("id") + "/picture?type=large");
                                                fbLikeItem.setPicURL(imageURL);
                                            } catch (MalformedURLException me) {
                                                me.printStackTrace();
                                            }

                                            mSearchResults.add(fbLikeItem);
                                        }
                                    }
                                }
                                catch (JSONException je)
                                {
                                    je.printStackTrace();
                                }

                                updateUI();
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("q", mSearchQuery);
                parameters.putString("type", "page");
                parameters.putString("fields", "is_verified,name,id");
                request.setParameters(parameters);
                request.executeAsync();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return mSearchResults;
        }

        @Override
        protected void onPostExecute(List<FBLike> fbSearchResults)
        {
            mFBSearchItems = fbSearchResults;
            updateUI();
        }
    }

    // Get likes stored in a DB:
    private void updateUI()
    {
        if (isAdded()) {
            mFBSearchRecyclerView.setAdapter(new FBLikeAdapter(mFBSearchItems));
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateUI();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

/*
 * To get the Facebook page which is liked by user's through creating a new request.
 * When the request is completed, a callback is called to handle the success condition.
*/

    // Create object to hold each FBLike entry to be displayed in RecyclerView
    private class FBLikeHolder extends RecyclerView.ViewHolder
    {
        // private TextView mCategoryTextView;
        private TextView mLikeName;
        private ImageView mLikePic;

        public FBLikeHolder(View fbLikeView)
        {
            super(fbLikeView);

            // mCategoryTextView = (TextView) fbLikeView.findViewById(R.id.fragment_fblike_category);
            //mLikeName = (TextView) fbLikeView.findViewById(R.id.fblike_name);
            mLikePic = (ImageView) fbLikeView.findViewById(R.id.fblike_image);
        }

        public void bindLikeItem(FBLike fbSearchItem)
        {
            //mLikeName.setText(fbSearchItem.getName().toString());
            new DownloadImage(mLikePic).execute(fbSearchItem.getPicURL().toString());
            // mCategoryTextView.setText(fbLikeItem.getCategory().toString());
        }
    }

    // Create adapter to handle FBLike refreshing
    private class FBLikeAdapter extends RecyclerView.Adapter<FBLikeHolder>
    {
        private List<FBLike> mFBLikes;

        public FBLikeAdapter(List<FBLike> fbLikes)
        {
            mFBLikes = fbLikes;
        }

        @Override
        public FBLikeHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View fbLikeView = inflater.inflate(R.layout.fblike_item, parent, false);
            return new FBLikeHolder(fbLikeView);
        }

        // Bind the FBLike Object to the Holder managed by this Adapter
        @Override
        public void onBindViewHolder(FBLikeHolder fbLikeHolder, int position)
        {
            FBLike fbLike = mFBLikes.get(position);
            fbLikeHolder.bindLikeItem(fbLike);
        }

        @Override
        public int getItemCount()
        {
            return mFBLikes.size();
        }
    }
}
