package com.example.kundalias.statusbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Abhishek on 12/1/2016.
 */

public class NotificationService extends NotificationListenerService {
    Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        Intent msgrcvd = new Intent("msg");
        try {
            String ticker = sbn.getNotification().tickerText.toString();
            Bundle extras = sbn.getNotification().extras;
            String title = extras.getString("android.title");
            String text = extras.getString("android.text");
            //String bigtext = extras.getString(Notification.EXTRA_BIG_TEXT);

            Log.i("Package", packageName);
            Log.i("Ticker", ticker);
            Log.i("Title", title);
            Log.i("Text", text);


            msgrcvd.putExtra("package", packageName);
            msgrcvd.putExtra("ticker", ticker);
            msgrcvd.putExtra("title", title);
            msgrcvd.putExtra("text", text);
            //Broadcast message to check if the notifications are read successfully
            LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcvd);

            // msgrcvd.putExtra("bigtext", bigtext);
            // this.cancelNotification(sbn.getKey());
        } catch (NullPointerException e) {
            Log.e("Notification Service", "throws null pointer exception on reading notification", e);
        }

        //Check if notifications are read properly
        Handler h = new Handler(Looper.getMainLooper());
        switch (packageName.toLowerCase()) {
            case "com.whatsapp":
                //Let this be the code in thread from main UI thread

                h.post(new Runnable() {
                    public void run() {
                        Context context = getApplicationContext();
                        Toast.makeText(context, "whatsapp notification poppedup", Toast.LENGTH_SHORT).show();
                    }
                });
                //implement send code for whatsapp
                break;

            case "com.facebook.orca":
                //Let this be the code in thread from main UI thread
                h.post(new Runnable() {
                    public void run() {
                        Context context = getApplicationContext();
                        Toast.makeText(context, "Facebook notification poppedup", Toast.LENGTH_SHORT).show();
                    }

                });
                // implement send code to messengar
                break;
            default:
                break;
        }

    }/*
        if (!TextUtils.isEmpty(packageName) && packageName.equals("com.whatsapp")) {
         */


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("Msg", "Notification Removed");
    }

}
