package com.ads.mylibrary2023;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

public class AppOpenManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private static final String TAG = "AppOpenManager";
    public static boolean doNotDisplayAds = false;
    private static boolean isShowingAd = false;
    public Ads_Preference adsPreference;
    private AppOpenAd appOpenAd = null;
    private Activity currentActivity;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private final App_Main myApplication;

    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    public void onActivityPaused(Activity activity) {
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    public void onActivityStopped(Activity activity) {
    }

    public AppOpenManager(App_Main appMain) {
        this.myApplication = appMain;
        appMain.registerActivityLifecycleCallbacks(this);
        this.adsPreference = new Ads_Preference(appMain);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public void fetchAd() {
        if (!isAdAvailable()) {
            Log.d(TAG, "fetchAd: ");
            this.loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                public void onAdLoaded(AppOpenAd appOpenAd) {
                    Log.d(AppOpenManager.TAG, "onAdLoaded: ");
                    AppOpenManager.this.appOpenAd = appOpenAd;
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    String str = AppOpenManager.TAG;
                    Log.d(str, "onAdFailedToLoad: " + loadAdError.getMessage());
                }
            };
            AppOpenAd.load(this.myApplication, this.adsPreference.getAdmAppOpenID(), getAdRequest(), 1, this.loadCallback);
        }
    }

    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public boolean isAdAvailable() {
        return this.appOpenAd != null;
    }

    public void showAdIfAvailable() {
        String str = TAG;
        Log.d(str, "showAdIfAvailable: ");
        if (isShowingAd || !isAdAvailable()) {
            Log.d(str, "Can not show ad.");
            fetchAd();
            return;
        }
        Log.d(str, "Will show ad.");
        this.appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                AppOpenManager.this.appOpenAd = null;
                boolean unused = AppOpenManager.isShowingAd = false;
                AppOpenManager.this.fetchAd();
            }

            @Override // com.google.android.gms.ads.FullScreenContentCallback
            public void onAdShowedFullScreenContent() {
                boolean unused = AppOpenManager.isShowingAd = true;
            }
        });
        Activity activity = this.currentActivity;
//        if (!(activity instanceof SplashActivity)) {
                this.appOpenAd.show(activity);
//        }

    }

    public void onActivityStarted(Activity activity) {
        this.currentActivity = activity;
    }

    public void onActivityResumed(Activity activity) {
        this.currentActivity = activity;
    }

    public void onActivityDestroyed(Activity activity) {
        this.currentActivity = null;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        if (!doNotDisplayAds) {
            showAdIfAvailable();
        }
        Log.d(TAG, "onStart");
    }
}
