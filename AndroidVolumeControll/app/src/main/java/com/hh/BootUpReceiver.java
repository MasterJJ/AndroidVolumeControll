package com.hh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by MasterJ on 2017-01-26.
 */

public class BootUpReceiver extends BroadcastReceiver {
    public static String TAG = BootUpReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Intent i = new Intent(context, BackgroundService.class);  //MyActivity can be anything which you want to start on bootup...
            //start forground
            i.setAction(Constants.ACTION.STARTFORGROUND_ACTION);
            if (context.startService(i) == null) {
                Log.e(TAG, "Could not start service " + BackgroundService.class.getSimpleName());
            }
            Log.d(TAG, "Start BackgroundService");
            System.out.println("Start BackgroundService");
        }
    }
}

