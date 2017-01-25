package com.hh;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by MasterJ on 2017-01-26.
 */

public class SimpleSharedPresetVal extends Activity {
    private static String FileName = "setting";
    private static Context mCtx = null;

    public SimpleSharedPresetVal(Context ctx) {
        mCtx = ctx;
    }

    private SimpleSharedPresetVal() {
    }

    // 값 불러오기
    public String getPreferences(final String name) {
        SharedPreferences pref = mCtx.getSharedPreferences(FileName, MODE_PRIVATE);
        return pref.getString(name, "");
    }

    // 값 저장하기
    public void savePreferences(final String name, final String val) {
        SharedPreferences pref = mCtx.getSharedPreferences(FileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(name, val);
        editor.commit();
    }

    public void removePreferences() {
        SharedPreferences pref = mCtx.getSharedPreferences(FileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(FileName);
        editor.commit();
    }

    public void removeAllPreferences() {
        SharedPreferences pref = mCtx.getSharedPreferences(FileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

}
