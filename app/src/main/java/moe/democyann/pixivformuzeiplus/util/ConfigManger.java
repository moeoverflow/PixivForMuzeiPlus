package moe.democyann.pixivformuzeiplus.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import moe.democyann.pixivformuzeiplus.R;

/**
 * Created by demo on 4/3/17.
 */

public class ConfigManger {

    private Context context;
    private SharedPreferences preferences;
    private static final String TAG = "ConfigManager";

    public ConfigManger(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getPassword() {

        String defaultValue = "",
                v = preferences.getString("password", defaultValue);
        return v;
    }

    public String getUsername() {
        String defaultValue = "",
                v = preferences.getString("pixivid", defaultValue);
        return v;
    }

    public boolean isOnlyUpdateOnWifi() {
        boolean defaultValue = false,
                v = preferences.getBoolean("only_wifi", defaultValue);
        Log.d(TAG, "pref_onlyWifi = " + v);
        return v;
    }

    public String getTage() {
        String defaultValue = "",
                v = preferences.getString("tags", defaultValue);
        return v;
    }

    public long getView() {
        String defaultValue = "0",
                s = preferences.getString("views", defaultValue);
        long v = 0;
        try {
            v = Long.valueOf(s);
        } catch (Exception e) {
            Log.e(TAG, "getViews: ", e);
        }
        if (v > 50000) v = 50000;
        return v;
    }

    public boolean getIs_no_R18() {
        boolean defaultValue = true,
                v = preferences.getBoolean("is_no_r18", defaultValue);
        return v;
    }

    public int getChangeInterval() {

        final String defaultValue = context.getString(R.string.time_default),
                s = preferences.getString("time_change", defaultValue);
        Log.d(TAG, "time_change = \"" + s + "\"");
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Log.w(TAG, e.toString(), e);
            return 0;
        }
    }

    public int getMethod() {
        final String defaultValue = context.getString(R.string.method_default),
                s = preferences.getString("method", defaultValue);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Log.w(TAG, e.toString(), e);
            return 0;
        }
    }

    public boolean getIs_check_Tag() {
        boolean defaultValue = false,
                v = preferences.getBoolean("is_tag", defaultValue);
        return v;
    }
    public boolean getIs_autopx() {
        boolean defaultValue = false,
                v = preferences.getBoolean("is_autopx", defaultValue);
        return v;
    }


    public String listToString(List list) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i)).append(',');
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }
    public List stringToList(String str) {
        ArrayList list= new ArrayList();
        if(!"".equals(str) && str.length()>100) {
            String arr[] = str.split(",");
            for (String a : arr) {
                list.add(a);
            }
        }
        return list;
    }
}
