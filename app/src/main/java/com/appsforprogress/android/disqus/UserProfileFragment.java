package com.appsforprogress.android.disqus;

import android.content.Context;
import android.content.Intent;
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
import com.appsforprogress.android.disqus.objects.User;
import com.appsforprogress.android.disqus.util.DBNodeConstants;
import com.appsforprogress.android.disqus.util.ItemTouchHelperAdapter;
import com.appsforprogress.android.disqus.util.OnStartDragListener;
import com.appsforprogress.android.disqus.util.SimpleItemTouchHelperCallback;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oswald on 3/5/2016.
 */
public class UserProfileFragment extends Fragment implements GetUserCallback.IGetUserResponse, OnStartDragListener
{
    // (Public) Accessed via an Intent by Login:
    public static final String EXTRA_USER_LOGOUT = "com.appsforprogress.android.disqus.logout";
    private static final Integer REQUEST_USER_LOGOUT = 1;

    private ShareDialog shareDialog;
    private User mUser;
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
    private DatabaseReference mDisqusDBRef;
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
        // mUserName.setText("" + mFirstName + " " + mLastName);
        mProfilePicture = (ImageView) view.findViewById(R.id.profileImage);

        // userProfileData = getActivity().getIntent().getStringExtra(HomeActivity.EXTRA_USER_PROFILE);
        userProfileData = QueryPreferences.getStoredProfile(getActivity());

        try
        {
            response = new JSONObject(userProfileData);

            Log.e("Response: ", response.toString());

            mUserEmail = (response.get("email").toString());
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

            // Reloads list to prevent dupes upon rotation:
            // How to call refresh from network?
            mFBLikeItems = new ArrayList<>();

            mDisqusDBRef = FirebaseDatabase
                    .getInstance()
                    .getReference(DBNodeConstants.FIREBASE_CHILD_FBLIKE);

            // LOOP through retrieved JSON posts:
            for (int i = 0; i <= 11; i++)
            {
                JSONObject like = likes.optJSONObject(i);

                String id = like.optString("id");
                String category = like.optString("category");
                String name = like.optString("name");

                int count = like.optInt("likes");
                // print id, page name and number of like of facebook page
                Log.e("id: ", id + " (name: " + name + " , category: "+ category + " likes count - " + count);

                FBLike fbLike = new FBLike();
                fbLike.setFBID(id);
                fbLike.setCategory(category);
                fbLike.setName(name);

                URL imageURL = new URL("https://graph.facebook.com/" + id + "/picture?type=large");
                // Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                fbLike.setPicURL(imageURL.toString());

                // Add each like to a List
                mFBLikeItems.add(fbLike);

                // Push FBLike to DB:
                mDisqusDBRef.push().setValue(fbLike);
                Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        mFBLikeRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_like_gallery_recycler_view);

        setUpFirebaseAdapter();

        updateUI();

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

    private void logOut(int resultCode)
    {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_USER_LOGOUT, 1);
        getActivity().setResult(resultCode, intent);
    }


    private void logout()
    {
        LoginManager.getInstance().logOut();
        Intent login = new Intent(getActivity(), LoginActivity.class);
        startActivity(login);
        getActivity().finish();
    }


    private void setUpFirebaseAdapter()
    {
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //String uid = user.getUid();

        /*
        mRestaurantReference = FirebaseDatabase
                .getInstance()
                .getReference(Constants.FIREBASE_CHILD_RESTAURANTS)
                .child(uid);
                */

        mFireBaseFBLikeAdapter = new FireBaseFBLikeAdapter(FBLike.class,
                R.layout.fragment_user_profile, FBLikeHolder.class,
                mDisqusDBRef, this, getContext());

        mFBLikeRecyclerView.setHasFixedSize(true);
        mFBLikeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFBLikeRecyclerView.setAdapter(mFireBaseFBLikeAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mFireBaseFBLikeAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mFBLikeRecyclerView);
    }


    // Get likes stored in a DB:
    private void updateUI()
    {
        // UserLikes ul = UserLikes.get(getActivity());
        // List<Like> likes = ul.getLikes();

        if (isAdded())
        {
            if (mFireBaseFBLikeAdapter == null)
            {
                setUpFirebaseAdapter();
                mFBLikeRecyclerView.setAdapter(mFireBaseFBLikeAdapter);
            }
            else
            {
                mFireBaseFBLikeAdapter.setFBLikes(mFBLikeItems);
                mFBLikeRecyclerView.setAdapter(mFireBaseFBLikeAdapter);
                mFireBaseFBLikeAdapter.notifyDataSetChanged();
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
        */
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

            DatabaseReference disqusDBRef = FirebaseDatabase.getInstance().getReference(DBNodeConstants.FIREBASE_CHILD_FBLIKE);

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
        private List<FBLike> mFBLikes;
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
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition)
        {
            return false;
        }

        @Override
        public void onItemDismiss(int position)
        {

        }
    }
}
