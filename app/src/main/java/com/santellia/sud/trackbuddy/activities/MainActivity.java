package com.santellia.sud.trackbuddy.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.santellia.sud.trackbuddy.R;
import com.santellia.sud.trackbuddy.global.API;
import com.santellia.sud.trackbuddy.helper.AppHelper;
import com.santellia.sud.trackbuddy.libhelpers.CroutonWrapper;
import com.santellia.sud.trackbuddy.network.NetworkQueueHandler;
import com.santellia.sud.trackbuddy.network.NetworkWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity {

    public static final String TAG = MapsActivity.class.getSimpleName();

    private Context mContext;
    CroutonWrapper cr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        cr = new CroutonWrapper(MainActivity.this);

        readContacts();

        Intent mapIntent = new Intent(this, MapsActivity.class);
        startActivity(mapIntent);
        finish();
    }

    private void readContacts() {

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor people = getContentResolver().query(uri, projection, null, null, null);

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        JSONArray array = new JSONArray();

        people.moveToFirst();

        if (people.getCount() == 0) {
            return;
        }

        do {
            String name = people.getString(indexName);
            String phone = people.getString(indexNumber);

            JSONObject json = new JSONObject();
            try {
                json.put("name", name);
                json.put("phone", phone);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            array.put(json);

        }while (people.moveToNext());

        if(array.length()>0)
        {
            sendContacts(array);
        }
    }

    private void sendContacts(JSONArray array) {

        JSONObject json = new JSONObject();

        try {
            json.put("contacts",array);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        if (AppHelper.isConnectedToInternet(mContext)) {
            Log.i(TAG, "Internet available");
            NetworkQueueHandler.initContext(mContext);
            NetworkWorker.authPost1(getApplicationContext(), API.USERCONTACT,    //APP.API.DEVLOGIN
                    "SendContact",
                    null,
                    json,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                JSONObject rep = response;

                                if (!rep.getString("ok").equals(true)) {
                                    cr.croutonAlert("Try Again");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                cr.croutonAlert("Sending contact failed!");
                            }
                        }
                    },
                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i(TAG, error.toString());
                            cr.croutonAlert("Sending contact failed!");
                        }
                    });

        } else {
            cr.croutonAlert("No Internet Found !");
        }
    }
}