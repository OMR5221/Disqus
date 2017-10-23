package com.appsforprogress.android.disqus;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appsforprogress.android.disqus.helpers.DownloadImage;
import com.appsforprogress.android.disqus.objects.FBLike;
import com.appsforprogress.android.disqus.objects.User;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oswald on 3/5/2016.
 */
public class ExploreFragment extends Fragment
{
    private RecyclerView mFBSearchRecyclerView;
    private List<FBLike> mFBSearchItems = new ArrayList<>();

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

        // Run user search:
        new SearchFBPagesTask().execute();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_explore, container, false);
        mFBSearchRecyclerView = (RecyclerView) v.findViewById(R.id.fb_search_results_recycler);
        mFBSearchRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        return v;
    }

    @Override
    // Add Search Bar:
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_explore_search, menu);
    }


    public class SearchFBPagesTask extends AsyncTask<Void, Void, List<FBLike>>
    {
        @Override
        protected List<FBLike> doInBackground(Void... params)
        {
            String query = "Google";
            return new FBPageFetcher().searchFBPages(query);
        }

        @Override
        protected void onPostExecute(List<FBLike> fbSearchPages)
        {
            mFBSearchItems = fbSearchPages;
            setupAdapter();
        }
    }

    private void setupAdapter()
    {
        if (isAdded())
        {
            mFBSearchRecyclerView.setAdapter(new FBLikeAdapter(mFBSearchItems));
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();
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
            mLikeName = (TextView) fbLikeView.findViewById(R.id.fblike_name);
            mLikePic = (ImageView) fbLikeView.findViewById(R.id.fblike_image);
        }

        public void bindLikeItem(FBLike fbSearchItem)
        {
            mLikeName.setText(fbSearchItem.getName().toString());
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
