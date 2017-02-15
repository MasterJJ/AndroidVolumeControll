package com.hh;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.hh.SimpleSharedPresetVal;

public class VolumeDialog extends Dialog implements OnClickListener, DialogInterface.OnDismissListener {
    private static String TAG = VolumeDialog.class.getSimpleName();

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

    private SeekBarVolumizer[] mSeekBarVolumizer;
    private static final int[] SEEKBAR_ID = new int[]{
            //R.id.incoming_call_volume_seekbar,
            //R.id.notification_volume_seekbar,
            R.id.Media_volume_seekbar,
            //R.id.alarm_volume_seekbar
    };
    private static final int[] SEEKBAR_TYPE = new int[]{
            AudioManager.STREAM_MUSIC,
            //AudioManager.STREAM_VOICE_CALL,
            //AudioManager.STREAM_RING,
            //AudioManager.STREAM_NOTIFICATION,
            //AudioManager.STREAM_MUSIC,
            //AudioManager.STREAM_SYSTEM,
            //AudioManager.STREAM_VOICE_CALL,
    };

    private Context mCtx = null;
    private SimpleSharedPresetVal settingData = null;
    private Context mContext;
    private Button mButtonOK;
    private Button mButtonCancel;
    private boolean mKeepVolumeMode = false;
    private Thread keepVolumeThread = null;
    private static int voiceVolumeTemp = 0;


    public VolumeDialog(Context context) {
        super(context, android.R.style.Theme_Dialog);
        mContext = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // xml ui load
        mCtx = this.getContext();
        settingData = new SimpleSharedPresetVal(this.getContext());


        requestWindowFeature(Window.ID_ANDROID_CONTENT);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);

        setContentView(R.layout.preference_dialog_ringervolume);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_settings_sound);
        setTitle(R.string.sound_settings);

        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        getWindow().setLayout(width, LayoutParams.WRAP_CONTENT);

        mButtonOK = (Button) findViewById(R.id.buttonOK);
        mButtonOK.setOnClickListener(this);
        mButtonCancel = (Button) findViewById(R.id.buttonCancel);
        mButtonCancel.setOnClickListener(this);

        mSeekBarVolumizer = new SeekBarVolumizer[SEEKBAR_ID.length];
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar) findViewById(SEEKBAR_ID[i]);
            Log.d(getClass().toString(), seekBar.toString());
            mSeekBarVolumizer[i] = new SeekBarVolumizer(mContext, seekBar,
                    SEEKBAR_TYPE[i]);
        }

        CheckBox checkBoxKeepVolume = (CheckBox) findViewById(R.id.keep_volum_checkbox);
        checkBoxKeepVolume.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mKeepVolumeMode = isChecked;
                if (isChecked) {
                    settingDataControll(false);
                    /*
                    if (backgroundService != null)
                        backgroundService.startKeepVolumeWorker(true);
                        */
                } else {
                    /*
                    if (backgroundService != null)
                        backgroundService.stopKeepVolumeWorker();
                        */
                }

            }
        });
        Log.d(getClass().toString(), checkBoxKeepVolume.toString());

        voiceVolumeTemp = mSeekBarVolumizer[0].getVolumeVal();

        // load default value
        settingDataControll(true);
        // init value

/*        //  init thread worker
        keepVolumeThread = new Thread(new Runnable() {


            @Override
            public void run() {
                while (true) {
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
        keepVolumeThread.start();*/


    }

    // load data
    public void settingDataControll(boolean load) {

        if (load) {
            // keep mode
            String getKeep = settingData.getPreferences("keep");
            CheckBox check = (CheckBox) findViewById(R.id.keep_volum_checkbox);
            if (getKeep.length() > 0)
                check.setChecked(true);
            else
                check.setChecked(false);

            // keep mode
            String getVoiceVolume = settingData.getPreferences("media_volume");
            if (getVoiceVolume.length() > 0)
                voiceVolumeTemp = Integer.parseInt(getVoiceVolume);

        } else { // save
            // keep mode
            CheckBox check = (CheckBox) findViewById(R.id.keep_volum_checkbox);
            if (check.isChecked())
                settingData.savePreferences("keep", "true");
            else
                settingData.savePreferences("keep", "");

            // keep mode
            settingData.savePreferences("media_volume", String.valueOf(voiceVolumeTemp));
        }
    }

