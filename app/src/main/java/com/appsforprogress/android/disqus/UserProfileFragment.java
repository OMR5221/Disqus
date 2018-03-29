package com.appsforprogress.android.disqus;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appsforprogress.android.disqus.helpers.DownloadImage;
import com.appsforprogress.android.disqus.helpers.FireBaseFBLikeViewHolder;
import com.appsforprogress.android.disqus.helpers.GetUserCallback;
import com.appsforprogress.android.disqus.helpers.QueryPreferences;
import com.appsforprogress.android.disqus.objects.FBLike;
import com.appsforprogress.android.disqus.objects.FBLikes;
import com.appsforprogress.android.disqus.objects.User;
import com.appsforprogress.android.disqus.util.DBNodeConstants;
import com.appsforprogress.android.disqus.util.ItemTouchHelperAdapter;
import com.appsforprogress.android.disqus.util.ItemTouchHelperViewHolder;
import com.appsforprogress.android.disqus.util.OnStartDragListener;
import com.appsforprogress.android.disqus.util.SimpleItemTouchHelperCallback;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Oswald on 3/5/2016.
 */
public class UserProfileFragment extends Fragment implements OnStartDragListener
{
    // (Public) Accessed via an Intent by Login:
    public static final String EXTRA_USER_LOGOUT = "com.appsforprogress.android.disqus.logout";
    private static final Integer REQUEST_USER_LOGOUT = 1;

    private ShareDialog shareDialog;
    private String mFirstName;
    private String mLastName;
    private String mProfilePicURL;
    private String mUserEmail;

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
    // private List<FBLike> mFBLikeItems = new ArrayList<>();
    private JSONObject response, profile_pic_data, profile_pic_url;
    private String userProfileData;
    // private DatabaseReference mDisqusDBRef;
    private FirebaseRecyclerAdapter mFireBaseFBLikeAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private User mUser;
    private LinearLayoutManager mManager;
    private DatabaseReference mFirebaseDatabaseReference;

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

