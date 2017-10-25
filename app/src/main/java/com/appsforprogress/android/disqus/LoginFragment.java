package com.appsforprogress.android.disqus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.appsforprogress.android.disqus.helpers.DownloadImage;
import com.appsforprogress.android.disqus.objects.FBLike;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginFragment extends Fragment
{
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    SharedPreferences pref;
    private boolean mLoggedIn = false;



    // For FaceBook Login:
    ViewSwitcher switcher;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private LoginButton mFBLoginButton;
    private List<FBLike> fbLikes = new ArrayList<FBLike>();
    private String facebook_id, first_name, last_name, name, gender, profile_image, full_name, email, email_id, profile_pic;
    private String profile_pic_url;

    private Button fbButton;

    // Facebook Login View:
    private TextView mUserName;
    private TextView mGender;
    private TextView mLocation;
    private ImageView mProfilePicture;
    private FloatingActionButton postFab;

    public static final String EXTRA_USER_ID = "com.appsforprogress.android.mycareerpath.user_id";
    public static final String EXTRA_FIRST_NAME = "com.appsforprogress.android.mycareerpath.first_name";
    public static final String EXTRA_LAST_NAME = "com.appsforprogress.android.mycareerpath.last_name";
    public static final String EXTRA_IMAGE_LINK = "com.appsforprogress.android.mycareerpath.profile_image";
    public static final String EXTRA_LOGIN_RESULT = "com.appsforprogress.android.mycareerpath.login_result";

    private AccessToken accessToken;
    private Profile profile;
    private RecyclerView mFBLikeRecyclerView;


    FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>()
    {

        @Override
        public void onSuccess(final LoginResult loginResult)
        {
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response)
                        {
                                try
                                {

                                    String uid = object.optString("id");
                                    String email = object.optString("email");
                                    String name = object.optString("name");
                                    JSONArray posts = object.getJSONObject("likes").optJSONArray("data");

                                    Uri.Builder builder = new Uri.Builder();
                                    builder.scheme("https")
                                            .authority("graph.facebook.com")
                                            .appendPath(uid)
                                            .appendPath("picture")
                                            .appendQueryParameter("width", "1000")
                                            .appendQueryParameter("height", "1000");

                                    Uri pictureUri = builder.build();

                                    // LOOP through retrieved JSON posts:
                                    for (int i = 0; i < posts.length(); i++)
                                    {
                                        JSONObject post = posts.optJSONObject(i);
                                        String likeId = post.optString("id");
                                        String likeCategory = post.optString("category");
                                        String likeName = post.optString("name");
                                        int likeCount = post.optInt("likes");

                                        // print id, page name and number of like of facebook page
                                        Log.e("id: ", likeId + " (name: " + likeName + " , category: "+ likeCategory + " likes count - " + likeCount);

                                        // Create Like Objects
                                        FBLike fbLike = new FBLike();
                                        fbLike.setId(likeId);
                                        fbLike.setCategory(likeCategory);
                                        fbLike.setName(likeName);

                                        // Add each like to a List
                                        fbLikes.add(fbLike);
                                    }

                                    //sendLogin(uid, name, email, pictureUri.toString(), "fb");
                                    mUserName.setText(name);
                                    mUserName.setVisibility(View.VISIBLE);

                                    new DownloadImage(mProfilePicture).execute(pictureUri.toString());
                                    mProfilePicture.setVisibility(View.VISIBLE);

                                    mFBLikeRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
                                    mFBLikeRecyclerView.setVisibility(View.VISIBLE);

                                    //shareDialog = new ShareDialog(getActivity());

                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id, name, email");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onError(FacebookException e) {
            Toast.makeText(getActivity(), "Something went wrong, please try again later", Toast.LENGTH_LONG).show();
        }
    };

    public void accessTokenCheck(AccessToken newToken)
    {
        if (newToken != null)
        {
            AccessToken.setCurrentAccessToken(newToken);
            accessToken = newToken;
            //refreshUI();
        }
        else if (newToken == null)
        {
            LoginManager.getInstance().logOut();
        }
    }


    public void profileCheck(Profile newProfile)
    {
        if (newProfile != null)
        {
            // this.stopTracking();
            Profile.setCurrentProfile(newProfile);
            profile = newProfile;
            // refreshUI();
        }
        else if (newProfile == null)
        {
            mUserName.setVisibility(View.GONE);
            mProfilePicture.setVisibility(View.GONE);
        }
    }

    public LoginFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();


        accessTokenTracker = new AccessTokenTracker()
        {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken)
            {
                // Determine if to logout:
                accessTokenCheck(newToken);
            }
        };

        profileTracker = new ProfileTracker()
        {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile)
            {
                // Determine if to remove UI?
                profileCheck(newProfile);
            }
        };

        profileTracker.startTracking();
        accessTokenTracker.startTracking();

        // prefUtil = new PrefUtil(this.getActivity());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_login, container, false);


        mUserName = (TextView) view.findViewById(R.id.fb_username);
        mUserName.setVisibility(View.INVISIBLE);
        mProfilePicture = (ImageView) view.findViewById(R.id.profileImage);
        mProfilePicture.setVisibility(View.INVISIBLE);
        mUserName.setVisibility(View.GONE);
        mProfilePicture.setVisibility(View.GONE);

        //mFBLikeRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_like_gallery_recycler_view);
        // Set up row of 3 elements
        //mFBLikeRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        //mFBLikeRecyclerView.setVisibility(View.INVISIBLE);


        /*
        // Initialize layout button
        fbButton = (Button) view.findViewById(R.id.custom_login_button);

        fbButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Call private method
                onFblogin();
            }
        });
        */
        mFBLoginButton = (LoginButton) view.findViewById(R.id.fb_login_button);
        mFBLoginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends", "user_likes"));

        callbackManager = CallbackManager.Factory.create();
        //mFBLoginButton.setFragment(this);
        mFBLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {

            @Override
            public void onSuccess(final LoginResult loginResult)
            {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback()
                        {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response)
                            {
                                try
                                {
                                    // Use main menu activity to return User Profile fragment
                                    Intent lgIntent = HomeActivity.logInIntent(getActivity(), object.toString());
                                    startActivity(lgIntent);

                                    /*
                                    String uid = object.optString("id");
                                    String email = object.optString("email");
                                    String name = object.optString("name");
                                    //JSONArray likes = object.getJSONArray("user_likes");

                                    Uri.Builder builder = new Uri.Builder();
                                    builder.scheme("https")
                                            .authority("graph.facebook.com")
                                            .appendPath(uid)
                                            .appendPath("picture")
                                            .appendQueryParameter("width", "1000")
                                            .appendQueryParameter("height", "1000");

                                    Uri pictureUri = builder.build();

                                    /* LOOP through retrieved JSON posts:
                                    for (int i = 0; i < likes.length(); i++)
                                    {
                                        JSONObject post = likes.optJSONObject(i);
                                        String likeId = post.optString("id");
                                        String likeCategory = post.optString("category");
                                        String likeName = post.optString("name");
                                        int likeCount = post.optInt("likes");

                                        // print id, page name and number of like of facebook page
                                        Log.e("id: ", likeId + " (name: " + likeName + " , category: "+ likeCategory + " likes count - " + likeCount);

                                        // Create Like Objects
                                        FBLike fbLike = new FBLike();
                                        fbLike.setId(likeId);
                                        fbLike.setCategory(likeCategory);
                                        fbLike.setName(likeName);

                                        // Add each like to a List
                                        fbLikes.add(fbLike);
                                    }
                                    */

                                    /*
                                    //sendLogin(uid, name, email, pictureUri.toString(), "fb");
                                    mUserName.setText(name);
                                    mUserName.setVisibility(View.VISIBLE);

                                    new DownloadImage(mProfilePicture).execute(pictureUri.toString());
                                    mProfilePicture.setVisibility(View.VISIBLE);

                                    mFBLikeRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
                                    mFBLikeRecyclerView.setVisibility(View.VISIBLE);

                                    //shareDialog = new ShareDialog(getActivity());
                                    */

                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, email, picture.width(120).height(120), likes");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(getActivity(), "Something went wrong, please try again later", Toast.LENGTH_LONG).show();
            }
        });

        mFBLikeRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_like_gallery_recycler_view);

        return view;
    }


    @Override
    public void onResume()
    {
        super.onResume();
        //Profile profile = Profile.getCurrentProfile();
        //profileCheck(profile);

        //  refreshUI();
        //deleteAccessToken();

    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        // Stop FB Login:
        //accessTokenTracker.stopTracking();
        //profileTracker.stopTracking();

        // refreshUI();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        LoginManager.getInstance().logOut();
        //accessTokenTracker.stopTracking();
        //profileTracker.stopTracking();
    }


    public void onActivityResult(int requestCode, int responseCode, Intent intent)
    {
        super.onActivityResult(requestCode, responseCode, intent);
        //Facebook login
        callbackManager.onActivityResult(requestCode, responseCode, intent);
    }

    private void refreshUI()
    {
        Profile profile = Profile.getCurrentProfile();

        if (accessToken != null && profile != null)
        {
            // Logged IN:
            String firstName = profile.getFirstName();
            String lastName = profile.getLastName();
            String imageUrl = profile.getProfilePictureUri(200, 200).toString();


            Toast.makeText(getActivity().getApplicationContext(), "Logging in...", Toast.LENGTH_SHORT).show();

            // MAKE OTHER ELEMENTS VISIBLE AND SET VALUES:
            mUserName.setText(firstName + " " + lastName);
            mUserName.setVisibility(View.VISIBLE);

            new DownloadImage(mProfilePicture).execute(imageUrl);
            mProfilePicture.setVisibility(View.VISIBLE);
        }
        else if (profile != null)
        {
            String firstName = profile.getFirstName();
            String lastName = profile.getLastName();
            String imageUrl = profile.getProfilePictureUri(200, 200).toString();


            Toast.makeText(getActivity().getApplicationContext(), "Logging in...", Toast.LENGTH_SHORT).show();

            // MAKE OTHER ELEMENTS VISIBLE AND SET VALUES:
            mUserName.setText(firstName + " " + lastName);
            mUserName.setVisibility(View.VISIBLE);

            new DownloadImage(mProfilePicture).execute(imageUrl);
            mProfilePicture.setVisibility(View.VISIBLE);
        }
        else
        {
            LoginManager.getInstance().logOut();
            mUserName.setVisibility(View.GONE);
            mProfilePicture.setVisibility(View.GONE);
        }
    }

    public static LoginFragment newInstance()
    {
        // return new UserProfileFragment();

        // UUID userId = (UUID) getIntent().getSerializableExtra(EXTRA_USER_ID);

        return new LoginFragment();
    }
}
