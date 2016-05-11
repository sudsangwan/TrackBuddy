package com.santellia.sud.trackbuddy.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.santellia.sud.trackbuddy.R;
import com.santellia.sud.trackbuddy.helper.AppHelper;

public class SplashActivity extends ActionBarActivity {

    Context mContext;
    boolean connected;
    boolean updated;
    boolean loggedIn;
    boolean locationOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContext = SplashActivity.this;

        TextView textView = (TextView) findViewById(R.id.tvSplashText);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/georgia_regular_new.ttf");
        textView.setTypeface(tf);
    }

    @Override
    protected void onStart() {
        super.onStart();

        performFunctions();
    }

    private void performFunctions() {
        connected = false;
        updated = false;

        ensureLocationEnabled();
//        ensureConnection();
//
//        if (connected) {
//            verifyLogin();
//        }
    }

    private void verifyLogin() {
        loggedIn = checkIfLoggedIn();
        if (loggedIn == true) {

            startApp();
        } else {

            redirectToLogin();
        }
    }

    private boolean checkIfLoggedIn() {
        //If application is not killed and user is logged in &  data available in shared preference
        String sessionId = AppHelper.getSystemValue("session", mContext);
        if (sessionId != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public void ensureLocationEnabled() {
        // Get Location Manager and check for GPS & Network location services
        LocationManager lm = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
            alertDialogBuilder.setTitle("Location Services Not Enabled");
            alertDialogBuilder.setMessage("Please enable Location Services and GPS");

            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();
                            finish();
                        }
                    });

            AlertDialog alert = alertDialogBuilder.create();
            alert.setCancelable(false);
            alert.show();
        } else {
            locationOn = true;

            if (locationOn) {
                ensureConnection();
            }
        }
    }

    public void ensureConnection() {
        if (!AppHelper.isConnectedToInternet(mContext)) {
            //Build the  alert dialog
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
            alertDialogBuilder.setTitle("No Internet connection");
            alertDialogBuilder.setMessage("This application requires internet connection.");

            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();
                            performFunctions();
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();
                            finish();
                        }
                    });

            AlertDialog alert = alertDialogBuilder.create();
            alert.setCancelable(false);
            alert.show();
        } else {
            connected = true;

            if (connected) {
                verifyLogin();
            }
        }
    }

    private void startApp() {

        Intent intent = new Intent(mContext, MainActivity.class);

        startActivity(intent);
        SplashActivity.this.finish();
    }

    public void redirectToLogin() {

        Intent loginIntent = new Intent(mContext, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(loginIntent);
        SplashActivity.this.finish();
    }
}