        // Register fragment to receive menu callbacks:
        setHasOptionsMenu(true);


        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null)
        {
            if (accessToken.isExpired())
            {
                LoginManager.getInstance().logOut();
                // Return to Login Fragment:
            }
            else
            {
                Profile profile = Profile.getCurrentProfile();

                if (profile == null)
                {
                    // Access USER DB:

                    DatabaseReference fbUserRef = FirebaseDatabase
                            .getInstance()
                            .getReference(DBNodeConstants.FIREBASE_CHILD_USER);

                    // Create User Instance:
                    fbUserRef.addValueEventListener(new ValueEventListener()
                    {
                        //attach listener
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            mUser = (dataSnapshot.getValue(User.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { //update UI here if error occurred.

                        }
                    });

                    DatabaseReference fbUserLikesRef = FirebaseDatabase
                            .getInstance()
                            .getReference(DBNodeConstants.FIREBASE_CHILD_USER_LIKES);
                }
                else {
                    mUser = new User();
                    mUser.setName(profile.getName());
                    mUser.setFBUserId(profile.getId());
                    mUser.setPictureURI(profile.getProfilePictureUri(1000, 1000));
                }
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        mUserName = (TextView) view.findViewById(R.id.fb_username);
        mUserName.setText("" + mUser.getName());
        mProfilePicture = (ImageView) view.findViewById(R.id.profileImage);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("graph.facebook.com")
                .appendPath(mUser.getFBUserId())
                .appendPath("picture")
                .appendQueryParameter("width", "400")
                .appendQueryParameter("height", "400");

        Uri userPictureUri = builder.build();

        new DownloadImage(mProfilePicture).execute(userPictureUri.toString());
        mProfilePicture.setVisibility(View.VISIBLE);

        mFBLikeRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_like_gallery_recycler_view);
        mFBLikeRecyclerView.setHasFixedSize(true);
        //code to do the HTTP request
        setUpFireBaseAdapter();


        /*
        profileTracker = new ProfileTracker()
        {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile)
            {
                // Determine if to remove UI?
                // updateUI();
            }
        };

        profileTracker.startTracking();

        /*
        // Set up row of 3 elements
        mFBLikeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFBLikeRecyclerView.setVisibility(View.VISIBLE);

        // shareDialog = new ShareDialog(getActivity());

        // setupAdapter();

        // new GetUserCallback(UserProfileFragment.this).getCallback();

        updateUI();


        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mFireBaseFBLikeAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mFBLikeRecyclerView);
        */

        return view;
    }

    @Override
    // Add Search Bar:
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_user_profile, menu);
    }

    @Override
    // Used for menu actions
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // Using to clear last query saved in SharedPref
            case R.id.nav_menu_user:
                logout();
                // logOut(Activity.RESULT_CANCELED); -- NON EXPLICIT Activity Call: NOT WORKING
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout()
    {
        LoginManager.getInstance().logOut();
        profileTracker.stopTracking();
        Intent login = new Intent(getActivity(), LoginActivity.class);
        startActivity(login);
        getActivity().finish();
    }

    /*
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mFBLikeRecyclerView.setLayoutManager(mManager);


        Profile profile = Profile.getCurrentProfile();

        String uid = profile.getId();

        Query query = FirebaseDatabase.getInstance()
                .getReference(DBNodeConstants.FIREBASE_CHILD_USER_LIKES)
                .child(uid)
                .orderByChild(DBNodeConstants.FIREBASE_CHILD_USER_LIKES_INDEX);

        mFireBaseFBLikeAdapter = new FireBaseFBLikeAdapter(
                FBLike.class, R.layout.fb_userlike_item_drag, FireBaseFBLikeViewHolder.class, query, this, getActivity())
                {

                    @Override
                    protected void populateViewHolder(final FireBaseFBLikeViewHolder viewHolder, final FBLike model, final int position)
                    {
                        final DatabaseReference userLikeRef = getRef(position);

                        final String fbLikeKey = userLikeRef.getKey();

                        viewHolder.bindFBLike(model);
                    }
                };

        mFBLikeRecyclerView.setAdapter(mFireBaseFBLikeAdapter);

    }
    */

    private void refreshUserLikes()
    {
        try
        {
            Profile profile = Profile.getCurrentProfile();

            GraphRequest request = GraphRequest.newGraphPathRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/" + profile.getId(),
                    new GraphRequest.Callback()
                    {
                        @Override
                        public void onCompleted(GraphResponse response)
                        {
                            List<FBLike> fbLikes = new ArrayList<>();
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
                                        fbLikeItem.setFBID(fbPageObject.getString("id"));
                                        fbLikeItem.setName(fbPageObject.getString("name"));
                                        try {
                                            URL imageURL = new URL("https://graph.facebook.com/" + fbPageObject.getString("id") + "/picture?type=large");
                                            fbLikeItem.setPicURL(imageURL.toString());
                                        } catch (MalformedURLException me) {
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

                            // mFBLikeItems = fbLikes;
                            // updateUI();
                            setUpFireBaseAdapter();
                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "likes{category,name,id,category_list}");
            request.setParameters(parameters);
            request.executeAsync();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void setUpFireBaseAdapter()
    {
        // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Profile profile = Profile.getCurrentProfile();

        String uid = profile.getId();

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        SnapshotParser<FBLike> parser = new SnapshotParser<FBLike>()
        {
            @Override
            public FBLike parseSnapshot(DataSnapshot dataSnapshot)
            {
                FBLike fbLike = dataSnapshot.getValue(FBLike.class);

                if (fbLike != null)
                {
                    fbLike.setFBID(dataSnapshot.getKey());
                }
                return fbLike;
            }
        };

        DatabaseReference fbLikesRef = mFirebaseDatabaseReference.child(uid);

        Query query = FirebaseDatabase.getInstance()
                .getReference(DBNodeConstants.FIREBASE_CHILD_USER_LIKES)
                .child(uid)
                .orderByChild(DBNodeConstants.FIREBASE_CHILD_USER_LIKES_INDEX);

        FirebaseRecyclerOptions<FBLike> options =
                new FirebaseRecyclerOptions.Builder<FBLike>()
                        .setQuery(query, FBLike.class)
                        .build();

        mFireBaseFBLikeAdapter = new FirebaseRecyclerAdapter<FBLike, FireBaseFBLikeViewHolder> (options)
        {
            @Override
            public FireBaseFBLikeViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
            {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fb_userlike_item_drag, parent, false);

                return new FireBaseFBLikeViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(FireBaseFBLikeViewHolder holder, int position, FBLike model)
            {
                holder.bindFBLike(model);
            }

        };

        /*
        new FireBaseFBLikeAdapter(FBLike.class,
        R.layout.fb_userlike_item_drag, FireBaseFBLikeViewHolder.class,
        query, this, getActivity());
        */

        // mManager.setReverseLayout(false);

        // mFBLikeRecyclerView.setHasFixedSize(true);
        //

        mFireBaseFBLikeAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
        {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount)
            {
                super.onItemRangeInserted(positionStart, itemCount);
                // mManager.smoothScrollToPosition(mFBLikeRecyclerView, null, mFireBaseFBLikeAdapter.getItemCount());
                mFireBaseFBLikeAdapter.notifyDataSetChanged();
            }

            /*
            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount)
            {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                mFireBaseFBLikeAdapter.notifyDataSetChanged();
            }
            */

        });


        mManager = new LinearLayoutManager(getActivity());
        mFBLikeRecyclerView.setLayoutManager(mManager);
        mFireBaseFBLikeAdapter.startListening();
        mFBLikeRecyclerView.setAdapter(mFireBaseFBLikeAdapter);
        mFBLikeRecyclerView.setVisibility(View.VISIBLE);

        /*
         //Initialize and request AdMob ad.
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
                  */

        /*
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mFireBaseFBLikeAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mFBLikeRecyclerView);
        */
    }


    public void saveFBLikeToFireBaseDB(String fbLike)
    {
        HomeActivity.mDisqusDBReference.setValue(fbLike);
    }


    // Get likes stored in a DB:
    private void updateUI()
    {
        String uid = mUser.getFBUserId();

        Query query = FirebaseDatabase
                .getInstance()
                .getReference(DBNodeConstants.FIREBASE_CHILD_USER_LIKES)
                .child(uid)
                .orderByChild(DBNodeConstants.FIREBASE_CHILD_USER_LIKES_INDEX);
        // List<Like> likes = ul.getLikes();

        if (isAdded())
        {
            if (mFireBaseFBLikeAdapter == null)
            {
                setUpFireBaseAdapter();
                mFBLikeRecyclerView.setAdapter(mFireBaseFBLikeAdapter);
            }
            else
            {
                // mFireBaseFBLikeAdapter.setFBLikes();
                mFBLikeRecyclerView.setAdapter(mFireBaseFBLikeAdapter);
                mFireBaseFBLikeAdapter.notifyDataSetChanged();
            }
        }

    }


    @Override
    public void onResume()
    {
        super.onResume();
        //setUpFireBaseAdapter();
    }

    /*
    @Override
    public void onCompleted(User user)
    {
        new DownloadImage(mProfilePicture).execute(user.getPicture().toString());
        mProfilePicture.setVisibility(View.VISIBLE);
        mProfilePicture.setImageURI(user.getPicture());

        mUserName.setText(user.getName());
        mUserName.setVisibility(View.VISIBLE);
        mFBLikeItems = user.getFBLikes();

        // mId.setText(user.getId());

        /*
        if (user.getEmail() == null) {
            mUserEmail.setText(R.string.no_email_perm);
            mUserEmail.setTextColor(Color.RED);
        } else {
            mEmail.setText(user.getEmail());
            mEmail.setTextColor(Color.BLACK);
        }
        mPermissions.setText(user.getPermissions());
    }
    */

    @Override
    public void onPause()
    {
        super.onPause();
        // mFireBaseFBLikeAdapter.cleanup();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // mFireBaseFBLikeAdapter.cleanup();
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder)
    {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
