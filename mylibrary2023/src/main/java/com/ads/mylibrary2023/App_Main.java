package com.ads.mylibrary2023;

import android.content.SharedPreferences;

public class App_Main extends PreferenceManager {
    
    public static SharedPreferences sharedPreferences;

    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(getPackageName(), 0);
        new AppOpenManager(this);

    }
}
