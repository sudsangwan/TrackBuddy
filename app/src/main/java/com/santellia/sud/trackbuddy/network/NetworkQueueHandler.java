package com.santellia.sud.trackbuddy.network;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class NetworkQueueHandler {

	private RequestQueue mRequestQueue;
    public static Context mContext;
    public ImageLoader mImageLoader;
    private static final String TAG = "DEFTYPE";
    private static NetworkQueueHandler NQH_INSTANCE;
    private static int requestCounter = 0;
    
    public static int getRequestCounter() {
		return requestCounter;
	}

	public static void incrementRequestCounter() {
		NetworkQueueHandler.requestCounter++;
	}
	
	public static void decrementRequestCounter() {
		NetworkQueueHandler.requestCounter--;
	}

	private static synchronized NetworkQueueHandler setInstance() {
    	NQH_INSTANCE = new NetworkQueueHandler();
    	requestCounter = 0;
		return NQH_INSTANCE;
	}

	public static synchronized NetworkQueueHandler getInstance() {

		if (NQH_INSTANCE == null) {
			setInstance();
		}
		return NQH_INSTANCE;
	}

	public static synchronized void clearInstance() {
		NQH_INSTANCE = null;
	}

	public  static void initContext(Context context){
    	mContext = context;
    }

	public synchronized RequestQueue getRequestQueue() {

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
            mRequestQueue.start();
        }
 
        return mRequestQueue;
    }
 	
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }
 
    public <T> void addToRequestQueue(Request<T> req) {

        req.setTag(TAG);
        getRequestQueue().add(req);
    }
 
    public 	void cancelPendingRequests(Object tag) {

        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
    

}
