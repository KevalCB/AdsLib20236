package com.ads.mylibrary2023;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;


public class PreferenceManager extends Application {

    static SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;

    private static PreferenceManager applicationInstance;

    public static int width, height;

    public static synchronized PreferenceManager getInstance() {
        return applicationInstance;
    }

    public static final String CHANNEL_ID = "channe";


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

    }

    @Override
    public void onCreate() {
        super.onCreate();

        applicationInstance = this;

        sharedPreferences = getSharedPreferences(getString(R.string.app_name),
                Context.MODE_PRIVATE);

        editor = sharedPreferences.edit();

        SharedPreferences mPrefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this);

        createNotificationChannels();

    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

}