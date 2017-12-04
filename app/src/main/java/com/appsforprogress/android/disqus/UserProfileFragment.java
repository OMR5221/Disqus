package com.appsforprogress.android.disqus;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
public class UserProfileFragment extends Fragment
{
    public static final String EXTRA_USER_ID = "com.appsforprogress.android.disqus.user_id";
    public static final String EXTRA_FIRST_NAME = "com.appsforprogress.android.disqus.first_name";
    public static final String EXTRA_LAST_NAME = "com.appsforprogress.android.disqus.last_name";
    public static final String EXTRA_IMAGE_LINK = "com.appsforprogress.android.disqus.profile_image";
    public static final String EXTRA_LOGIN_RESULT = "com.appsforprogress.android.disqus.login_result";

    private ShareDialog shareDialog;
    private User mUser;
    private String mFirstName;
    private String mLastName;
    private String mProfilePicURL;

    // Facebook Login View:
    private TextView mUserName;
    private TextView mGender;
    private TextView mLocation;
    private ImageView mProfilePicture;
    private FloatingActionButton quizFab;
    private RecyclerView mFBLikeRecyclerView;
    private ProfileTracker profileTracker;
    private AccessTokenTracker accessTokenTracker;
    private LoginButton loginButton;
    private CallbackManager mCallbackManager;
    private List<FBLike> mFBLikeItems = new ArrayList<>();
    private JSONObject response, profile_pic_data, profile_pic_url;
    private String userProfileData;
    private FBLikeAdapter mFBLikeAdapter;


    public static UserProfileFragment newInstance()
    {
        Bundle args = new Bundle();

        // args.putSerializable(EXTRA_FIRST_NAME, firstName);
        // args.putSerializable(EXTRA_LAST_NAME, lastName);
        // args.putSerializable(EXTRA_IMAGE_LINK, profileImg);

        UserProfileFragment fragment = new UserProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public UserProfileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        userProfileData = getActivity().getIntent()
                .getStringExtra(HomeActivity.EXTRA_USER_PROFILE);

        /*
        mFirstName = (String) getActivity().getIntent()
                .getSerializableExtra(UserProfileActivity.EXTRA_FIRST_NAME);
        mLastName = (String) getActivity().getIntent()
                .getSerializableExtra(UserProfileActivity.EXTRA_LAST_NAME);
        mProfilePicURL = (String) getActivity().getIntent()
                .getSerializableExtra(UserProfileActivity.EXTRA_IMAGE_LINK);
         */
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        mUserName = (TextView) view.findViewById(R.id.fb_username);
        //mUserName.setText("" + mFirstName + " " + mLastName);
        mProfilePicture = (ImageView) view.findViewById(R.id.profileImage);

        try
        {
            response = new JSONObject(userProfileData);
            // user_email.setText(response.get("email").toString());
            mUserName.setText(response.get("name").toString());
            mUserName.setVisibility(View.VISIBLE);

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("graph.facebook.com")
                    .appendPath(response.get("id").toString())
                    .appendPath("picture")
                    .appendQueryParameter("width", "1000")
                    .appendQueryParameter("height", "1000");

            Uri pictureUri = builder.build();

            profile_pic_data = new JSONObject(response.get("picture").toString());
            profile_pic_url = new JSONObject(profile_pic_data.getString("data"));
            new DownloadImage(mProfilePicture).execute(pictureUri.toString());
            mProfilePicture.setVisibility(View.VISIBLE);

            // convert Json object into Json array
            JSONArray likes = response.getJSONObject("likes").optJSONArray("data");

            // LOOP through retrieved JSON posts:
            for (int i = 0; i <= 12; i++)
            {
                JSONObject like = likes.optJSONObject(i);

                String id = like.optString("id");
                //String category = post.optString("category");
                String name = like.optString("name");

                int count = like.optInt("likes");
                // print id, page name and number of like of facebook page
                // Log.e("id: ", id + " (name: " + name + " , category: "+ category + " likes count - " + count);

                FBLike fbLike = new FBLike();
                fbLike.setId(id);
                // fbLike.setCategory(category);
                fbLike.setName(name);

                URL imageURL = new URL("https://graph.facebook.com/" + id + "/picture?type=large");
                // Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                fbLike.setPicURL(imageURL);

                // Add each like to a List
                mFBLikeItems.add(fbLike);
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        quizFab = (FloatingActionButton) view.findViewById(R.id.quiz_fab);
        quizFab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // ShareLinkContent content = new ShareLinkContent.Builder().build();
                // shareDialog.show(content);
            }
        });
        quizFab.setVisibility(View.INVISIBLE);

        mFBLikeRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_like_gallery_recycler_view);
        // Set up row of 3 elements
        mFBLikeRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mFBLikeRecyclerView.setVisibility(View.VISIBLE);

        // shareDialog = new ShareDialog(getActivity());

        // setupAdapter();

        updateUI();

        return view;
    }

    private void setupAdapter()
    {
        if (isAdded())
        {
            mFBLikeRecyclerView.setAdapter(new FBLikeAdapter(mFBLikeItems));
        }
    }

    // Get likes stored in a DB:
    private void updateUI()
    {
        //UserLikes ul = UserLikes.get(getActivity());
        //List<Like> likes = ul.getLikes();

        if (mFBLikeAdapter == null)
        {
            mFBLikeAdapter = new FBLikeAdapter(mFBLikeItems);
            mFBLikeRecyclerView.setAdapter(mFBLikeAdapter);
        }
        else {
            if (!isAdded())
            {
                mFBLikeRecyclerView.setAdapter(new FBLikeAdapter(mFBLikeItems));
            }
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
        //private ImageView mLikePic;

        public FBLikeHolder(View fbLikeView)
        {
            super(fbLikeView);

            // mCategoryTextView = (TextView) fbLikeView.findViewById(R.id.fragment_fblike_category);
            mLikeName = (TextView) fbLikeView.findViewById(R.id.fblike_name);
            //mLikePic = (ImageView) fbLikeView.findViewById(R.id.fblike_image);
        }

        public void bindLikeItem(FBLike fbLikeItem)
        {
            mLikeName.setText(fbLikeItem.getName().toString());
            // new DownloadImage(mLikePic).execute(fbLikeItem.getPicURL().toString());
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
            View fbLikeView = inflater.inflate(R.layout.fblike_item_up, parent, false);
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
