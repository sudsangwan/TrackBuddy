package com.santellia.sud.trackbuddy.global;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;

import com.santellia.sud.trackbuddy.helper.AppHelper;
import com.santellia.sud.trackbuddy.model.UserModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class Global {

    public boolean isLoggingModeOn = true;

    public UserModel user = new UserModel();
    public String userNumber = "";
    public String OTP;

    public JSONArray rep;

    private static Global GLOBAL_INSTANCE;

    private static synchronized Global setInstance() {
        GLOBAL_INSTANCE = new Global();
        return GLOBAL_INSTANCE;
    }

    public static synchronized Global getInstance() {
        if (GLOBAL_INSTANCE == null) {
            setInstance();
        }
        return GLOBAL_INSTANCE;
    }
//
//    public static synchronized void clearInstance() {
//        GLOBAL_INSTANCE = null;
//    }
//
//    public static void onTerminate(Context context) {
//        Global g = getInstance();
//        for (Field field : g.getClass().getDeclaredFields()) {
//            //field.setAccessible(true); // if you want to modify private fields
//            try {
//
//                    /*Gson gson = new Gson();
//                    String data = gson.toJson(field.get(g));
//                    //				System.out.println("Data : "+data);
//                    AppHelper.setSystemValue(field.getName(), data, context);*/
//
//
//            } catch (IllegalArgumentException e) {
//                e.printStackTrace();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void onReload(Context context) {
//
//        try {
//            Global g = getInstance();
//            Gson gson = new Gson();
//
//            // g.username = gson.fromJson(AppHelper.getSystemValue("username", context), String.class);
//            //g.password = gson.fromJson(AppHelper.getSystemValue("password", context), String.class);
//
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//
//        } catch (JsonSyntaxException e) {
//            e.printStackTrace();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Utilities related
     */

//    public boolean renewPromptShown = false;
    //For timer => countdown and a time
    public CountDownTimer ctTimer = null;
    public long sleepTimeSeconds = 0;

    /**
     * **************************************   Newtwork header related    ********************************
     */
    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_COOKIE = "session";

    /**
     * Checks the response headers for session cookie and saves it
     * if it finds it.
     *
     * @param headers Response Headers.
     */
    public final void checkSessionCookie(Map<String, String> headers, Context mContext) {

        if (headers.containsKey(SET_COOKIE_KEY)
                && headers.get(SET_COOKIE_KEY).startsWith(SESSION_COOKIE)) {

            String cookie = headers.get(SET_COOKIE_KEY);
            if (cookie.length() > 0) {
                String[] splitCookie = cookie.split(";");

                for (int i = 0; i < splitCookie.length; i++) {

                    if (splitCookie[i].contains(SESSION_COOKIE)) {
                        String[] splitSessionId = splitCookie[i].split("=");
                        cookie = splitSessionId[1];
                        AppHelper.setSystemValue(SESSION_COOKIE, cookie, mContext);
                    }
                }

                Log.i("COOKIE", cookie);
            }
        }
    }

    /**
     * Adds session cookie to headers if exists.
     *
     * @param headers
     */
    public final void addSessionCookie(Map<String, String> headers, Context mContext) {

        String sessionId = AppHelper.getSystemValue(SESSION_COOKIE, mContext);

        if (sessionId != null && sessionId.length() > 0) {

            StringBuilder builder = new StringBuilder();
            builder.append(SESSION_COOKIE);
            builder.append("=");
            builder.append(sessionId);

            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }

            headers.put(COOKIE_KEY, builder.toString());
        }
    }

    public boolean getStartUpInformation(JSONObject response, Context mContext) {
        return true;
    }
}
