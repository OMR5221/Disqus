package com.appsforprogress.android.disqus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.appsforprogress.android.disqus.helpers.FBAccessTokenPreferences;
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
import org.json.JSONObject;
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
            parameters.putString("fields", "id, name, email, picture.width(120).height(120), likes");
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

        // If MainActivity is reached without the user being logged in, redirect to the Login
        // Activity
        if (AccessToken.getCurrentAccessToken() != null)
        {
            Intent homeIntent = new Intent(getActivity(), HomeActivity.class);
            startActivity(homeIntent);
            getActivity().finish();
        }

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
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mFBLoginButton = (LoginButton) view.findViewById(R.id.fb_login_button);

        mPermissions = new ArrayList<>(Arrays.asList("public_profile", "email", "user_friends", "user_likes"));

        mFBLoginButton.setReadPermissions(mPermissions);

        callbackManager = CallbackManager.Factory.create();
        // mFBLoginButton.setFragment(this);
        mFBLoginButton.registerCallback(callbackManager, loginResultFacebookCallback);

        LoginManager.getInstance().registerCallback(callbackManager, loginResultFacebookCallback);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null)
        {
            if (accessToken.isExpired())
            {
                LoginManager.getInstance().logOut();
            }
            else
            {
                /// This is code responsible for re-login
                LoginManager.getInstance().logInWithReadPermissions(this, mPermissions);
            }
        }

        return view;
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
