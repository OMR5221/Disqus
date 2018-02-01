package com.appsforprogress.android.disqus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;

import com.appsforprogress.android.disqus.abstracts.SingleFragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by oswal on 1/27/2018.
 */

public class ConnectActivity extends SingleFragmentActivity
{
    private static final int REQUEST_ERROR = 0;

    @Override
    protected Fragment createFragment()
    {
        return ConnectFragment.newInstance();
    }

    @Override
    // Check if Location Service is Available:
    protected void onResume()
    {
        super.onResume();

        int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS)
        {
            Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode, REQUEST_ERROR,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // Leave if Google Play services are unavailable:
                            finish();
                        }
                    });

            errorDialog.show();
        }
    }
}
