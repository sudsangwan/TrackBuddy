package com.santellia.sud.trackbuddy.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.santellia.sud.trackbuddy.global.API;
import com.santellia.sud.trackbuddy.global.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NetworkWorker {

    /**
     * Class that handles network calls. Calls are made via Network queue handler and subsequently
     * requestqueue is incremented and decremented.
     * <p/>
     * LIST OF FUNCTIONS**
     * <p/>
     * 1) authGet(........) ==> network hit using get method
     * 2)authPost (...........) ==>  network hit using post method
     */

    private static final String tag = "NetworkWorker";
    static Global gInstance = Global.getInstance();
    private Map<String, String> headers;

    /**
     * This function hits API using GET method,
     * its parameter are same as that of volley JSON request.
     * response (through Volley)is transferred to the responseListener of the calling function,m.'
     *
     * Means onResponseListener and onerrorListener need to be implemented in the calling function as well.
     * <p/>
     * volley jsonrequest params => (method, url,?? , response.litener{what to be done}, errorListener{what to be done})
     */

    public static void authGet(final Context context, String url, String tag,
                               Map<String, String> params, JSONObject jsonObject,
                               final Response.Listener<JSONObject> responseListener,
                               final Response.ErrorListener errorListener) {

        String requestURL = urlBuilder(url, params);

        Log.i("CREATEPATH",url);

        Log.d(tag, "RequestUrl : " + requestURL);

        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.GET, requestURL, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
    					Log.d("RequestUrl : ", response.toString());
                        NetworkQueueHandler.decrementRequestCounter();
                        responseListener.onResponse(response);   //response Listener of parent is called
                    }
                },
                new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkQueueHandler.decrementRequestCounter();
                Toast.makeText(context, "Oops! Some problem occurred.", Toast.LENGTH_SHORT).show();
                errorListener.onErrorResponse(error);       //Error listener fo parent is called
            }
        })

        {
            @Override
            public String getBodyContentType() {

                return "application/json; charset=utf-8";
            }

            /** (non-Javadoc)
             * @see com.android.volley.toolbox.StringRequest#parseNetworkResponse(com.android.volley.NetworkResponse)
             */
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                // since we don't know which of the two underlying network vehicles
                // will Volley use, we have to handle and store session cookies manually
                gInstance.checkSessionCookie(response.headers , context);
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = super.getHeaders();

                if (headers == null
                        || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<String, String>();
                }

                setHeaders(headers);
                setHeaders(headers);
                gInstance.addSessionCookie(headers, context);

                return headers;
            }
        };

        // add it to the RequestQueue, Here network queue handler is called

        getRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        NetworkQueueHandler.getInstance().addToRequestQueue(getRequest, "");  //Here request is added and counter is incremented
        NetworkQueueHandler.incrementRequestCounter();
    }

    /**
     * This is similar to authGet. Only difference is that POST method is used here
     * <p/>
     * NOTE IT IS UNIMPLEMENTED YET==> works as GET method, as parameters are builded id UrlBuilder
     * <p/>
     * To make it POST truly ==> GETPARAMS() needs to be overridden
     */
    public static void authPost(final Context context, String url, String tag,
                                Map<String, String> params, JSONObject jsonObject,
                                final Response.Listener<JSONObject> responseListener,
                                final Response.ErrorListener errorListener) {

//		params.put("Content-Type", "application/x-www-form-urlencoded");
        String requestURL = urlBuilder(url, params);
        Log.d(tag, "RequestUrl : " + requestURL);

        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST, requestURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//						NetworkQueueHandler.decrementRequestCounter();
                        responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//
                NetworkQueueHandler.decrementRequestCounter();
                Toast.makeText(context, "Oops! Some problem occurred.", Toast.LENGTH_SHORT).show();
                errorListener.onErrorResponse(error);
            }
        });

        // add it to the RequestQueue

        postRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Ecommerce SingleTon.mRequestQueue.add(getRequest);
        NetworkQueueHandler.getInstance().addToRequestQueue(postRequest, "");
        NetworkQueueHandler.incrementRequestCounter();

    }

    public static void authPost1(final Context context, String url, String tag,
                                 final Map<String, String> params, JSONObject jsonObject,
                                 final Response.Listener<JSONObject> responseListener,
                                 final Response.ErrorListener errorListener) {

        String requestURL = urlBuilder(url, null);
        Log.d(tag, "RequestUrl : " + requestURL);

        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST, requestURL, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        responseListener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkQueueHandler.decrementRequestCounter();
                handleError(context, error);
                errorListener.onErrorResponse(error);
            }
        })

        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            /** (non-Javadoc)
             * @see com.android.volley.toolbox.StringRequest#parseNetworkResponse(com.android.volley.NetworkResponse)
             */
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                // since we don't know which of the two underlying network vehicles
                // will Volley use, we have to handle and store session cookies manually
                gInstance.checkSessionCookie(response.headers , context);
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = super.getHeaders();

                if (headers == null
                        || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<String, String>();
                }

                setHeaders(headers);
                gInstance.addSessionCookie(headers, context);

                return headers;
            }
        };

        // add it to the RequestQueue
        postRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        NetworkQueueHandler.getInstance().addToRequestQueue(postRequest, "");
        NetworkQueueHandler.incrementRequestCounter();
    }

    private static void handleError(Context context, VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        String res = null;

        if (networkResponse != null) {

            try {
                res = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                JSONObject response = new JSONObject(res);
                String err = response.getString("error");

                Toast.makeText(context, err, Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * TO reset password.
     * Note as it is in POST method :  UrlBuilder is only used to create path. Parameters need to be hidden,
     * so getParams() is overridden here and a hashmap is returned.
     * <p/>
     * API will hit== > Server will then send password changing code to email address.
     *
     * @param context
     * @param url
     * @param tag              ==>  of calling funtion
     * @param email            ==> to where resel link will be sent
     * @param jsonObject       ==>
     * @param responseListener
     * @param errorListener
     */

    public static void authPostResetPassword(final Context context, String url, String tag,
                                             final String email, JSONObject jsonObject,
                                             final Response.Listener<String> responseListener,
                                             final Response.ErrorListener errorListener) {

        String requestURL = urlBuilder(url);
//		Log.d(tag, "RequestUrl : " + requestURL);

        StringRequest postRequest = new StringRequest(Request.Method.POST, requestURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
//
                        NetworkQueueHandler.decrementRequestCounter();
                        responseListener.onResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                NetworkQueueHandler.decrementRequestCounter();
                Toast.makeText(context, "Oops! Some problem occurred.", Toast.LENGTH_SHORT).show();
                errorListener.onErrorResponse(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("key", API.KEY);
                params.put("email", email);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        // add it to the RequestQueue
//		Log.e("EcommerceSingleTon", "Adding request");
        postRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // EcommerceSingleTon.mRequestQueue.add(getRequest);
        NetworkQueueHandler.getInstance().addToRequestQueue(postRequest, "");
        NetworkQueueHandler.incrementRequestCounter();

    }

    //Builds url from base url and the url parameters
    @SuppressLint("NewApi")
    public static String urlBuilder(String createPath,
                                    Map<String, String> params) {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(API.SCHEME)
                .encodedAuthority(API.AUTHORITY)
                .path(createPath);
        if (params != null) {
            for (String key : params.keySet()) {
                if (params.get(key) != null) {

                    builder.appendQueryParameter(key, params.get(key));
                } else {
                    throw new IllegalArgumentException(
                            "params must not contain any null values");
                }
            }
        }
        String ret = builder.build().toString();
        // To decode @ back to its original form. Not a good solution.
        ret = Uri.decode(ret);
        // Log.i("BUILDER:", ret);
        if (Global.getInstance().isLoggingModeOn) {
            Log.d("BUILDER:", ret);
        }
        return ret;
    }

    /* Builds url from base url and the url parameters */
    @SuppressLint("NewApi")
    public static String urlBuilder(String createPath) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(API.SCHEME)
                .encodedAuthority(API.AUTHORITY)
                .path(createPath);

        String ret = builder.build().toString();
        // To decode @ back to its original form. Not a good solution.
        ret = Uri.decode(ret);
        // Log.i("BUILDER:", ret);
        if (Global.getInstance().isLoggingModeOn) {
            Log.d("BUILDER:", ret);
        }
        return ret;
    }


    /**
     * This function hits API using GET method,
     * its parameter are same as that of volley JSON request.
     * response (through Volley)is transferred to the responseListener of the calling function,m.'
     *
     * Means onResponseListener and onerrorListener need to be implemented in the calling function as well.
     * <p/>
     * volley jsonrequest params => (method, url,?? , response.litener{what to be done}, errorListener{what to be done})
     */


    public static void authDel(final Context context, String url, String tag,
                               Map<String, String> params, JSONObject jsonObject,
                               final Response.Listener<JSONObject> responseListener,
                               final Response.ErrorListener errorListener) {

        String requestURL = urlBuilder(url, params);
        Log.i("CREATEPATH",url);

        Log.d(tag, "RequestUrl : " + requestURL);
        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.DELETE, requestURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("RequestUrl : ", response.toString());
//						System.out.println(response);
                        NetworkQueueHandler.decrementRequestCounter();
                        responseListener.onResponse(response);   //response Listener of parent is called
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkQueueHandler.decrementRequestCounter();
                Toast.makeText(context, "Oops! Some problem occurred.", Toast.LENGTH_SHORT).show();
                errorListener.onErrorResponse(error);       //Error listener fo parent is called
            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            /** (non-Javadoc)
             * @see com.android.volley.toolbox.StringRequest#parseNetworkResponse(com.android.volley.NetworkResponse)
             */
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                // since we don't know which of the two underlying network vehicles
                // will Volley use, we have to handle and store session cookies manually
                gInstance.checkSessionCookie(response.headers , context);
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = super.getHeaders();

                if (headers == null
                        || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<String, String>();
                }

                setHeaders(headers);
                gInstance.addSessionCookie(headers, context);

                return headers;
            }
        };

        // add it to the RequestQueue, Herenetwork queue handeler is called

        getRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // EcommerceSingleTon.mRequestQueue.add(getRequest);
        NetworkQueueHandler.getInstance().addToRequestQueue(getRequest, "");  //Here request is added and counter is incremented
        NetworkQueueHandler.incrementRequestCounter();
    }

    public static void authGet1(final Context context, String url, String tag,
                               Map<String, String> params, JSONObject jsonObject,
                               final Response.Listener<JSONObject> responseListener,
                               final Response.ErrorListener errorListener) {

        String requestURL = urlBuilder(url, params);
        Log.i("CREATEPATH",url);

        Log.d(tag, "RequestUrl : " + requestURL);
        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.GET, requestURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("RequestUrl : ", response.toString());
//						System.out.println(response);
                        NetworkQueueHandler.decrementRequestCounter();
                        responseListener.onResponse(response);   //response Listener of parent is called
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkQueueHandler.decrementRequestCounter();
                Toast.makeText(context, "Oops! Some problem occurred.", Toast.LENGTH_SHORT).show();
                errorListener.onErrorResponse(error);       //Error listener fo parent is called
            }
        });

        // add it to the RequestQueue, Here network queue handler is called

        getRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // EcommerceSingleTon.mRequestQueue.add(getRequest);
        NetworkQueueHandler.getInstance().addToRequestQueue(getRequest, "");  //Here request is added and counter is incremented
        NetworkQueueHandler.incrementRequestCounter();
    }

    public static void setHeaders(Map<String,String> headers) {
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("User-Agent", "AndroidApp/1");

    }
}
