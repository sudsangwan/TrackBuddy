package com.santellia.sud.trackbuddy.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

public class AppHelper {

    private static final String tag = "AppHelper";

    /**
     * A variable to check if the Properties file has been loaded or not to
     * prevent it from being loaded more than once.
     */
    private static boolean ispropertyLoaded = false;

    private static final Properties customProperties = new Properties();
    public static final String DATE_DAY_MONTH_YEAR = "dd MMM, yyyy";
    public static final String DATE_YEAR_MONTH_DAY = "yyyy-MM-dd hh:mm:sss";
    public static final String DATE_YEAR_MONTH_DAY_SHORT = "yyyy-MM-dd";
    public static final String DATE_DAY_MONTH_YEAR_SHORT = "dd-MM-yyyy";
    public static final String DATE_DAY_DATE_MONTH = "E, dd MMM";
    private static final String SHARED_PREFERENCES_NAME = "TrackBuddyPreferences";

    // --------------------------------------------------------------------------

    public static String getApplicationDirectory(Context context) {
        PackageManager m = context.getPackageManager();
        String s = context.getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(s, 0);
            s = p.applicationInfo.dataDir;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
            s = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return s;
    }

    public static void removeSystemValues(Context p_context) {
        SharedPreferences myPrefs = p_context
                .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        if (myPrefs.getAll().size() > 0) {
            SharedPreferences.Editor prefsEditor = myPrefs.edit();
            prefsEditor.clear();
            prefsEditor.commit();
        }
    }

    public static void setSystemValue(String key, String value, Context p_context) {
        SharedPreferences myPrefs = p_context
                .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();

    }

    public static String getSystemValue(String key, Context p_context) {
        String value = null;
        SharedPreferences myPrefs = p_context
                .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        value = myPrefs.getString(key, null);

        return value;
    }

    public static void setIntSystemValue(String key, int value, Context p_context) {
        SharedPreferences myPrefs = p_context
                .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putInt(key, value);
        prefsEditor.commit();
    }

    public static int getIntSystemValue(String key, Context p_context) {
        int value = -1;
        SharedPreferences myPrefs = p_context
                .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        value = myPrefs.getInt(key, -1);

        return value;
    }
    /**
     * This methods loads the properties from the custom properties file placed
     * inside res/raw folder.
     *
     * @param p_context The context of the calling Activity or view.
     */
    public static synchronized void loadProperties(Context p_context) {

        if (!ispropertyLoaded) {
            InputStream l_stream = new ByteArrayInputStream(getBytesFromFile(
                    "customproperties", p_context));

            try {
                customProperties.load(l_stream);

            } catch (IOException e) {
                e.printStackTrace();
            }
            ispropertyLoaded = true;
        }
    }
    /**
     * This method is used to fetch the Property File Byte array from apk RAW
     * folder or from property hash table
     *
     * @param p_name    The name of the image to be fetched.
     * @param p_context The context of the Calling Activity or view.
     * @return The byte array of property file fetched from the database based
     * on the name entered by the user.
     */
    public static byte[] getBytesFromFile(String p_name, Context p_context) {
        // this object will store a reference to the Activity's resources
        Resources resources = p_context.getResources();
        byte response[] = null;

        try {

            response = AppHelper.convertInputStreamToByteArray(resources
                    .openRawResource(getRawFile(p_name, p_context)));

        } catch (Exception e) {
            System.out.println("caught an exception of custom properties");
        }
        return response;
    }
    /**
     * This is method that converts InputStream to Custom p byte array
     *
     * @param p_stream holds the input stream
     */
    public static byte[] convertInputStreamToByteArray(InputStream p_stream) {

        if (p_stream != null) {
            // System.out.println("PROPERTIES NOT NULL");
            int read = 0;
            byte[] buffer = new byte[8192];
            ByteArrayOutputStream l_ous = new ByteArrayOutputStream();

            try {
                try {
                    while ((read = p_stream.read(buffer)) != -1) {
                        l_ous.write(buffer, 0, read);
                    }

                } finally {
                    try {
                        if (l_ous != null)
                            l_ous.close();

                    } catch (IOException e) {

                    }
                }
            } catch (IOException io) {

            }
            return l_ous.toByteArray();

        } else {

            return null;
        }
    }

    public static int getRawFile(String p_name, Context p_context) {

        return p_context.getResources().getIdentifier(p_name, "raw",
                p_context.getPackageName());
    }

    /**
     * Returns the string with the property from custom properties file.
     *
     * @param p_propname The name of the property whose value needs to be fetched.
     * @return The value of the property if found in the file, empty string("")
     * if no such property found.
     */
    public static String getProperty(String p_propname) {

        if (customProperties.size() > 0) {
            return customProperties.getProperty(p_propname, "");

        } else {
            return "";
        }
    }

    // -------------------------------------------------------------------------

    public static SparseArray<String> parseStringArray(int stringArrayResourceId, Context mContext) {

        if (mContext != null) {
            String[] stringArray = mContext.getResources().getStringArray(
                    stringArrayResourceId);
            SparseArray<String> outputArray = new SparseArray<String>(
                    stringArray.length);

            for (String entry : stringArray) {
                String[] splitResult = entry.split("\\|", 2);
                outputArray
                        .put(Integer.valueOf(splitResult[0]), splitResult[1]);
            }
            return outputArray;
        }

        return null;
    }

    public static boolean isConnectedToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }

        return false;
    }



    public static String getTodaysDate() {
        Calendar c = GregorianCalendar.getInstance(TimeZone.getDefault(),
                Locale.US);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_YEAR_MONTH_DAY_SHORT,
                Locale.US);
        String strDate = sdf.format(c.getTime());

        return strDate;
    }

    public static String getCurrentDate(Date date) {
        String timeStamp = new String();
        try {
            SimpleDateFormat fmtOut = new SimpleDateFormat(DATE_YEAR_MONTH_DAY,
                    Locale.US);
            timeStamp = fmtOut.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return timeStamp;
    }

    public static String getStringFromDate(Date date) {
        String timeStamp = new String();
        try {
            SimpleDateFormat fmtOut = new SimpleDateFormat(DATE_YEAR_MONTH_DAY_SHORT,
                    Locale.US);
            timeStamp = fmtOut.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return timeStamp;
    }

    public static Date getDateFromString(String toDate) {
        SimpleDateFormat format = new SimpleDateFormat(AppHelper.DATE_YEAR_MONTH_DAY_SHORT, Locale.getDefault());
        Date d = new Date();
        try {
            d = format.parse(toDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return d;
    }

    public static Date getDateFromString(String toDate, String dateFormat) {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.getDefault());
        Date d = new Date();
        try {
            d = format.parse(toDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return d;
    }

    /**
     * This method convets dp unit to equivalent device specific value in
     * pixels.
     *
     * @param dp      A value in dp(Device independent pixels) unit. Which we need
     *                to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent Pixels equivalent to dp according to
     * device
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        // Log.i(tag, "DP : " + dp + " converted to " + px + " pixels");
        return px;
    }

    /**
     * This method converts device specific pixels to device independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent db equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        // Log.i(tag, "Pixels : " + px + " converted to " + dp + " DP");
        return dp;
    }

    public static String checkNull(String toCheck) {
        if (toCheck != null && toCheck.length() > 0 && !toCheck.equalsIgnoreCase("null")) {
            return toCheck;
        } else {
            return "";
        }
    }
}
