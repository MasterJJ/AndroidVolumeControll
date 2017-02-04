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

        	
        	// start activity
        	
        	// call VolumeControl
        	
            
        }
    }
}

