package com.appsforprogress.android.disqus;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by oswald on 1/27/2018.
 */

public class ConnectFragment extends SupportMapFragment
{
    private static final String TAG = "ConnectFragment";
    // private ImageView mImageView;
    private GoogleApiClient mClient;

    public static ConnectFragment newInstance()
    {
        return new ConnectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // To use Google Play Services a API Client must be created:
        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks()
                {
                    @Override
                    public void onConnected(Bundle bundle)
                    {
                        // Invalidate Toolbar when it is confirmed that we are connected:
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {}
                })
                .build();
    }

    /*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_connect, container, false);

        mImageView = (ImageView) v.findViewById(R.id.connection);

        return v;
    }
    */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_connect_search, menu);

        // Enable/Disable the connect button depending on whether the client is connected:
        MenuItem connectItem = menu.findItem(R.id.action_locate);
        connectItem.setEnabled(mClient.isConnected());
    }

    @Override
    public void onStart()
    {
        super.onStart();

        // Invalidate Menu
        getActivity().invalidateOptionsMenu();
        // Connect to Google Play Service Client
        mClient.connect();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        mClient.disconnect();
    }


    private void findConnection()
    {
        // Get a single high accuracy location fix as soon as possible:
        LocationRequest lr = LocationRequest.create();
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // How Android should prioritize battery life vs loc accuracy
        lr.setNumUpdates(1); // How many times the location should be updated
        lr.setInterval(0); // How frequently the location should be updated

        // Send the request and listen for Location to come back:
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mClient, lr, new LocationListener()
                {
                    @Override
                    public void onLocationChanged(Location location)
                    {
                        Log.i(TAG, "Got a loc fix: " + location);
                        // new SearchTask().execute(location);
                    }
                });
    }

    // Use Connect Button to initiate the location fix:
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_locate:
                findConnection();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    private class SearchTask extends AsyncTask<Location, Void, Void>
    {
        private GalleryItem mGalleryItem;


    }
    */
}