/*
    public void keepVolumeWorker() {
        // get val
        int current_val = mSeekBarVolumizer[0].getVolumeVal();
        if (voiceVolumeTemp < current_val) {
            if (mKeepVolumeMode) {
                Log.d(TAG, "Keep Volume : " + voiceVolumeTemp);
                mSeekBarVolumizer[0].postSetVolumeEx(voiceVolumeTemp);
            } else {
                voiceVolumeTemp = current_val;
                Log.d(TAG, "Keep Volume Change: " + current_val);
            }
        }

        return;
    }*/

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.buttonOK:
                //mCtx.unbindService(serviceConnect);
                voiceVolumeTemp = mSeekBarVolumizer[0].getVolumeVal();
                settingDataControll(false);
                closeVolumes();

                if (mKeepVolumeMode) {
                    //start forground
                    Intent startIntent;
                    startIntent = new Intent(mCtx, BackgroundService.class);
                    startIntent.setAction(Constants.ACTION.STARTFORGROUND_ACTION);
                    mCtx.startService(startIntent);
                } else {
                    Intent startIntent;
                    startIntent = new Intent(mCtx, BackgroundService.class);
                    startIntent.setAction(Constants.ACTION.STOPFORGROUND_ACTION);
                    mCtx.startService(startIntent);

                }

                ((VolumeControl) mContext).close();

                break;
            case R.id.buttonCancel:
                //mCtx.unbindService(serviceConnect);
                revertVolume();
                closeVolumes();
                ((VolumeControl) mContext).close();


                break;
        }
    }

    @Override
    public void onBackPressed() {
        revertVolume();
        closeVolumes();
        ((VolumeControl) mContext).close();
    }

    private void revertVolume() {
        for (SeekBarVolumizer vol : mSeekBarVolumizer) {
            vol.revertVolume();
        }
    }

    private void closeVolumes() {
        for (SeekBarVolumizer vol : mSeekBarVolumizer) {
            vol.stop();
        }
    }

    protected void onSampleStarting(SeekBarVolumizer volumizer) {
        for (SeekBarVolumizer vol : mSeekBarVolumizer) {
            if (vol != null && vol != volumizer) vol.stopSample();
        }
    }

    public class SeekBarVolumizer implements OnSeekBarChangeListener, Runnable {

        private Context mContext;
        private Handler mHandler = new Handler();

        private AudioManager mAudioManager;
        private int mStreamType;
        private int mOriginalStreamVolume;
        private Ringtone mRingtone;

        private int mLastProgress = -1;
        private SeekBar mSeekBar;

        private ContentObserver mVolumeObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (mSeekBar != null) {
                    int volume = Settings.System.getInt(mContext.getContentResolver(),
                            DEF_VOLUME_SETTINGS[mStreamType], -1);
                    if (volume >= 0) {
                        mSeekBar.setProgress(volume);
                        voiceVolumeTemp = volume;
                    }
                }
            }
        };

        public SeekBarVolumizer(Context context, SeekBar seekBar, int streamType) {
            mContext = context;
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            mStreamType = streamType;
            mSeekBar = seekBar;

            initSeekBar(seekBar);
        }


        @SuppressLint("NewApi")
        private void initSeekBar(SeekBar seekBar) {
            seekBar.setMax(mAudioManager.getStreamMaxVolume(mStreamType));
            mOriginalStreamVolume = mAudioManager.getStreamVolume(mStreamType);
            seekBar.setProgress(mOriginalStreamVolume);
            seekBar.setOnSeekBarChangeListener(this);

            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(DEF_VOLUME_SETTINGS[mStreamType]),
                    false, mVolumeObserver);

            Uri defaultUri = null;

            if (mStreamType == AudioManager.STREAM_VOICE_CALL) {
                defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            } else if (mStreamType == AudioManager.STREAM_RING) {
                defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
            } else if (mStreamType == AudioManager.STREAM_NOTIFICATION) {
                defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            } else if (mStreamType == AudioManager.STREAM_MUSIC) {
                defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            } else {
                defaultUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
            }

            mRingtone = RingtoneManager.getRingtone(mContext, defaultUri);
            if (mRingtone != null) {
                mRingtone.setStreamType(mStreamType);
            }
        }

        public void stop() {
            stopSample();
            mContext.getContentResolver().unregisterContentObserver(mVolumeObserver);
            mSeekBar.setOnSeekBarChangeListener(null);
        }

        public void revertVolume() {
            mAudioManager.setStreamVolume(mStreamType, mOriginalStreamVolume, 3);
        }

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromTouch) {
            if (!fromTouch) {
                return;
            }

            postSetVolume(progress);
        }

        void postSetVolume(int progress) {
            mLastProgress = progress;
            mHandler.removeCallbacks(this);
            mHandler.post(this);
        }


        void postSetVolumeEx(int progress) {
            mLastProgress = progress;
            mHandler.removeCallbacks(this);
            mHandler.post(this);
            mSeekBar.setProgress(progress);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mRingtone != null && !mRingtone.isPlaying()) {
                sample();
            }
        }

        public void run() {
            Log.d(TAG, "Set setStreamVolume");
            mAudioManager.setStreamVolume(mStreamType, mLastProgress, 3);
        }

        private void sample() {
            onSampleStarting(this);
            mRingtone.play();
        }

        public void stopSample() {
            if (mRingtone != null) {
                mRingtone.stop();
            }
        }

        public SeekBar getSeekBar() {
            return mSeekBar;
        }


        //  get current volume
        public int getVolumeVal() {
            return mAudioManager.getStreamVolume(mStreamType);
        }

        public void changeVolumeBy(int amount) {
            mSeekBar.incrementProgressBy(amount);
            if (mRingtone != null && !mRingtone.isPlaying()) {
                sample();
            }
            postSetVolume(mSeekBar.getProgress());
        }

        public void onSaveInstanceState(VolumeStore volumeStore) {
            if (mLastProgress >= 0) {
                volumeStore.volume = mLastProgress;
                volumeStore.originalVolume = mOriginalStreamVolume;
            }
        }

        public void onRestoreInstanceState(VolumeStore volumeStore) {
            if (volumeStore.volume != -1) {
                mOriginalStreamVolume = volumeStore.originalVolume;
                mLastProgress = volumeStore.volume;
                postSetVolume(mLastProgress);
            }
        }
    }

    public static class VolumeStore {
        public int volume = -1;
        public int originalVolume = -1;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }

}
