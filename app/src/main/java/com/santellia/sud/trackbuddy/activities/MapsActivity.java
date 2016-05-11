package com.santellia.sud.trackbuddy.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.santellia.sud.trackbuddy.R;
import com.santellia.sud.trackbuddy.global.API;
import com.santellia.sud.trackbuddy.global.Global;
import com.santellia.sud.trackbuddy.helper.AppHelper;
import com.santellia.sud.trackbuddy.libhelpers.CroutonWrapper;
import com.santellia.sud.trackbuddy.network.NetworkQueueHandler;
import com.santellia.sud.trackbuddy.network.NetworkWorker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public static final String TAG = MapsActivity.class.getSimpleName();

    private Context mContext = MapsActivity.this;
    CroutonWrapper cr;
    private Toolbar tb;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tb = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(tb);

        tb.setLogo(R.mipmap.ic_app);
        getSupportActionBar().setTitle("TrackBuddy");
        tb.inflateMenu(R.menu.menu_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
            alertDialogBuilder.setTitle("Exit App");
            alertDialogBuilder.setMessage("Choose one option!");

            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            AppHelper.removeSystemValues(mContext);
                            dialogInterface.dismiss();
                            finish();
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();
                            finish();
                        }
                    })
                    .setNeutralButton("Stay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();
                        }
                    });

                AlertDialog alert = alertDialogBuilder.create();
                alert.setCancelable(false);
                alert.show();

            return true;
        }

        if (id == R.id.action_favorites) {

            Intent intent = new Intent(mContext, FavoritesActivity.class);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

        }
    }

    /**
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(LatLng latLng, String message) {

        mMap.addMarker(new MarkerOptions().position(latLng).title(message));
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
    }

    private void myLocation(LatLng latLng, String message) {

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title(message));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {

            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }

        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        myLocation(latLng, "Me");
        sendLocation(location);
    }

    private void sendLocation(Location location) {

        //CroutonWrapper
        cr = new CroutonWrapper(MapsActivity.this);

        sendLatLong(location);
        receiveLatLong();
    }

    private void sendLatLong(Location location) {

        JSONObject json = new JSONObject();
        try {
            json.put("lat", location.getLatitude());
            json.put("lng", location.getLongitude());
            json.put("time", location.getTime());
            json.put("speed", location.getSpeed());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        if (AppHelper.isConnectedToInternet(mContext)) {
            Log.i(TAG, "Internet available");
            NetworkQueueHandler.initContext(mContext);
            NetworkWorker.authPost1(getApplicationContext(), API.USERLOCATION,    //APP.API.DEVLOGIN
                    "SendLocation",
                    null,
                    json,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                JSONObject rep = response;

                                if (!rep.getString("ok").equals(true)) {
                                    //cr.croutonAlert("Try Again");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                cr.croutonAlert("Sending location failed!");
                            }
                        }
                    },
                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i(TAG, error.toString());
                            cr.croutonAlert("Sending location failed!");
                        }
                    });

        } else {
            cr.croutonAlert("No Internet Found !");
        }
    }

    private void receiveLatLong() {

        if (AppHelper.isConnectedToInternet(mContext)) {
            Log.i(TAG, "Internet available");
            NetworkQueueHandler.initContext(mContext);
            NetworkWorker.authGet(getApplicationContext(), API.USERLOCATION,    //APP.API.DEVLOGIN
                    "GetLocation",
                    null,
                    null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                JSONArray rep = response.getJSONArray("locations");
                                //JSONArray rep = Global.getInstance().rep;

                                if(rep!=null) {
                                    Global.getInstance().rep = rep;

                                    for (int i = 0; i < rep.length(); i++) {
                                        JSONObject jsonObject = rep.getJSONObject(i);

                                        Double lat = jsonObject.getDouble("lat");
                                        Double lng = jsonObject.getDouble("lng");
                                        String message = jsonObject.getString("name");
//                                        String number = jsonObject.getString("phone");

                                        LatLng latLng = new LatLng(lat, lng);

                                        setUpMap(latLng, message);
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                cr.croutonAlert("Receiving location failed!");
                            }
                        }
                    },
                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i(TAG, error.toString());
                            cr.croutonAlert("Receiving location failed!");
                        }
                    });

        } else {
            cr.croutonAlert("No Internet Found !");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }
}