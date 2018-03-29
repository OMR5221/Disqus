package com.appsforprogress.android.disqus;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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
import com.appsforprogress.android.disqus.helpers.FBPageFetcher;
import com.appsforprogress.android.disqus.helpers.QueryPreferences;
import com.appsforprogress.android.disqus.objects.FBLike;
import com.appsforprogress.android.disqus.objects.FBLikes;
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
public class ExploreFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<FBLike>>
{
    private final static String TAG = "ConnectFragment";
    private final static Integer SEARCH_OPERATION_ID = 0;
    private String SEARCH_QUERY_STRING;
    private RecyclerView mFBSearchRecyclerView;
    private FBLikeAdapter mFBLikeAdapter;
    private List<FBLike> mFBSearchItems;
    private SearchFBPagesTask mFBSearchTask;

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
        //setRetainInstance(true);

        // Register fragment to receive menu callbacks:
        setHasOptionsMenu(true);

        // Run last user search:
        //updateSearchResults();

        // mFBSearchItems = FBLikes.getInstance(getActivity()).getAllFBLikes();

        // Set auto Search run: BROKEN (NULL SEARCH)
        // UserNotifyService.setServiceAlarm(getActivity(), true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_explore_search, container, false);
        mFBSearchRecyclerView = (RecyclerView) v.findViewById(R.id.fb_search_results_recycler);
        mFBSearchRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        // updateSearchResults();

        return v;
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
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        /* setEmptyText("No applications");

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new AppListAdapter(getActivity());
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);
        */

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        //getLoaderManager().initLoader(SEARCH_OPERATION_ID, null, this);
        updateSearchResults();

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

