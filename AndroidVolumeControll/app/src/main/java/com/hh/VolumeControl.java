package com.hh;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class VolumeControl extends Activity {

    private static final String TAG = "VolumeControl";
    private VolumeDialog volumeDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // if bootup mode
        Intent intent = getIntent();
        boolean bootUpMode = intent.getBooleanExtra("bootup", false);
        if (bootUpMode == true) {
            Intent startIntent;
            Context ctx = this;
            startIntent = new Intent(ctx, BackgroundService.class);
            startIntent.setAction(Constants.ACTION.STARTFORGROUND_ACTION);
            ctx.startService(startIntent);
        } else {
            showVolumeDialog();
        }

    }
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
    
    private void showVolumeDialog() {
        volumeDialog = new VolumeDialog(this);
        volumeDialog.show();
    }
    
    public void close() {
        volumeDialog.dismiss();
        finish();
    }
}