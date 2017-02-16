package com.hh;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends Service {
    public static String TAG = BackgroundService.class.getSimpleName();

    // fore ground id
    private static final int DEF_FOREGROUND_ID = 10;


    // define api16 version
    public static final String VOLUME_RING = "volume_ring";
    public static final String VOLUME_SYSTEM = "volume_system";
    public static final String VOLUME_VOICE = "volume_voice";
    public static final String VOLUME_MUSIC = "volume_music";
    public static final String VOLUME_ALARM = "volume_alarm";
    public static final String VOLUME_BLUETOOTH_SCO = "volume_bluetooth_sco";
    public static final String VOLUME_NOTIFICATION = "volume_notification";
    private static final String[] DEF_VOLUME_SETTINGS = {
            VOLUME_MUSIC, VOLUME_VOICE, VOLUME_SYSTEM, VOLUME_RING,
            VOLUME_ALARM, VOLUME_NOTIFICATION, VOLUME_BLUETOOTH_SCO
    };

    AudioManager audioManager = null;
    //(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);


    private final IBinder localBinder = new BackgounrdServiceBinder();

    public class BackgounrdServiceBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    // dowork
    // member valuse
    private static Context mCtx = null;
    private static int voiceVolume = 0;
    private static boolean keepVolumeMode = false;
    private Thread keepVolumeThread = null;
    private static boolean foregroundServiceMode = false;
    private static int voiceVolumeTemp = 0;

    // load setting
    private SimpleSharedPresetVal settingData = null;

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        //throw new UnsupportedOperationException("Not yet implemented");
        return localBinder;
    }

    public boolean stopKeepVolumeWorker() {
        if (keepVolumeThread != null) {
            keepVolumeMode = false;
        }
        if (foregroundServiceMode)
            stopForeground(true);

        return true;
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public boolean startKeepVolumeWorker(boolean foreGround) {
        // load
        // load data
        settingDataControll(true);

        //  init thread worker
        keepVolumeThread = new Thread(new Runnable() {

            @Override
            public void run() {

                while (keepVolumeMode) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    keepVolumeWorker();

                }
            }

        }

        );
        keepVolumeThread.setDaemon(true);
        if (keepVolumeMode == true) {
            keepVolumeThread.start();


            if (foreGround) {
                Log.d(TAG, "Use ForegroundServiceMode");
                foregroundServiceMode = foreGround;


                startForeground(Constants.NOTIFICATION_ID.FORGROUND_SERVICE, new Notification());
            }
        }


        return true;
    }

    /*
    *   볼륨을 설정한 데로 제어한다.
    *
    * */
    public void keepVolumeWorker() {
        // get val
        int current_val = audioManager.getStreamVolume(3);
        if (voiceVolumeTemp < current_val) {
            if (keepVolumeMode) {
                Log.d(TAG, "Keep Volume : " + voiceVolumeTemp);
                audioManager.setStreamVolume(3, voiceVolumeTemp, 3);
            } else {
                voiceVolumeTemp = current_val;
                Log.d(TAG, "Keep Volume Change: " + current_val);
            }
        }
        return;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(Constants.ACTION.STARTFORGROUND_ACTION)) {

            //Toast.makeText(this, "onStart Backgroundservice", Toast.LENGTH_SHORT).show();
            //startKeepVolumeWorker(true);
            startKeepVolumeWorker(true);
        } else if (intent.getAction().equals(Constants.ACTION.STARTFORGROUND_ACTION1)) {
            //Toast.makeText(this, "onStop Backgroundservice STARTFORGROUND_ACTION1", Toast.LENGTH_SHORT).show();

        } else {
            //Toast.makeText(this, "onStop Backgroundservice", Toast.LENGTH_SHORT).show();
            stopKeepVolumeWorker();
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startid) {
        //Intent newIntent = new Intent(this, VolumeDialog.class);
        //startActivity(newIntent);
    }

    @Override
    public void onCreate() {
        mCtx = this;
        settingData = new SimpleSharedPresetVal(mCtx);
        audioManager = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);

    }

    @Override
    public void onDestroy() {
        if (foregroundServiceMode)
            stopForeground(true);
    }

    // only load data
    public void settingDataControll(boolean load) {
        if (load) {
            // keep mode
            String getKeep = settingData.getPreferences("keep");
            if (getKeep.length() > 0)
                keepVolumeMode = true;
            else
                keepVolumeMode = false;

            // keep mode
            String getVoiceVolume = settingData.getPreferences("media_volume");
            if (getVoiceVolume.length() > 0)
                voiceVolumeTemp = voiceVolume = Integer.parseInt(getVoiceVolume);

        }
    }


}
