package com.appsforprogress.android.disqus;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
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
import com.appsforprogress.android.disqus.helpers.GetUserCallback;
import com.appsforprogress.android.disqus.helpers.QueryPreferences;
import com.appsforprogress.android.disqus.objects.FBLike;
import com.appsforprogress.android.disqus.objects.FBLikes;
import com.appsforprogress.android.disqus.objects.User;
import com.appsforprogress.android.disqus.util.DBNodeConstants;
import com.appsforprogress.android.disqus.util.ItemTouchHelperAdapter;
import com.appsforprogress.android.disqus.util.OnStartDragListener;
import com.appsforprogress.android.disqus.util.SimpleItemTouchHelperCallback;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import static com.appsforprogress.android.disqus.LoginFragment.mUser;

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
    private List<FBLike> mFBLikeItems = new ArrayList<>();
    private JSONObject response, profile_pic_data, profile_pic_url;
    private String userProfileData;
    // private DatabaseReference mDisqusDBRef;
    private FireBaseFBLikeAdapter mFireBaseFBLikeAdapter;
    private ItemTouchHelper mItemTouchHelper;

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

        /*
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null)
        {
            if (accessToken.isExpired())
            {
                LoginManager.getInstance().logOut();
            }
            else
            {
                Profile profile = Profile.getCurrentProfile();

                if (profile == null)
                {
                    userProfileData = getActivity().getIntent()
                            .getStringExtra(HomeActivity.EXTRA_USER_PROFILE);
                }
            }
        }
        */
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
                .appendQueryParameter("width", "1000")
                .appendQueryParameter("height", "1000");

        Uri userPictureUri = builder.build();

        new DownloadImage(mProfilePicture).execute(userPictureUri.toString());
        mProfilePicture.setVisibility(View.VISIBLE);

        mFBLikeRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_like_gallery_recycler_view);

        setUpFireBaseAdapter();

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
        Intent login = new Intent(getActivity(), LoginActivity.class);
        startActivity(login);
        getActivity().finish();
    }

    private void refreshUserLikes()
    {
        try
        {
            GraphRequest request = GraphRequest.newGraphPathRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/" + mUser.getFBUserId().toString(),
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

                            mFBLikeItems = fbLikes;
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

        String uid = mUser.getFBUserId();

        Query query = FirebaseDatabase.getInstance()
                .getReference(DBNodeConstants.FIREBASE_CHILD_USER_LIKES)
                .child(uid)
                .orderByChild(DBNodeConstants.FIREBASE_CHILD_USER_LIKES_INDEX);


        mFBLikeRecyclerView.setHasFixedSize(true);
        mFBLikeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (isAdded())
        {
            mFireBaseFBLikeAdapter = new FireBaseFBLikeAdapter(FBLike.class,
                    R.layout.fragment_user_profile, FBLikeHolder.class,
                    query, this, getActivity());

            mFBLikeRecyclerView.setAdapter(mFireBaseFBLikeAdapter);
        }

        mFBLikeRecyclerView.setVisibility(View.VISIBLE);


        mFireBaseFBLikeAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
        {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount)
            {
                super.onItemRangeInserted(positionStart, itemCount);
                mFireBaseFBLikeAdapter.notifyDataSetChanged();
            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mFireBaseFBLikeAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mFBLikeRecyclerView);
    }


    public void saveFBLikeToFireBaseDB(String fbLike)
    {
        HomeActivity.mDisqusDBReference.setValue(fbLike);
    }


    /* Get likes stored in a DB:
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
                mFireBaseFBLikeAdapter.setFBLikes();
                mFBLikeRecyclerView.setAdapter(mFireBaseFBLikeAdapter);
                mFireBaseFBLikeAdapter.notifyDataSetChanged();
            }
        }

    }
    */

    @Override
    public void onResume()
    {
        super.onResume();

        // setUpFireBaseAdapter();
        // updateUI();
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
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mFireBaseFBLikeAdapter.cleanup();
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder)
    {
        mItemTouchHelper.startDrag(viewHolder);
    }


/*
 * To get the Facebook page which is liked by user's through creating a new request.
 * When the request is completed, a callback is called to handle the success condition.
*/

    // Create object to hold each FBLike entry to be displayed in RecyclerView
    private class FBLikeHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener
    {
        private static final int MAX_WIDTH = 200;
        private static final int MAX_HEIGHT = 200;

        private TextView mCategoryTextView;
        private TextView mLikeName;
        private ImageView mLikePic;
        Context mContext;

        public FBLikeHolder(View fbLikeView)
        {
            super(fbLikeView);
            mContext = fbLikeView.getContext();
            fbLikeView.setOnClickListener(this);

            mCategoryTextView = (TextView) fbLikeView.findViewById(R.id.user_fb_category_name);
            mLikeName = (TextView) fbLikeView.findViewById(R.id.user_fb_like_name);
            mLikePic = (ImageView) fbLikeView.findViewById(R.id.user_fb_like_image);
        }

        public void bindLikeItem(FBLike fbLikeItem)
        {
            mLikeName.setText(fbLikeItem.getName().toString());
            // new DownloadImage(mLikePic).execute(fbLikeItem.getPicURL().toString());
            mCategoryTextView.setText(fbLikeItem.getCategory().toString());

            Picasso.with(mContext)
                    .load(fbLikeItem.getPicURL())
                    .resize(MAX_WIDTH, MAX_HEIGHT)
                    .centerCrop()
                    .into(mLikePic);
        }

        @Override
        public void onClick(View v)
        {
            final ArrayList<FBLike> fbLikes = new ArrayList<>();

            DatabaseReference disqusDBRef = FirebaseDatabase.getInstance().getReference(DBNodeConstants.FIREBASE_CHILD_USER_LIKES);

            disqusDBRef.addListenerForSingleValueEvent(new ValueEventListener()
            {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        fbLikes.add(snapshot.getValue(FBLike.class));
                    }

                    int itemPosition = getLayoutPosition();

                    /*
                    Intent intent = new Intent(mContext, RestaurantDetailActivity.class);
                    intent.putExtra("position", itemPosition + "");
                    intent.putExtra("restaurants", Parcels.wrap(restaurants));
                    mContext.startActivity(intent);
                    */
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
    }

    // Create adapter to handle FBLike refreshing
    private class FireBaseFBLikeAdapter extends FirebaseRecyclerAdapter<FBLike, FBLikeHolder> implements ItemTouchHelperAdapter
    {
        private ChildEventListener mChildEventListener;
        private ArrayList<FBLike> mFBLikes = new ArrayList<>();
        private DatabaseReference mRef;
        private Context mContext;
        // Processes user touch:
        private OnStartDragListener mOnStartDragListener;


        public FireBaseFBLikeAdapter(Class<FBLike> modelClass, int modelLayout,
                                             Class<FBLikeHolder> viewHolderClass,
                                             Query ref, OnStartDragListener onStartDragListener, Context context)
        {
            super(modelClass, modelLayout, viewHolderClass, ref);
            mRef = ref.getRef();
            mOnStartDragListener = onStartDragListener;
            mContext = context;
            // mFBLikes = mFBLikeItems;

            mChildEventListener = mRef.addChildEventListener(new ChildEventListener()
            {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s)
                {
                    // Updates listing of likes
                    mFBLikes.add(dataSnapshot.getValue(FBLike.class));
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s)
                {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot)
                {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s)
                {

                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }


        /*
        public FBLikeAdapter(List<FBLike> fbLikes)
        {
            mFBLikes = fbLikes;
        }
        */

        @Override
        public FBLikeHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View fbLikeView = inflater.inflate(R.layout.fb_userlike_item_drag, parent, false);
            return new FBLikeHolder(fbLikeView);
        }

        /*
        // Bind the FBLike Object to the Holder managed by this Adapter
        @Override
        public void onBindViewHolder(FBLikeHolder fbLikeHolder, int index)
        {
            FBLike fbLike = mFBLikes.get(index);
            fbLikeHolder.bindLikeItem(fbLike);
        }

        // Used to refresh FBLikes displayed:
        public void setFBLikes(List<FBLike> fbLikes)
        {
            mFBLikes = fbLikes;
        }
        */


        @Override
        public int getItemCount()
        {
            return mFBLikes.size();
        }

        @Override
        protected void populateViewHolder(final FBLikeHolder fbLikeHolder, FBLike fbLike, int position)
        {
            fbLikeHolder.bindLikeItem(fbLike);

            // Set Touch Listener on ImageView to allow for sorting:
            fbLikeHolder.mLikePic.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN)
                    {
                        mOnStartDragListener.onStartDrag(fbLikeHolder);
                    }

                    return false;
                }
            });

            fbLikeHolder.itemView.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    int itemPosition = fbLikeHolder.getAdapterPosition();
                    /*
                    if (mOrientation == Configuration.ORIENTATION_LANDSCAPE)
                    {
                        createDetailFragment(itemPosition);
                    } else {
                        Intent intent = new Intent(mContext, RestaurantDetailActivity.class);
                        intent.putExtra(DBNodeConstants.EXTRA_KEY_POSITION, itemPosition);
                        intent.putExtra(DBNodeConstants.EXTRA_KEY_RESTAURANTS, Parcels.wrap(mRestaurants));
                        intent.putExtra(DBNodeConstants.KEY_SOURCE, Constants.SOURCE_SAVED);
                        mContext.startActivity(intent);
                    }
                    */
                }
            });
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition)
        {
            Collections.swap(mFBLikes, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            return false;
        }

        @Override
        // Allow user to remove item from their listing
        // Any way to batch unlike to FB?
        public void onItemDismiss(int index)
        {
            mFBLikes.remove(index);
            getRef(index).removeValue();
        }

        // Will re-assign the index in our ArrayList:
        private void setIndexInFireBase()
        {
            for (FBLike fbLike : mFBLikes)
            {
                int index = mFBLikes.indexOf(fbLike);
                DatabaseReference ref = getRef(index);
                ref.child("index").setValue(Integer.toString(index));
                /*
                fbLike.setIndex(Integer.toString(index));
                ref.setValue(fbLike);
                */
            }
        }


        @Override
        // We only officially update the index of FBLikes in the DB upon the closing of the app:
        public void cleanup()
        {
            super.cleanup();
            setIndexInFireBase();
            mRef.removeEventListener(mChildEventListener);
        }
    }
}
