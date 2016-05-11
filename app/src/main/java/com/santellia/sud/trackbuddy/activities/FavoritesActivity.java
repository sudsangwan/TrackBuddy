package com.santellia.sud.trackbuddy.activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.santellia.sud.trackbuddy.R;
import com.santellia.sud.trackbuddy.helper.ContactInfoAdapter;
import com.santellia.sud.trackbuddy.model.ContactInfo;

import java.util.ArrayList;

public class FavoritesActivity extends ActionBarActivity {

    private Context mContext = FavoritesActivity.this;
//    CroutonWrapper cr = new CroutonWrapper(FavoritesActivity.this);

    private Toolbar tb;

//    private static LoadContacts loadContacts;

    ArrayList<ContactInfo> contactInfos;
//    List<ContactInfo> temp;
    ListView listView;
    Cursor phones;

    ContentResolver resolver;
    ContactInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // ToolBar Setup
        tb = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(tb);

        tb.setNavigationIcon(R.drawable.ic_back);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tb.setLogo(R.mipmap.ic_app);
        getSupportActionBar().setTitle("TrackBuddy");
        tb.inflateMenu(R.menu.menu_main);

        // Contacts Setup
        contactInfos = new ArrayList<ContactInfo>();
        resolver = this.getContentResolver();
        listView = (ListView) findViewById(R.id.listview_contact);

        phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        LoadContact loadContact = new LoadContact();
        loadContact.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Load data on background
    class LoadContact extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Get Contact list from Phone

            if (phones != null) {
                Log.e("count", "" + phones.getCount());
                if (phones.getCount() == 0) {
                    Toast.makeText(FavoritesActivity.this, "No contacts in your contact list.",
                            Toast.LENGTH_LONG).show();
                }

            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                ContactInfo contactInfo = new ContactInfo();
                contactInfo.setContactName(name);
                contactInfo.setContactNumber(phoneNumber);
                contactInfos.add(contactInfo);
            }
        } else {
            Log.e("Cursor close 1", "....");
        }
        //phones.close();
        return null;
    }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new ContactInfoAdapter(contactInfos, FavoritesActivity.this);
            listView.setAdapter(adapter);

            // Select item on list click
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    Log.e("search", "contact click option");

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                    alertDialogBuilder.setTitle("TrackBuddy");
                    alertDialogBuilder.setMessage("Choose one option!");

                    alertDialogBuilder.setCancelable(false)
                            .setPositiveButton("SendInvite", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogInterface.dismiss();
                                }
                            });

                    AlertDialog alert = alertDialogBuilder.create();
                    alert.setCancelable(false);
                    alert.show();

                }
            });

            listView.setFastScrollEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        phones.close();
    }

}
