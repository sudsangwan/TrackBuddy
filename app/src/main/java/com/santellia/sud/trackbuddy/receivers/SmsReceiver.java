package com.santellia.sud.trackbuddy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.santellia.sud.trackbuddy.global.Global;

/**
 * Note
 *
 * This Reciever will be used for SMs red for Alarms in case user forgets to read the notification
 */
public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = SmsReceiver.class.getSimpleName();

    private final String SMS_SENDER_ADDRESS = "tbuddy";
    private final String OTP_DELIMITER = ":"; //Message should be like " Your OTP pin is : 9DPK"
    private final int OTP_LENGTH = 4;

    //Broadcast identifiers
    public static final String BROADCAST_ORDER = "com.example.sud.trackbuddy.receivers.SmsReceiver";
    public static final String BROADCAST_OTP= "com.example.sud.trackbuddy.receivers.SmsReceiver.OTP";

    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (Object aPdusObj : pdusObj) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    String senderAddress = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();

                    Log.e(TAG, "Received SMS: " + message + ", Sender: " + senderAddress);

                    // if the SMS is not from our gateway, ignore the message
                    if (!senderAddress.toLowerCase().contains(SMS_SENDER_ADDRESS)) {
                        return;
                    }

                    identifyMessageAndBroadcast(context, message);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void identifyMessageAndBroadcast(Context context, String message) {

        if(message.contains("OTP"))
        {
            // locally (inside the application)
            LocalBroadcastManager local = LocalBroadcastManager.getInstance(context);
            // verification code from sms
            String verificationCode = getVerificationCode(message);
            if(verificationCode!=null && verificationCode.length()==OTP_LENGTH)
            {
                Log.e(TAG, "OTP received: " + verificationCode);

                Global.getInstance().OTP = verificationCode;

                Intent broadcastIntent = new Intent(SmsReceiver.BROADCAST_OTP);
                local.sendBroadcast(broadcastIntent);
            }
        }
    }

    /**
     * Getting the OTP from sms message body
     * ':' is the separator of OTP from the message
     *
     * @param message
     * @return
     */
    private String getVerificationCode(String message) {

        String code = null;
        int index = message.indexOf(OTP_DELIMITER);

        if (index != -1) {
            int start = index + 2;
            code = message.substring(start, start + OTP_LENGTH);
            return code;
        }

        return code;
    }
}
