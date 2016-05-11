package com.santellia.sud.trackbuddy.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.santellia.sud.trackbuddy.R;
import com.santellia.sud.trackbuddy.global.API;
import com.santellia.sud.trackbuddy.global.Global;
import com.santellia.sud.trackbuddy.helper.AppHelper;
import com.santellia.sud.trackbuddy.libhelpers.CroutonWrapper;
import com.santellia.sud.trackbuddy.model.UserModel;
import com.santellia.sud.trackbuddy.network.NetworkQueueHandler;
import com.santellia.sud.trackbuddy.network.NetworkWorker;
import com.santellia.sud.trackbuddy.receivers.SmsReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String tag = "LoginActivity";

    private Context mContext;
    private EditText etCountryCode, etMobileNumber, etPassword, etName;
    private TextView tvLogin, tvRequestOTP, tvResendOTP, tvChangeNumber, tvOTPMessage;

    ProgressDialog pDialog;

    Global gInstance = Global.getInstance();

    boolean OTPSTAGE = false;
    boolean OTPResended = false;

    String userNumber, countryCode;

    CroutonWrapper cr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView textView = (TextView) findViewById(R.id.tvAppName);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/georgia_regular_new.ttf");
        textView.setTypeface(tf);

        init();
        setOnClickListeners();
    }

    private void init() {
        //Context
        mContext = LoginActivity.this;

        //CroutonWrapper
        cr = new CroutonWrapper(LoginActivity.this);

        //Fields
        etCountryCode = (EditText) findViewById(R.id.etCountryCode);
        etMobileNumber = (EditText) findViewById(R.id.etMobileNumber);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etName = (EditText) findViewById(R.id.etName);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvOTPMessage = (TextView) findViewById(R.id.tvOTPMessage);
        tvRequestOTP = (TextView) findViewById(R.id.tvRequestOTP);
        tvResendOTP = (TextView) findViewById(R.id.tvResendOTP);
        tvChangeNumber = (TextView) findViewById(R.id.tvChangeNumber);
    }

    private void setOnClickListeners() {

        etMobileNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    tvRequestOTP.performClick();
                }
                return false;
            }
        });

        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    tvLogin.performClick();
                }
                return false;
            }
        });

        tvRequestOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userNumber = etMobileNumber.getText().toString().trim();
                countryCode = etCountryCode.getText().toString().trim();
                String globalNumber = countryCode + userNumber;

                    if (TextUtils.isEmpty(countryCode)) {
                        cr.croutonAlert("Please enter your country code");

                        return;
                    }

                    if (TextUtils.isEmpty(userNumber)) {
                        cr.croutonAlert("Please enter your Mobile Number!");

                        return;

                    } else if (!PhoneNumberUtils.isGlobalPhoneNumber(globalNumber) || (userNumber.length() != 10)) {
                        cr.croutonAlert("Please enter a valid Mobile Number!");

                        return;
                    }

                requestOTP(userNumber);
            }
        });

        tvResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!OTPSTAGE || userNumber == null || userNumber.length() != 10) {
                    cr.croutonAlert("Please enter a valid Mobile Number!");
                    return;
                }

                requestOTP(userNumber);
            }
        });

        tvChangeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!OTPSTAGE || userNumber == null || userNumber.length() != 10) {
                    cr.croutonAlert("Please enter a valid Mobile Number!");
                    return;
                }

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        mContext);

                alertDialogBuilder.setMessage("Are you sure you want to leave this session?");
                alertDialogBuilder.setTitle("TrackBuddy");

                // setup a dialog window
                alertDialogBuilder
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                //initialize edit texts with empty strings

                                etMobileNumber.setText("");
                                etPassword.setText("");
                                etName.setText("");

                                recreate();
                            }
                        })
                        .setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });

                // create an alert dialog
                AlertDialog alert = alertDialogBuilder.create();
                alert.setCancelable(false);
                alert.show();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = etMobileNumber.getText().toString().trim();
                String password = etPassword.getText().toString();
                String urName = etName.getText().toString();

                if (TextUtils.isEmpty(userName)) {
                    cr.croutonAlert("Please enter a Number!");

                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    cr.croutonAlert("Please enter a OTP!");

                    return;
                }

                if (TextUtils.isEmpty(urName)) {
                    cr.croutonAlert("Please enter your Name!");

                    return;
                }

                authenticate(userName, password, urName);
            }
        });
    }

    private void requestOTP(String number) {

        HashMap<String, String> loginData = new HashMap<String, String>();
        loginData.put("key", API.KEY);

        gInstance.userNumber = number;

        JSONObject json = new JSONObject();
        try {
            json.put("phone", number);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        if (AppHelper.isConnectedToInternet(mContext)) {
            Log.i(tag, "Internet available");
            pDialog = ProgressDialog.show(mContext, "", "Requesting OTP...", true);
            NetworkQueueHandler.initContext(mContext);
            NetworkWorker.authPost1(getApplicationContext(), API.REQUESTOTP,    //APP.API.DEVLOGIN
                    "Login",
                    loginData,
                    json,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            hideDialog();

                            try {
                                JSONObject rep = response;

                                if (!rep.getString("status").equals("otp sent")) {
                                    cr.croutonAlert("Try Again");

                                } else {
                                    arrangeUIForOTP();
                                    registerOTPReceiver();
                                    OTPSTAGE = true;
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                cr.croutonAlert("Authentication failed! please try again.");
                            }
                        }
                    },
                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideDialog();
                            Log.i(tag, error.toString());
                            cr.croutonAlert("Authentication failed! please try again.");
                        }
                    });

        } else {
            cr.croutonAlert("No Internet Found !");
        }
    }

    private void authenticate(String userName, String password1, String urName) {
        final String phone = userName;
        final String otp = password1;
        final String name = urName;
        //Global gInstance = Global.getInstance();
        Map<String, String> params = new HashMap<String, String>();

        JSONObject json = new JSONObject();
        try {
            json.put("phone", phone);
            json.put("otp", otp);
            json.put("name", name);

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        if (AppHelper.isConnectedToInternet(mContext)) {
            Log.i(tag, "Internet available");

            showDialog("Authenticating...");
            NetworkQueueHandler.initContext(mContext);
            NetworkWorker.authPost1(getApplicationContext(),API.LOGIN,    //APP.API.DEVLOGIN
                    "Login",
                    params,
                    json,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            hideDialog();

                            boolean success = gInstance.getStartUpInformation(response, mContext);

                            if (success) {
                                Gson gson = new Gson();
                                String userData = gson.toJson(gInstance.user);
                                AppHelper.setSystemValue("user", userData, mContext);
                                logInUser(gInstance.user);

                            } else {
                                etPassword.setText("");
                                cr.croutonAlert("Temporary network problem occured");
                            }
                        }
                    },
                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideDialog();
                            Log.i(tag, error.toString());
                            etPassword.setText("");
                            cr.croutonAlert("Authentication failed! please try again.");
                        }
                    });

        } else {
            cr.croutonAlert("No internet found !");
        }
    }

    /**
     * logs in user directly given the user profile as input.
     *
     * @param user
     */
    private void logInUser(UserModel user) {

        Intent l_intent = new Intent(mContext, MainActivity.class);
        l_intent.putExtra("id", user.getUserId());
        l_intent.putExtra("name", user.getUsername());
        l_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mContext.startActivity(l_intent);
        LoginActivity.this.finish();
    }

    private void showDialog(String s) {

        pDialog = ProgressDialog.show(LoginActivity.this, "", s, true);
    }

    private void hideDialog() {

        if (pDialog != null && pDialog.isShowing() && !isFinishing()) {
            pDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void arrangeUIForOTP() {

        tvRequestOTP.setVisibility(View.GONE);
        etPassword.setVisibility(View.VISIBLE);
        etName.setVisibility(View.VISIBLE);
        tvLogin.setVisibility(View.VISIBLE);
        tvOTPMessage.setVisibility(View.VISIBLE);
        etCountryCode.setVisibility(View.GONE);
        etMobileNumber.setEnabled(false);
        tvResendOTP.setVisibility(View.VISIBLE);
        tvChangeNumber.setVisibility(View.VISIBLE);
        tvResendOTP.setText("Resend OTP");
        tvResendOTP.setEnabled(false);

        startTimer(30000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
        // Unregister the SMS receiver
        unregisterOTPReciever();
    }

    @Override
    public void onClick(View view) {

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
     * @param time = > total timer left
     */
    public void startTimer(long time) {
        if (gInstance.ctTimer != null) {
            gInstance.ctTimer.cancel();
            gInstance.ctTimer = null;
        }

        gInstance.ctTimer = new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
                gInstance.sleepTimeSeconds = millisUntilFinished;

                if (OTPSTAGE && !OTPResended) {
                    tvResendOTP.setText("Resend OTP(" + (millisUntilFinished) / 1000 + ")");

                } else {
                    tvResendOTP.setVisibility(View.GONE);
                }
            }

            public void onFinish() {

                if (OTPSTAGE && !OTPResended) {
                    tvResendOTP.setText("Resend OTP");
                    tvResendOTP.setEnabled(true);
                    OTPResended = true;
                }
            }
        };

        gInstance.ctTimer.start();
    }

    private void cancelTimer() {
        if (gInstance.ctTimer == null)
            return;
        gInstance.ctTimer.cancel();
        gInstance.ctTimer = null;

        gInstance.sleepTimeSeconds = 0;
    }

    //Register receiver

    public void registerOTPReceiver() {
        // Registering our BroadcastReceiver to listen to orders
        // from inside our own application.
        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .registerReceiver(optBroadcastReceiver, new IntentFilter(SmsReceiver.BROADCAST_OTP));
    }

    public void unregisterOTPReciever() {
        // Registering our BroadcastReceiver to listen to orders
        // from inside our own application.
        if(optBroadcastReceiver!=null){
            LocalBroadcastManager
                    .getInstance(getApplicationContext())
                    .unregisterReceiver(optBroadcastReceiver);
        }
    }

    //Broadcast Receiver
    /**
     * The thing that will keep an eye on LocalBroadcasts
     */
    BroadcastReceiver optBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(gInstance.OTP == null || gInstance.OTP.length()!=4)
            {
                return;
            }
            etPassword.setText(gInstance.OTP);
        }
    };
}