    public static void hideSoftKeyboard(Activity activity)
    {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void updateSearchResults()
    {
        String query = QueryPreferences.getStoredQuery(getActivity());

        try
        {
            GraphRequest request = GraphRequest.newGraphPathRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/search",
                    new GraphRequest.Callback()
                    {
                        @Override
                        public void onCompleted(GraphResponse response)
                        {
                            ArrayList<FBLike> fbLikes = new ArrayList<>();
                            // Insert your code here:
                            try
                            {
                                JSONArray rawSearchResults = response.getJSONObject().getJSONArray("data");

                                for (int i = 0; i <= rawSearchResults.length(); i++)
                                {
                                    // Get FB Page Item
                                    JSONObject fbPageObject = rawSearchResults.getJSONObject(i);

                                    if (fbPageObject.getString("is_verified") == "true")
                                    {
                                        // Set FB Like Object settings:
                                        FBLike fbLikeItem = new FBLike();
                                        fbLikeItem.setFBID(fbPageObject.getString("id"));
                                        fbLikeItem.setName(fbPageObject.getString("name"));

                                        try
                                        {
                                            URL imageURL = new URL("https://graph.facebook.com/" + fbPageObject.getString("id") + "/picture?type=large");
                                            fbLikeItem.setPicURL(imageURL.toString());
                                        }
                                        catch (MalformedURLException me)
                                        {
                                            me.printStackTrace();
                                        }

                                        fbLikes.add(fbLikeItem);
                                    }
                                }
                            }
                            catch (JSONException je)
                            {
                                je.printStackTrace();
                            }
                            mFBSearchItems = fbLikes;
                            updateUI();
                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("q", query);
            parameters.putString("type", "page");
            parameters.putString("fields", "is_verified,name,id");
            request.setParameters(parameters);
            request.executeAsync();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void updateSearchResultsLoader()
    {
        // Get query stored:
        String query = QueryPreferences.getStoredQuery(getActivity());

        // Create a bundle called queryBundle
        Bundle queryBundle = new Bundle();

        // Use putString with OPERATION_QUERY_URL_EXTRA as the key and the String value of the URL as the value
        //url value here is https://jsonplaceholder.typicode.com/posts
        queryBundle.putString(SEARCH_QUERY_STRING, query);

        // Call to check if a loader already exists:
        LoaderManager loaderManager = getLoaderManager();

        // Get our loader and check if it is the id we are are searching for:
        Loader<String> loader = loaderManager.getLoader(SEARCH_OPERATION_ID);

        if (loader == null)
        {
            loaderManager.initLoader(SEARCH_OPERATION_ID, queryBundle, this);
        } else {
            loaderManager.restartLoader(SEARCH_OPERATION_ID, queryBundle, this);
        }

        // mFBSearchTask = new SearchFBPagesTask(getContext(), query);
        // mFBSearchTask.loadInBackground();
        // mFBSearchTask.onLoadFinished(); -- How to call?
    }



    @Override
    // Here is where you construct the actual Loader instance
    public Loader<List<FBLike>> onCreateLoader(int id, Bundle args)
    {
        // Get Search query:
        // String query = QueryPreferences.getStoredQuery(getActivity());
        String query = args.getString(SEARCH_QUERY_STRING);

        return new SearchFBPagesTask(getActivity(), query);
    }

    @Override
    // This is where the results you deliver appear.
    // Display our data, for instance updating our adapter
    // In my case: FBLikeAdapter
    public void onLoadFinished(Loader<List<FBLike>> loader, List<FBLike> data)
    {
        mFBSearchItems = data;
        // setupAdapter();
        updateUI();
        // mFBLikeAdapter.setFBLikes(data);
    }


    @Override
    // Chance to clean up any references to the now reset Loader data
    // Loader reset, throw away our data,
    // unregister any listeners, etc..
    public void onLoaderReset(Loader<List<FBLike>> loader)
    {
        mFBLikeAdapter.setFBLikes(null);
        // mFBLikeAdapter.setFBLikes(new ArrayList<FBLike>());
    }

    /*
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //You can change menuitem property
        //menu.findItem(R.id.notification).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }
    */




    public static class SearchFBPagesTask extends AsyncTaskLoader<List<FBLike>>
    {
        private String mSearchQuery;
        private List<FBLike> mSearchResults;
        // final PackageManager mPm;

        // Create SearchFBPagesTask with Search String defined:
        public SearchFBPagesTask(Context context, String searchQuery)
        {
            super(context);
            mSearchQuery = searchQuery;
            //mPm = getContext().getPackageManager();
            //forceLoad();
        }

        @Override
        public void onStartLoading()
        {
            if (mSearchResults != null)
            {
                deliverResult(mSearchResults);
            }
            else {
                forceLoad();
            }
            // Notify the loader to reload the data
            // onContentChanged();
            // If the loader is started, this will kick off
            // loadInBackground() immediately. Otherwise,
            // the fact that something changed will be cached
            // and can be later retrieved via takeContentChanged()
        }

        @Override
        public List<FBLike> loadInBackground()
        {
            // Call FB API Search:
            return new FBPageFetcher().prepareSearch(mSearchQuery);
        }

        @Override
        public void deliverResult(List<FBLike> data)
        {
            mSearchResults = data;
            super.deliverResult(data);
        }
    }


    public void saveLocationToFirebase(String fblikeSearchQuery)
    {
        // HomeActivity.mDisqusDBReference.setValue(fblikeSearchQuery);
    }

    // Get likes stored in a DB:
    private void updateUI()
    {

        /*
        // Get FBLikes instance from DB code:
        FBLikes fbLikeResults = FBLikes.getInstance(getActivity());
        List<FBLike> fbLikes = fbLikeResults.getAllFBLikes();

        //  Old Working call that does not refresh correctly:
        if (isAdded()) {
            mFBSearchRecyclerView.setAdapter(new FBLikeAdapter(fbLikes));
        }
        */

        if (isAdded())
        {
            // Get FBLikes from DB:
            //FBLikes fbLikeResults = FBLikes.getInstance(getActivity());
            ///List<FBLike> fbLikes = fbLikeResults.getAllFBLikes();

            // If adapter does not exist then recreate and set:
            if (mFBLikeAdapter == null)
            {
                mFBLikeAdapter = new FBLikeAdapter(mFBSearchItems);
                mFBSearchRecyclerView.setAdapter(mFBLikeAdapter);
            }
            // else if exists then refresh with pulled FBLikes:
            else {
                mFBLikeAdapter.setFBLikes(mFBSearchItems);
                mFBSearchRecyclerView.setAdapter(mFBLikeAdapter);
                mFBLikeAdapter.notifyDataSetChanged();
            }
        }
    }


    // Get likes stored in a DB:
    private void setupAdapter()
    {
        //  Old Working call that does not refresh correctly:
        if (isAdded())
        {
            mFBSearchRecyclerView.setAdapter(new FBLikeAdapter(mFBSearchItems));
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //setupAdapter();
        // Reload search results to reflect user like/unlike updates:
        // updateSearchResults();
        // updateUI();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        /*
        // Update the FBLike data in the DB:
        FBLikes.getInstance(getActivity())
                .updateFBLikes(mFBSearchItems);
        */
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
        // private LikeView mLikePage;

        public FBLikeHolder(View fbLikeView)
        {
            super(fbLikeView);

            // mCategoryTextView = (TextView) fbLikeView.findViewById(R.id.fragment_fblike_category);
            // mLikeName = (TextView) fbLikeView.findViewById(R.id.fblike_name);
            mLikePic = (ImageView) fbLikeView.findViewById(R.id.fblike_image);
            //mLikePage = (LikeView) fbLikeView.findViewById(R.id.fblike_page);
            //mLikePage.setLikeViewStyle(LikeView.Style.BUTTON);
            //mLikePage.setAuxiliaryViewPosition(LikeView.AuxiliaryViewPosition.INLINE);
            //mLikePage.setHorizontalAlignment(LikeView.HorizontalAlignment.CENTER);
        }

        public void bindLikeItem(FBLike fbSearchItem)
        {
            //mLikeName.setText(fbSearchItem.getName().toString());
            new DownloadImage(mLikePic).execute(fbSearchItem.getPicURL().toString());
            // mCategoryTextView.setText(fbLikeItem.getCategory().toString());
            /*
            mLikePage.setObjectIdAndType(fbSearchItem.getPicURL().toString(),
                    LikeView.ObjectType.OPEN_GRAPH);
                    */
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
            View fbLikeView = inflater.inflate(R.layout.fblike_item_explore, parent, false);
            return new FBLikeHolder(fbLikeView);
        }

        // Bind the FBLike Object to the Holder managed by this Adapter
        @Override
        public void onBindViewHolder(FBLikeHolder fbLikeHolder, int position)
        {
            FBLike fbLike = mFBLikes.get(position);
            fbLikeHolder.bindLikeItem(fbLike);
        }

        // Used to refresh FBLikes displayed:
        public void setFBLikes(List<FBLike> fbLikes)
        {
            mFBLikes = fbLikes;
        }

        @Override
        public int getItemCount()
        {
            return mFBLikes.size();
        }
    }
}