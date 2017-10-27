package com.appsforprogress.android.disqus;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Oswald on 3/5/2016.
 */
public class ExploreFragment extends Fragment
{
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

        // Run user search:
        new SearchFBPagesTask().execute();


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_explore, container, false);
        mFBSearchRecyclerView = (RecyclerView) v.findViewById(R.id.fb_search_results_recycler);
        mFBSearchRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        setupAdapter();

        return v;
    }

    @Override
    // Add Search Bar:
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_explore_search, menu);
    }


    public class SearchFBPagesTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            String query = "Google";
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

                                    for (int i = 0; i <= 9; i++) {
                                        // Get FB Page Item
                                        JSONObject fbPageObject = rawSearchResults.getJSONObject(i);

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

                                        mFBSearchItems.add(fbLikeItem);
                                    }
                                }
                                catch (JSONException je)
                                {
                                    je.printStackTrace();
                                }

                                setupAdapter();
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("q", query);
                parameters.putString("type", "page");
                request.setParameters(parameters);
                request.executeAsync();

            } catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        /*
        @Override
        protected void onPostExecute(List<FBLike> fbSearchPages)
        {
            mFBSearchItems = fbSearchPages;
            setupAdapter();
        }
        */
    }

    private void setupAdapter()
    {
        if (isAdded())
        {
            mFBSearchRecyclerView.setAdapter(new FBLikeAdapter(mFBSearchItems));
        }
    }

    // Get likes stored in a DB:
    private void updateUI()
    {
        //UserLikes ul = UserLikes.get(getActivity());
        //List<Like> likes = ul.getLikes();

        if (mFBSearchRecyclerView == null)
        {
            mFBLikeAdapter = new FBLikeAdapter(mFBSearchItems);
            mFBSearchRecyclerView.setAdapter(mFBLikeAdapter);
        }
        else {
            if (!isAdded())
            {
                mFBSearchRecyclerView.setAdapter(new FBLikeAdapter(mFBSearchItems));
            }
        }

    }


    @Override
    public void onResume()
    {
        super.onResume();
        setupAdapter();
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
