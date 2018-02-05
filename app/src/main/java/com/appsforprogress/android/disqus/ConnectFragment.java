package com.appsforprogress.android.disqus;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.appsforprogress.android.disqus.objects.User;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by oswald on 1/27/2018.
 */

// SupportMapFragment: Creates a MapView
public class ConnectFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "ConnectFragment";
    private GoogleApiClient mClient;
    private GoogleMap mMap;
    private Bitmap mMapImage;
    private User mUser;
    private Location mUserLocation;

    public static ConnectFragment newInstance()
    {
        return new ConnectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // setHasOptionsMenu(true);

        /*
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
       */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_connect, container, false);

        return v;
    }

    // Craete fragment first then try the map load once it completes:
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);
    }

    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_connect_search, menu);

        // Enable/Disable the connect button depending on whether the client is connected:
        MenuItem connectItem = menu.findItem(R.id.action_locate);
        connectItem.setEnabled(mClient.isConnected());
    }

    // Use Connect Button to initiate the location fix:
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_locate:
                // findConnection();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
    */


    private void getUserLocation()
    {
        // Get a single high accuracy location fix as soon as possible:
        LocationRequest lr = LocationRequest.create();
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // How Android should prioritize battery life vs loc accuracy
        lr.setNumUpdates(1); // How many times the location should be updated
        lr.setInterval(0); // How frequently the location should be updated

        // Send the request and listen for Location to come back:
        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, lr, new LocationListener()
        {
                    @Override
                    public void onLocationChanged(Location location)
                    {
                        Log.i(TAG, "Got a loc fix: " + location);
                        mUserLocation = location;
                        // new SearchTask().execute(location);
                    }
                });
    }

    // Perform map zoom:
    private void updateUI()
    {
        if (mMap == null)
        {
            return;
        }

        // LatLng itemPoint = new LatLng(mUser.getLat(), mUser.getLon());
        LatLng myLocation = new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude());

        // Get boundaries between user and other point:
        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(myLocation)
                                // can include other items location long/lat to define boundary
                                //.include()
                                .build();

        // Set margin for map zoom
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);

        // Allows us to move the Map around:
        // Creates an update that points the map camera at a specific rectangular LAtLong boundary
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mMap.animateCamera(update);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
