package com.appsforprogress.android.disqus;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.appsforprogress.android.disqus.helpers.FBAccessTokenPreferences;
import com.appsforprogress.android.disqus.objects.FBLike;
import com.appsforprogress.android.disqus.objects.User;
import com.appsforprogress.android.disqus.util.DBNodeConstants;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;



/**
 * A login screen that offers login via email/password.
 */
public class LoginFragment extends Fragment
{
    // For FaceBook Login:
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private LoginButton mFBLoginButton;
    private final Integer REQUEST_USER_LOGOUT = 1;
    private AccessToken mAccessToken;
    private Profile mProfile;
    private ArrayList<String> mPermissions;
    public static User mUser;

    FacebookCallback<LoginResult> loginResultFacebookCallback = new FacebookCallback<LoginResult>()
    {

        @Override
        public void onSuccess(final LoginResult loginResult)
        {
            // Save Access Taken in SharedPreference:
            FBAccessTokenPreferences.setStoredAccessToken(getActivity(), loginResult.getAccessToken().toString());

            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback()
                    {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response)
                        {
                            try
                            {
                                String userFBID = object.getString("id").toString();

                                String userName = object.get("name").toString();
                                String userEmail = object.get("email").toString();

                                // Save User to DB:
                                // Saving to User
                                /*
                                DatabaseReference userRef = FirebaseDatabase
                                        .getInstance()
                                        .getReference(DBNodeConstants.FIREBASE_CHILD_USER);
                                */


                                // convert Json object into Json array
                                JSONArray likes = object.getJSONObject("likes").optJSONArray("data");

                                DatabaseReference fbUserRef = FirebaseDatabase
                                    .getInstance()
                                    .getReference(DBNodeConstants.FIREBASE_CHILD_USER)
                                    .child(userFBID);

                                mUser = new User();
                                mUser.setFBUserId(userFBID);
                                mUser.setName(userName);
                                mUser.setEmail(userEmail);

                                // Push USER Info to DB:
                                fbUserRef.push().setValue(mUser);
                                Toast.makeText(getContext(), "Saved User Info to DB", Toast.LENGTH_SHORT).show();

                                DatabaseReference userLikeRef = FirebaseDatabase
                                        .getInstance()
                                        .getReference(DBNodeConstants.FIREBASE_CHILD_USER_LIKES)
                                        .child(userFBID);

                                // Reloads list to prevent dupes upon rotation:
                                // How to call refresh from network?
                                ArrayList<FBLike> fbLikeItems = new ArrayList<>();

                                // LOOP through retrieved user likes:
                                for (int i = 0; i <= 11; i++)
                                {
                                    JSONObject like = likes.optJSONObject(i);

                                    String fbLikeID = like.optString("id");
                                    String likeCategory = like.optString("category");
                                    String likeName = like.optString("name");

                                    int count = like.optInt("likes");
                                    // print id, page name and number of like of facebook page
                                    Log.e("id: ", fbLikeID + " (name: " + likeName + " , category: "+ likeCategory + " likes count - " + count);

                                    FBLike fbLike = new FBLike();
                                    fbLike.setFBID(fbLikeID);
                                    fbLike.setCategory(likeCategory);
                                    fbLike.setName(likeName);

                                    URL imageURL = new URL("https://graph.facebook.com/" + fbLikeID + "/picture?type=large");
                                    // Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                                    fbLike.setPicURL(imageURL.toString());

                                    // Add each like to a List
                                    fbLikeItems.add(fbLike);

                                    // Push FBLike to DB:
                                    userLikeRef.push().setValue(fbLike);
                                    Toast.makeText(getContext(), "Saved User Like: " + fbLike.getName(), Toast.LENGTH_SHORT).show();
                                }

                                // Too much data to push:
                                // mUser = new User(userPictureUri, userName, userFBID, userEmail, mPermissions.toString(), fbLikeItems);

                                // Successful Login: Start HomeActivity with User Profile selected
                                Intent lgIntent = HomeActivity.logInIntent(getActivity(), object.toString());
                                startActivityForResult(lgIntent, REQUEST_USER_LOGOUT);
                                getActivity().finish();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id, name, email, picture.width(120).height(120), likes{category,name,id,category_list}");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel()
        {
            // LoginManager.getInstance().logOut();
        }

        @Override
        public void onError(FacebookException e)
        {
            Toast.makeText(getActivity(), "Something went wrong, please try again later", Toast.LENGTH_LONG).show();
        }
    };

    // Need to generate DB instance and load data:
    private void userRegistration()
    {



        userLogin();
    }

    // Need to check for changes in FB and update DB if needed:
    private void userLogin()
    {

    }


    public static LoginFragment newInstance()
    {
        return new LoginFragment();
    }


    public void accessTokenCheck(AccessToken newToken)
    {
        if (newToken != null)
        {
            AccessToken.setCurrentAccessToken(newToken);
            mAccessToken = newToken;
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
            mProfile = newProfile;
        }
    }

    public LoginFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        /*
        // callbackManager = CallbackManager.Factory.create();

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
        */
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mPermissions = new ArrayList<>(Arrays.asList("public_profile", "email", "user_friends", "user_likes"));

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null)
        {
            if (accessToken.isExpired())
            {
                reLogin(view);
            }
            else
            {
                /// This is code responsible for re-login
                LoginManager.getInstance().logInWithReadPermissions(this, mPermissions);

                Intent homeIntent = new Intent(getActivity(), HomeActivity.class);
                startActivity(homeIntent);
                getActivity().finish();
            }
        }
        else
        {
            reLogin(view);
        }

        return view;
    }


    private void reLogin(View view)
    {
        LoginManager.getInstance().logOut();

        mFBLoginButton = (LoginButton) view.findViewById(R.id.fb_login_button);

        mFBLoginButton.setReadPermissions(mPermissions);

        callbackManager = CallbackManager.Factory.create();
        // mFBLoginButton.setFragment(this);
        mFBLoginButton.registerCallback(callbackManager, loginResultFacebookCallback);

        LoginManager.getInstance().registerCallback(callbackManager, loginResultFacebookCallback);
    }


    @Override
    public void onResume()
    {
        super.onResume();

        // Profile profile = Profile.getCurrentProfile();
        // profileCheck(profile);
        // deleteAccessToken();

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
        // accessTokenTracker.stopTracking();
        // profileTracker.stopTracking();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        //profileTracker.stopTracking();

        // LoginManager.getInstance().logOut();
        // accessTokenTracker.stopTracking();
        // profileTracker.stopTracking();
    }


    // Run once activity has been completed:
    public void onActivityResult(int requestCode, int responseCode, Intent intent)
    {
        if (responseCode != Activity.RESULT_OK)
        {
            return;
        }

        if (requestCode == REQUEST_USER_LOGOUT)
        {
            LoginManager.getInstance().logOut();
        }
        else
        {
            super.onActivityResult(requestCode, responseCode, intent);

            // Facebook login:
            callbackManager.onActivityResult(requestCode, responseCode, intent);
        }
    }
}
