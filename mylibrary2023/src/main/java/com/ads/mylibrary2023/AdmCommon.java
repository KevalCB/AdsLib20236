package com.ads.mylibrary2023;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.facebook.ads.Ad;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.MediaViewListener;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.firebase.messaging.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AdmCommon {
    private static final String TAG = "AdmCommon";
    public static AdmCommon mInstance;
    private AdsCallBack adsCallingBack;
    public boolean isLoading = false;
    private static com.facebook.ads.InterstitialAd fb_interstitial;
    public static InterstitialAd mInterstitialAd;
    public static int mInterstitialAdClickCount = 0;
    public static int mInterstitialAdBackPressClickCount = 0;

    private static Dialog dialog;
    private static ProgressDialog pdialog;
    public ImageView imageViewBig, imageviewSmall;
    public NativeAdLayout nativeAdLayout;
    public com.facebook.ads.MediaView nativeAdMedia;
    boolean fail = false;
    public static int interstitialGapNumber;
    public static int interstitialBackpressGapNumber;
    public static boolean isAdsFail = false;
    public static String app_name;
    public static String short_description;
    public static String app_desc_title;
    public static String app_sub_desc;
    public static String app_icon;
    public static String app_banner;
    public static String app_link;
    public static String app_rate;
    public static String app_review;
    public static String app_sponsored;
    public boolean live = false;
    public static int i = 1;
    public static int i_back = 1;
    public Dialog LoadingAds;
    public static boolean isFromSplash = false;
    int timeCount;
    public static int ratingDialogCounter = 20;


    public static AdmCommon getInstance() {
        if (mInstance == null) {
            mInstance = new AdmCommon();
        }
        return mInstance;
    }



    public void loadOrShowAdmInterstial(boolean isFrom, final Activity activity, AdsCallBack adsCallingBack2) {


        if (!new AdmCommon().isOnline(activity)) {//check internet connection if false
            new AdmCommon().NoConnectionDialog(isFrom, activity, adsCallingBack2);//show alert dialog

        } else {//if connect internet true

            if (ratingDialogCounter == AdmCommon.getInstance().mInterstitialAdClickCount) {
                ratingAppDialog(activity, adsCallingBack2);//Display Rating Dialog
                ratingDialogCounter = ratingDialogCounter + 20;

            } else {
                //display load ad dialog
                LoadingAds = new Dialog(activity);
                LoadingAds.setContentView(R.layout.ad_dialog);
                LoadingAds.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                LoadingAds.setCanceledOnTouchOutside(false);
                LoadingAds.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                //this condition for if all parameter are "false" but intent possible in app
                if (new Ads_Preference(activity).get_Custom_Inter() == false && new Ads_Preference(activity).getQurekaEnable() == false && new Ads_Preference(activity).getinterstitial_enable() == false) {
                    if (this.isLoading || !isOnline(activity)) {
                        AdsCallBack adsCallingBack3 = this.adsCallingBack;
                        if (adsCallingBack3 != null) {
                            adsCallingBack3.onLoading();

                        }
                    }
                }

                interstitialGapNumber = Integer.parseInt(new Ads_Preference(activity).getInterstitialClickGap());
                interstitialBackpressGapNumber = Integer.parseInt(new Ads_Preference(activity).getInterstitialBackPressClickGap());

                this.adsCallingBack = adsCallingBack2;
                AdsCallBack adsCallingBack4 = this.adsCallingBack;

                if (new Ads_Preference(activity).get_priority().equalsIgnoreCase("AM")) {//if priority AM
                    if (isFrom) {//check if isFrom true = ad for Intent
                        // if false = ad for onBackPressed

                        //if mInterstitialAdClickCount = 0 and i = 1 , interstitialGapNumber = 2
                        //enter in if condition and increase i value that indicate after gap next display ad position
                        //on click mInterstitialAdClickCount increase and now it's value is 1
                        //(mInterstitialAdClickCount = i)(condition match!)
                        //display AM ad
                        //  i = i + interstitialGapNumber + 1
                        //  i =  1 + 2 + 1 ---->  i = 4
                        // so in 2nd click not display  , 3rd click not display
                        // on 4th click (4 == 4) (condition match!) display ad and increase i value and get next ad display position

                        if (AdmCommon.getInstance().mInterstitialAdClickCount == i) {//check condition
                            if (adsCallingBack2.toString().contains("SplashActivity")) {//condition for after splash on first click display ad
                                i = 1;
                                mInterstitialAdClickCount = 0;
                            } else {
                                i = i + interstitialGapNumber + 1;
                            }


                            if (this.isLoading || !isOnline(activity)) {
                                AdsCallBack adsCallingBack3 = this.adsCallingBack;
                                if (adsCallingBack3 != null) {
                                    adsCallingBack3.onLoading();

                                }
                            } else if (this.mInterstitialAd == null) {//if AM interstitial response is null

                                AM_Interstitial_Load(activity, adsCallingBack4); //get AM interstitial ad response and display AM ad

                            }

                        } else { //condition not match
                            if (new Ads_Preference(activity).getInterstitialGapEnable() == true) {//condition for display ad in Interstitial ad gap if value is true
                                qurekaOrCustomInterstitial(activity, adsCallingBack4);//display custom or qureka interstitial
                                AdmCommon.this.isLoading = false;

                            } else {
                                adsCallingBack4.onLoading();//if false intent to next activity
                            }

                        }
                    } else {
                        if (new Ads_Preference(activity).getInterstitialEnableBackPress()) { //for display interstitial on onBackPressed if value true

                            if (AdmCommon.getInstance().mInterstitialAdBackPressClickCount == i_back) {//check condition
                                i_back = i_back + interstitialBackpressGapNumber + 1;

                                if (this.isLoading || !isOnline(activity)) {
                                    AdsCallBack adsCallingBack3 = this.adsCallingBack;
                                    if (adsCallingBack3 != null) {
                                        adsCallingBack3.onLoading();
                                    }
                                } else if (this.mInterstitialAd == null) {//if AM interstitial response is null
                                    AM_Interstitial_Load(activity, adsCallingBack4); // get AM interstitial ad response and display AM ad
                                }

                            } else { //condition not match
                                if (new Ads_Preference(activity).getInterstitialGapEnable() == true) {//condition for display ad in Interstitial ad gap if value is true
                                    qurekaOrCustomInterstitial(activity, adsCallingBack4);//display custom or qureka interstitial
                                    AdmCommon.this.isLoading = false;

                                } else {
                                    if (LoadingAds.isShowing()) {
                                        LoadingAds.dismiss();
                                    }
                                    adsCallingBack4.onLoading();//if false go to previous activity
                                }

                            }
                        } else {
                            if (LoadingAds.isShowing()) {
                                LoadingAds.dismiss();
                            }
                            adsCallingBack4.onLoading();//if false go to previous activity
                        }

                    }


                } else if (new Ads_Preference(activity).get_priority().equalsIgnoreCase("FB")) {
                    if (isFrom) {//for FB interstitial
                        // check if isFrom true = ad for Intent
                        // if false = ad for onBackPressed


                        if (AdmCommon.getInstance().mInterstitialAdClickCount == i) {  //for Intent interstitial
                            if (adsCallingBack2.toString().contains("SplashActivity")) {//condition for after splash on first click display ad
                                i = 1;
                                mInterstitialAdClickCount = 0;
                            } else {
                                i = i + interstitialGapNumber + 1;
                            }

                            if (this.isLoading || !isOnline(activity)) {
                                AdsCallBack adsCallingBack3 = this.adsCallingBack;
                                if (adsCallingBack3 != null) {
                                    adsCallingBack3.onLoading();
                                }
                            } else if (fb_interstitial == null) {//if FB interstitial response is null
                                FB_Interstitial_Load(activity, adsCallingBack4);//get FB interstitial ad response and display FB ad

                            }

                        } else {
                            if (new Ads_Preference(activity).getInterstitialGapEnable() == true) {//condition for display ad in Interstitial ad gap
                                qurekaOrCustomInterstitial(activity, adsCallingBack4);//display custom or qureka interstitial
                                AdmCommon.this.isLoading = false;

                            } else {

                                adsCallingBack4.onLoading();//if false intent to next activity
                            }
                        }
                    } else {

                        if (new Ads_Preference(activity).getInterstitialEnableBackPress()) { //for display interstitial on onBackPressed if value true

                            if (AdmCommon.getInstance().mInterstitialAdBackPressClickCount == i_back) { //check condition
                                i_back = i_back + interstitialBackpressGapNumber + 1;

                                if (this.isLoading || !isOnline(activity)) {
                                    AdsCallBack adsCallingBack3 = this.adsCallingBack;
                                    if (adsCallingBack3 != null) {
                                        adsCallingBack3.onLoading();
                                    }
                                } else if (fb_interstitial == null) {//if FB interstitial response is null
                                    FB_Interstitial_Load(activity, adsCallingBack4);// get FB interstitial ad response and display FB ad

                                }

                            } else {
                                if (new Ads_Preference(activity).getInterstitialGapEnable() == true) {//condition for display ad in Interstitial ad gap if value is true
                                    qurekaOrCustomInterstitial(activity, adsCallingBack4);//display custom or qureka interstitial
                                    AdmCommon.this.isLoading = false;

                                } else {
                                    if (LoadingAds.isShowing()) {
                                        LoadingAds.dismiss();
                                    }
                                    adsCallingBack4.onLoading();//if false go to previous activity
                                }

                            }
                        } else {
                            if (LoadingAds.isShowing()) {
                                LoadingAds.dismiss();
                            }
                            adsCallingBack4.onLoading();//if false go to previous activity
                        }

                    }


                } else if (new Ads_Preference(activity).get_priority().equalsIgnoreCase("")
                        || !new Ads_Preference(activity).get_priority().equalsIgnoreCase("FB")
                        || !new Ads_Preference(activity).get_priority().equalsIgnoreCase("AM")) {//check if priority value is not FB and AM
                    if (isFrom) {// for interstitial
                        // check if isFrom true = ad for Intent
                        // if false = ad for onBackPressed

                        if (AdmCommon.getInstance().mInterstitialAdClickCount == i) {  //for Intent interstitial
                            if (adsCallingBack2.toString().contains("SplashActivity")) {//condition for after splash on first click display ad
                                i = 1;
                                mInterstitialAdClickCount = 0;

                            } else {
                                i = i + interstitialGapNumber + 1;
                            }
                            qurekaOrCustomInterstitial(activity, adsCallingBack4);//display custom or qureka interstitial
                            AdmCommon.this.isLoading = false;
                        } else {
                            if (new Ads_Preference(activity).getInterstitialGapEnable() == true) {//condition for display ad in Interstitial ad gap
                                qurekaOrCustomInterstitial(activity, adsCallingBack4);//display custom or qureka interstitial
                                AdmCommon.this.isLoading = false;

                            } else {
                                adsCallingBack4.onLoading();//if false intent to next activity
                            }
                        }
                    } else {
                        if (new Ads_Preference(activity).getInterstitialEnableBackPress()) {//for interstitial onBackPressed
                            if (AdmCommon.getInstance().mInterstitialAdBackPressClickCount == i_back) {
                                i_back = i_back + interstitialBackpressGapNumber;

                                qurekaOrCustomInterstitial(activity, adsCallingBack4);//display custom or qureka interstitial
                                AdmCommon.this.isLoading = false;

                            } else {
                                if (new Ads_Preference(activity).getInterstitialGapEnable() == true) {//condition for display ad in Interstitial ad gap
                                    qurekaOrCustomInterstitial(activity, adsCallingBack4);//display custom or qureka interstitial
                                    AdmCommon.this.isLoading = false;

                                } else {
                                    if (LoadingAds.isShowing()) {
                                        LoadingAds.dismiss();
                                    }

                                    adsCallingBack4.onLoading();//if false go to previous activity
                                }
                            }

                        } else {
                            if (LoadingAds.isShowing()) {
                                LoadingAds.dismiss();
                            }
                            adsCallingBack4.onLoading();//if false go to previous activity
                        }
                    }
                }
            }
        }


    }


    private void FB_Interstitial_Load(Activity activity, AdsCallBack adsCallingBack4) {

        if (new Ads_Preference(activity).getinterstitial_enable() && new Ads_Preference(activity).getFbInterstitialID() != null) {
            LoadingAds.show();
            AdmCommon.this.isLoading = true;
            fb_interstitial = new com.facebook.ads.InterstitialAd(activity, new Ads_Preference(activity).getFbInterstitialID());
            fb_interstitial.loadAd(
                    fb_interstitial.buildLoadAdConfig()
                            .withAdListener(new InterstitialAdListener() {
                                @Override
                                public void onInterstitialDisplayed(Ad ad) {

                                }

                                @Override
                                public void onInterstitialDismissed(Ad ad) {
                                    AppOpenManager.doNotDisplayAds = false;
                                    fb_interstitial = null;
                                    if (AdmCommon.this.adsCallingBack != null) {
                                        AdmCommon.this.adsCallingBack.onAdsClose();//close FB ad
                                    }
                                }

                                @Override
                                public void onError(Ad ad, com.facebook.ads.AdError adError) {
                                    fb_interstitial = null;
                                    AdmCommon.this.isLoading = false;
                                    qurekaOrCustomInterstitial(activity, adsCallingBack4);//show custom or qureka interstitial

                                }

                                @Override
                                public void onAdLoaded(Ad ad) {
                                    AppOpenManager.doNotDisplayAds = true;
                                    LoadingAds.show();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (LoadingAds.isShowing()) {
                                                LoadingAds.dismiss();
                                                fb_interstitial.show(); //FB interstitial show
                                                fb_interstitial = null;
                                            }
                                        }

                                    }, 2000);

                                    AdmCommon.this.isLoading = false;
                                }

                                @Override
                                public void onAdClicked(Ad ad) {

                                }

                                @Override
                                public void onLoggingImpression(Ad ad) {

                                }
                            })
                            .build());
        } else {//condition not match
            AdmCommon.this.isLoading = false;
            qurekaOrCustomInterstitial(activity, adsCallingBack4);//display custom or qureka interstitial
        }


    }

    private void AM_Interstitial_Load(Activity activity, AdsCallBack adsCallingBack4) {

        if (new Ads_Preference(activity).getinterstitial_enable() && new Ads_Preference(activity).getAdmInterstitialID() != null) {//if true display interstitial ad of AM
            LoadingAds.show();
            if (mInterstitialAd == null) {
                this.isLoading = true;
                InterstitialAd.load(activity, new Ads_Preference(activity).getAdmInterstitialID(), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                    public void onAdLoaded(InterstitialAd interstitialAd) {

                        LoadingAds.show();

                        AdmCommon.this.isLoading = false;
                        AdmCommon.this.mInterstitialAd = interstitialAd;
                        Log.e("TAG", "onAdLoaded: " + AdmCommon.this.mInterstitialAd);
                        AM_Interstitial_Load(activity, adsCallingBack4);
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {//fail FB interstitial ad
                        qurekaOrCustomInterstitial(activity, adsCallingBack4);
                        AdmCommon.this.isLoading = false;
                        AdmCommon.this.mInterstitialAd = null;

                    }
                });

            } else {
                if (mInterstitialAd != null) {
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            AppOpenManager.doNotDisplayAds = false;
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            AppOpenManager.doNotDisplayAds = false;
                            AdmCommon.this.mInterstitialAd = null;

                            if (AdmCommon.this.adsCallingBack != null) {
                                AdmCommon.this.adsCallingBack.onAdsClose();//close FB interstitial ad

                            }

                        }
                    });
                    LoadingAds.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (LoadingAds.isShowing()) {
                                LoadingAds.dismiss();
                            }
                            mInterstitialAd.show(activity);//show FB interstitial ad
                            mInterstitialAd = null;
                        }

                    }, 3000);

                }
            }
        } else {
            AdmCommon.this.isLoading = false;
            qurekaOrCustomInterstitial(activity, adsCallingBack4);
        }

    }

    private static class GetContacts extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = "http://mojilugujarat.com/CB/AppList.json";
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray contacts = jsonObj.getJSONArray("data");
                    int random_computer_card = new Random().nextInt(contacts.length());
                    JSONObject jo_inside = contacts.getJSONObject(random_computer_card);
                    app_name = jo_inside.getString("app_name");
                    short_description = jo_inside.getString("short_description");
                    app_desc_title = jo_inside.getString("app_desc_title");
                    app_sub_desc = jo_inside.getString("app_sub_desc");
                    app_icon = jo_inside.getString("app_icon");
                    app_banner = jo_inside.getString("app_banner");
                    app_review = jo_inside.getString("app_review");
                    app_link = jo_inside.getString("app_link");
                    app_rate = jo_inside.getString("app_rate");
                    app_sponsored = jo_inside.getString("app_sponsored");

                } catch (final JSONException e) {

                }

            } else {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    @SuppressLint("WrongConstant")
    public void AmNativeLoad(final Activity activity, final ViewGroup viewGroup,
                             final boolean z) {

        if (isOnline(activity)) {

            if (new Ads_Preference(activity).getnative_ads_enable()) {//check if true
                if (new Ads_Preference(activity).get_priority().equalsIgnoreCase("AM")) {//check if priority is AM
                    viewGroup.findViewById(R.id.rlLoading).setVisibility(0);
                    String admNativeID = new Ads_Preference(activity).getAdmNativeID();
                    String str = TAG;
                    AdLoader.Builder builder = new AdLoader.Builder(activity, admNativeID);
                    builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(NativeAd nativeAd) {//load AM native
                            viewGroup.findViewById(R.id.rlLoading).setVisibility(8);
                            AdmCommon.this.populateUnifiedNativeAdView(nativeAd, (NativeAdView) viewGroup.findViewById(R.id.nativeAdView), z);
                        }
                    });
                    builder.withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {//error in load native
                            viewGroup.findViewById(R.id.rlLoading).setVisibility(8);
                            if (new Ads_Preference(activity).getnative_qureka_enable()) {//if true
                                loadQureka(activity, viewGroup, z);//display qureka native
                                viewGroup.findViewById(R.id.cardQureka).setVisibility(0);
                            } else {
                                viewGroup.findViewById(R.id.cardQureka).setVisibility(8);

                            }
                            viewGroup.findViewById(R.id.nativeAdView).setVisibility(8);


                        }
                    }).build().loadAd(new AdRequest.Builder().build());

                } else if (new Ads_Preference(activity).get_priority().equalsIgnoreCase("FB")) {//check if priority is FB
                    viewGroup.findViewById(R.id.rlLoading).setVisibility(0);
                    try {
                        final com.facebook.ads.NativeAd nativeAd = new com.facebook.ads.NativeAd(activity, new Ads_Preference(activity).getFbNativeID());
                        NativeAdListener nativeAdListener = new NativeAdListener() {
                            @Override
                            public void onMediaDownloaded(Ad ad) {
                            }

                            @SuppressLint("MissingPermission")
                            @Override
                            public void onError(Ad ad, com.facebook.ads.AdError adError) {//error in load FB ad
                                viewGroup.findViewById(R.id.rlLoading).setVisibility(8);
                                if (new Ads_Preference(activity).getnative_qureka_enable()) {
                                    loadQureka(activity, viewGroup, z);
                                    viewGroup.findViewById(R.id.cardQureka).setVisibility(0);
                                } else {
                                    viewGroup.findViewById(R.id.cardQureka).setVisibility(8);

                                }
                                viewGroup.findViewById(R.id.nativeAdView).setVisibility(8);

                            }

                            @Override
                            public void onAdLoaded(Ad ad) {//load Fb native
                                viewGroup.findViewById(R.id.rlLoading).setVisibility(8);
                                viewGroup.findViewById(R.id.cardQureka).setVisibility(8);
                                nativeAdLayout = (NativeAdLayout) activity.getLayoutInflater()
                                        .inflate(R.layout.ad_fb_big_native, null);
                                inflateAd(nativeAd, nativeAdLayout, z);
                                viewGroup.removeAllViews();
                                viewGroup.addView(nativeAdLayout);
                            }

                            @Override
                            public void onAdClicked(Ad ad) {
                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {
                            }
                        };
                        nativeAd.loadAd(
                                nativeAd.buildLoadAdConfig()
                                        .withAdListener(nativeAdListener)
                                        .build());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (new Ads_Preference(activity).get_priority().equalsIgnoreCase("")
                        || !new Ads_Preference(activity).get_priority().equalsIgnoreCase("FB")
                        || !new Ads_Preference(activity).get_priority().equalsIgnoreCase("AM")) {//if priority is not FB and AM
                    loadQureka(activity, viewGroup, z);//display native qureka
                    viewGroup.findViewById(R.id.cardQureka).setVisibility(0);
                }
                return;
            } else if (new Ads_Preference(activity).getnative_qureka_enable()) {//if true load qureka native
                loadQureka(activity, viewGroup, z);//display native qureka
                viewGroup.findViewById(R.id.cardQureka).setVisibility(0);
            }
        } else {
//            NoConnectionDialog(activity);
        }

        viewGroup.findViewById(R.id.rlLoading).setVisibility(8);
        viewGroup.findViewById(R.id.nativeAdView).setVisibility(8);
    }

    @SuppressLint("WrongConstant")
    public void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView nativeAdView,
                                            boolean z) {
        if (nativeAdView != null) {
            if (nativeAdView.findViewById(R.id.ad_media) != null) {
                MediaView mediaView = (MediaView) nativeAdView.findViewById(R.id.ad_media);
                nativeAdView.setMediaView(mediaView);
                mediaView.setVisibility(0);
            }
            if (nativeAdView.findViewById(R.id.ad_headline) != null) {
                nativeAdView.setHeadlineView(nativeAdView.findViewById(R.id.ad_headline));
                View headlineView = nativeAdView.getHeadlineView();
                if (headlineView != null) {
                    ((TextView) headlineView).setText(nativeAd.getHeadline());
                }
            }
            if (nativeAdView.findViewById(R.id.ad_body) != null) {
                nativeAdView.setBodyView(nativeAdView.findViewById(R.id.ad_body));
                View bodyView = nativeAdView.getBodyView();
                if (nativeAd.getBody() == null) {
                    if (bodyView != null) {
                        bodyView.setVisibility(4);
                    }
                } else if (bodyView != null) {
                    bodyView.setVisibility(0);
                    ((TextView) bodyView).setText(nativeAd.getBody());
                }
            }
            if (nativeAdView.findViewById(R.id.ad_call_to_action) != null) {
                nativeAdView.setCallToActionView(nativeAdView.findViewById(R.id.ad_call_to_action));
                View callToActionView = nativeAdView.getCallToActionView();
                if (nativeAd.getCallToAction() == null) {
                    if (callToActionView != null) {
                        callToActionView.setVisibility(4);
                    }
                } else if (callToActionView != null) {
                    callToActionView.setVisibility(0);
                    ((Button) callToActionView).setText(nativeAd.getCallToAction());
                }
            }
            if (nativeAdView.findViewById(R.id.ad_app_icon) != null) {
                nativeAdView.setIconView(nativeAdView.findViewById(R.id.ad_app_icon));
                View iconView = nativeAdView.getIconView();
                if (nativeAd.getIcon() == null) {
                    if (iconView != null) {
                        iconView.setVisibility(8);
                    }
                } else if (iconView != null) {
                    iconView.setVisibility(0);
                    ((ImageView) iconView).setImageDrawable(nativeAd.getIcon().getDrawable());
                }
            }
            if (nativeAdView.findViewById(R.id.ad_price) != null) {
                nativeAdView.setPriceView(nativeAdView.findViewById(R.id.ad_price));
                View priceView = nativeAdView.getPriceView();
                if (nativeAd.getPrice() == null) {
                    if (priceView != null) {
                        priceView.setVisibility(4);
                    }
                } else if (priceView != null) {
                    priceView.setVisibility(0);
                    ((TextView) priceView).setText(nativeAd.getPrice());
                }
            }
            if (nativeAdView.findViewById(R.id.ad_stars) != null) {
                nativeAdView.setStarRatingView(nativeAdView.findViewById(R.id.ad_stars));
                View starRatingView = nativeAdView.getStarRatingView();
                if (nativeAd.getStarRating() == null) {
                    if (starRatingView != null) {
                        starRatingView.setVisibility(4);
                    }
                } else if (starRatingView != null) {
                    starRatingView.setVisibility(0);
                    ((RatingBar) starRatingView).setRating(Float.parseFloat(String.valueOf(nativeAd.getStarRating().doubleValue())));
                }
            }
            if (nativeAdView.findViewById(R.id.ad_store) != null) {
                nativeAdView.setStoreView(nativeAdView.findViewById(R.id.ad_store));
                View storeView = nativeAdView.getStoreView();
                if (nativeAd.getStore() == null) {
                    if (storeView != null) {
                        storeView.setVisibility(4);
                    }
                } else if (storeView != null) {
                    storeView.setVisibility(0);
                    ((TextView) storeView).setText(nativeAd.getStore());
                }
            }
            if (nativeAdView.findViewById(R.id.ad_advertiser) != null) {
                nativeAdView.setAdvertiserView(nativeAdView.findViewById(R.id.ad_advertiser));
                View advertiserView = nativeAdView.getAdvertiserView();
                if (nativeAd.getAdvertiser() == null) {
                    if (advertiserView != null) {
                        advertiserView.setVisibility(4);
                    }
                } else if (advertiserView != null) {
                    advertiserView.setVisibility(0);
                    ((TextView) advertiserView).setText(nativeAd.getAdvertiser());
                }
            }
            if (nativeAdView.getHeadlineView() != null) {
                nativeAdView.setNativeAd(nativeAd);
            }
            nativeAdView.setVisibility(0);
        }
    }

    public void inflateAd(com.facebook.ads.NativeAd nativeAd, View adView, boolean z) {
        nativeAdLayout = adView.findViewById(R.id.native_ad_container);
        Log.d(TAG, "Aspect ratio of ad: " + nativeAd.getAspectRatio());

        com.facebook.ads.MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        nativeAdMedia = adView.findViewById(R.id.native_ad_media);
        nativeAdMedia.setListener(getMediaViewListener());
        if (!z) {
            nativeAdMedia.setVisibility(View.GONE);
        }

        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        sponsoredLabel.setText(nativeAd.getAdHeadline());

        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdIcon);
        clickableViews.add(nativeAdMedia);
        clickableViews.add(nativeAdCallToAction);
        nativeAd.registerViewForInteraction(nativeAdLayout, nativeAdMedia, nativeAdIcon, clickableViews);

    }

    private static MediaViewListener getMediaViewListener() {
        return new MediaViewListener() {
            @Override
            public void onPlay(com.facebook.ads.MediaView mediaView) {

            }

            @Override
            public void onVolumeChange(com.facebook.ads.MediaView mediaView, float v) {

            }

            @Override
            public void onPause(com.facebook.ads.MediaView mediaView) {

            }

            @Override
            public void onComplete(com.facebook.ads.MediaView mediaView) {

            }

            @Override
            public void onEnterFullscreen(com.facebook.ads.MediaView mediaView) {

            }

            @Override
            public void onExitFullscreen(com.facebook.ads.MediaView mediaView) {

            }

            @Override
            public void onFullscreenBackground(com.facebook.ads.MediaView mediaView) {

            }

            @Override
            public void onFullscreenForeground(com.facebook.ads.MediaView mediaView) {

            }
        };
    }

    public void loadBanner(Activity activity, ViewGroup viewGroup) {
        if (isOnline(activity)) {
            if (new Ads_Preference(activity).getbanner_enable()) {
                if (new Ads_Preference(activity).get_priority().equalsIgnoreCase("AM")) {
                    String admBannerID = new Ads_Preference(activity).getAdmBannerID();
                    AdView adView = new AdView(activity);
                    adView.setAdSize(AdSize.BANNER);
                    adView.setAdUnitId(admBannerID);
                    adView.loadAd(new AdRequest.Builder().build());
                    ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
                    viewGroup.removeAllViews();
                    viewGroup.addView(adView, layoutParams);
                } else if (new Ads_Preference(activity).get_priority().equalsIgnoreCase("FB")) {
                    com.facebook.ads.AdView adView = new com.facebook.ads.AdView(activity, new Ads_Preference(activity).getFbBannerID(), com.facebook.ads.AdSize.BANNER_HEIGHT_50);
                    adView.loadAd(adView.buildLoadAdConfig().withAdListener(new com.facebook.ads.AdListener() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onError(Ad ad, com.facebook.ads.AdError adError) {
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            viewGroup.removeAllViews();
                            viewGroup.addView(adView);
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    }).build());
                }
            }
        }
    }

    @SuppressLint("WrongConstant")
    public boolean isOnline(Activity activity) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) activity.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

    }

    public void loadQureka(Activity activity, ViewGroup viewGroup, Boolean z) {

        viewGroup.findViewById(R.id.rlLoading).setVisibility(8);

        Integer[] numArr1 = {Integer.valueOf((int) R.drawable.qureka_native_small_1), Integer.valueOf((int) R.drawable.qureka_native_small_2), Integer.valueOf((int) R.drawable.qureka_native_small_3), Integer.valueOf((int) R.drawable.qureka_native_small_4), Integer.valueOf((int) R.drawable.qureka_native_small_5)};
        Integer[] numArr2 = {Integer.valueOf((int) R.drawable.qureka_1), Integer.valueOf((int) R.drawable.quiz_2), Integer.valueOf((int) R.drawable.qureka_3), Integer.valueOf((int) R.drawable.qureka_4), Integer.valueOf((int) R.drawable.qureka_5)};
        String url = new Ads_Preference(activity).getQureka_link();

        CardView cardQureka;
        cardQureka = viewGroup.findViewById(R.id.cardQureka);
        cardQureka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(Color.parseColor(activity.getString(R.color.app_color))).setShowTitle(true);
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.intent.setPackage("com.android.chrome");
                customTabsIntent.launchUrl(activity, Uri.parse(url));
            }
        });

        AppCompatButton btnPlayBig;
        btnPlayBig = viewGroup.findViewById(R.id.ivPlay);

        btnPlayBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(Color.parseColor(activity.getString(R.color.app_color))).setShowTitle(true);
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.intent.setPackage("com.android.chrome");
                customTabsIntent.launchUrl(activity, Uri.parse(url));
            }
        });

        if (z) {//true means big ad layout
            imageViewBig = (ImageView) viewGroup.findViewById(R.id.ivQurekaBig);
            Glide.with(activity).load(numArr1[new Random().nextInt(numArr1.length)]).into(imageViewBig);

        } else {//false means small ad layout
            imageviewSmall = (ImageView) viewGroup.findViewById(R.id.ivQurekaSmall);
            Glide.with(activity).load(numArr2[new Random().nextInt(numArr2.length)]).into(imageviewSmall);
        }

    }

    public void loadQurekaInterstitial(Activity activity, AdsCallBack adsCallingBack4) {

        if (new Ads_Preference(activity).get_qureka_webview_on_off().equalsIgnoreCase("ON")) {

            dialog = new Dialog(activity, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
            dialog.setContentView(R.layout.ad_interstitial_qureka);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            dialog.setCancelable(false);
            ProgressBar progressBar = dialog.findViewById(R.id.progressLoad);
            TextView tvCounter = dialog.findViewById(R.id.tvCounter);
            LinearLayout llClose = dialog.findViewById(R.id.llClose);
            llClose.setVisibility(View.GONE);
            tvCounter.setVisibility(View.GONE);
            llClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    adsCallingBack4.onLoading();

                }
            });

            //display webview in dialog
            WebView webView = dialog.findViewById(R.id.wb_webview);
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);

            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.getSettings().setDomStorageEnabled(true);

            webView.setWebViewClient(new WebViewClient() {
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    progressBar.setVisibility(View.VISIBLE);
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    //display time in timer after timer gone display close button
                    progressBar.setVisibility(View.GONE);
                    String qurekaTIme = new Ads_Preference(activity).get_qureka_time();
                    tvCounter.setVisibility(View.VISIBLE);
                    new CountDownTimer(Integer.parseInt(qurekaTIme), 600) {

                        public void onTick(long millisUntilFinished) {
                            timeCount = (int) millisUntilFinished / 1000;
                            tvCounter.setText("Close in : " + timeCount);

                            if (timeCount == 0) {
                                tvCounter.setVisibility(View.GONE);
                                llClose.setVisibility(View.VISIBLE);
                            }
                        }

                        public void onFinish() {

                        }
                    }.start();
                    super.onPageFinished(view, url);
                }

                @SuppressWarnings("deprecation")
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    webView.loadUrl("http://www.google.com");//if problem in load URL so display google page
                    llClose.setVisibility(View.VISIBLE);
                }
            });

            webView.loadUrl(new Ads_Preference(activity).getQureka_link());//get load url from fire store database
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (LoadingAds.isShowing()) {
                        LoadingAds.dismiss();
                    }
                    dialog.show();
                }

            }, 2000);
        }

        else if (new Ads_Preference(activity).get_qureka_webview_on_off().equalsIgnoreCase("OFF")){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (LoadingAds.isShowing()) {
                        LoadingAds.dismiss();
                    }
                        adsCallingBack4.onLoading();
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setToolbarColor(Color.parseColor(activity.getString(R.color.app_color))).setShowTitle(true);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.intent.setPackage("com.android.chrome");
                        customTabsIntent.launchUrl(activity, Uri.parse(new Ads_Preference(activity).getQureka_link()));
                }

            }, 1000);
        }

    }

    public void loadCustomInterstitial(Activity activity, AdsCallBack adsCallingBack4) {

        new GetContacts().execute();
        dialog = new Dialog(activity, 16974120);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.ad_interstitial);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        TextView tv_inter_ad_title = dialog.findViewById(R.id.tv_inter_ad_title);
        TextView tv_inter_ad_subtitle = dialog.findViewById(R.id.tv_inter_ad_subtitle);
        TextView tv_inter_review_count = dialog.findViewById(R.id.tv_inter_review_count);
        TextView tv_inter_ad_desc = dialog.findViewById(R.id.tv_inter_ad_desc);
        TextView tv_inter_ad_sub_desc = dialog.findViewById(R.id.tv_inter_ad_sub_desc);
        TextView tv_sponsored = dialog.findViewById(R.id.tv_sponsored);
        TextView tv_install_btn_inter = dialog.findViewById(R.id.tv_install_btn_inter);
        RatingBar iv_inter_star_rating = dialog.findViewById(R.id.iv_inter_star_rating);
        ImageView iv_inter_main_banner = dialog.findViewById(R.id.iv_inter_main_banner);
        ImageView iv_ad_icon = dialog.findViewById(R.id.iv_ad_icon);
        if (app_icon != null) {
            Glide.with(activity).load(app_icon).into(iv_ad_icon);
        } else {
            Glide.with(activity).load(R.drawable.quiz_2).into(iv_ad_icon);
        }
        if (app_banner != null) {
            Glide.with(activity).load(app_banner).into(iv_inter_main_banner);
        } else {
            Glide.with(activity).load(R.drawable.qureka_native_5).into(iv_inter_main_banner);
        }
        if (app_rate != null) {
            iv_inter_star_rating.setRating(Float.parseFloat(app_rate));
        }
        LinearLayout lay_close_ad = dialog.findViewById(R.id.lay_close_ad);
        ImageView iv_inter_info = dialog.findViewById(R.id.iv_inter_info);
        iv_inter_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(activity);
                dialog.setCancelable(true);
                dialog.getWindow().setBackgroundDrawableResource(R.color.black);
                View view1 = dialog.getLayoutInflater().inflate(R.layout.info_dialog, null);
                dialog.setContentView(view1);
                TextView delete = (TextView) view1.findViewById(R.id.no);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                lay_close_ad.setVisibility(View.VISIBLE);
            }

        }, 700);
        lay_close_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                adsCallingBack4.onLoading();

            }
        });
        tv_install_btn_inter.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(Color.parseColor(activity.getString(R.color.app_color))).setShowTitle(true);
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.intent.setPackage("com.android.chrome");
                if (app_link != null) {
                    customTabsIntent.launchUrl(activity, Uri.parse(app_link));
                } else {
                    customTabsIntent.launchUrl(activity, Uri.parse(new Ads_Preference(activity).getQureka_link()));
                }
            }
        });
        if (app_name != null) {
            tv_inter_ad_title.setText(app_name);
        }
        if (short_description != null) {
            tv_inter_ad_subtitle.setText(short_description);
        }
        if (app_review != null) {
            tv_inter_review_count.setText("(" + app_review + ")");
        }
        if (app_desc_title != null) {
            tv_inter_ad_desc.setText(app_desc_title);
        }
        if (app_sub_desc != null) {
            tv_inter_ad_sub_desc.setText(app_sub_desc);
        }
        if (app_sponsored != null) {
            tv_sponsored.setText(app_sponsored);
        }

        dialog.setCancelable(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (LoadingAds.isShowing()) {
                    LoadingAds.dismiss();
                }
                dialog.show();
            }

        }, 2000);
    }

    public void qurekaOrCustomInterstitial(Activity activity, AdsCallBack adsCallingBack4) {

        if (new Ads_Preference(activity).get_Custom_Inter() || new Ads_Preference(activity).getQurekaEnable()) {
            LoadingAds.show();

        } else {
            if (LoadingAds.isShowing()) {
                LoadingAds.dismiss();
            }
        }

        isFromSplash = false;

        if (adsCallingBack4.toString().contains("SplashActivity")) {
            //this condition for if AM and FB not load or occur error in splash activity so,
            // this condition use for not display qureka and custom interstitial in splash
            isFromSplash = true;
        } else {
            isFromSplash = false;
        }

        if (!isFromSplash) {//if false means load activity is not splash activity
            if (new Ads_Preference(activity).get_Custom_Inter()) {
                loadCustomInterstitial(activity, adsCallingBack4);

            } else if (new Ads_Preference(activity).getQurekaEnable()) {
                loadQurekaInterstitial(activity, adsCallingBack4);

            } else {

                adsCallingBack4.onLoading();

            }
        } else {//if true means load activity is splash activity so ad not load and intent to next activity

            adsCallingBack4.onLoading();

        }
    }

    public void NoConnectionDialog(boolean isFrom, final Activity activity, AdsCallBack adsCallingBack2) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("No internet Connection");
            builder.setMessage("Please turn on internet connection to continue");
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setCancelable(false);

            builder.setNegativeButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (isOnline(activity)) {//connect to internet so dismiss dialog and load ad
                        dialog.dismiss();
                    } else {
                        builder.show();//if internet not connect so alert dialog not close
                    }
                }
            });

            builder.show();
        } catch (Exception e) {
            Log.d(Constants.TAG, "Show Dialog: " + e.getMessage());
        }
    }

    private void ratingAppDialog(Activity activity, AdsCallBack adsCallingBack4) {

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_rating_app);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.findViewById(R.id.tvLater).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });
        dialog.findViewById(R.id.tvNow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("text/plain");
                    intent.putExtra("android.intent.extra.SUBJECT", R.string.app_name);
                    intent.putExtra("android.intent.extra.TEXT", "\nLet me recommend you this application\n\n" + "https://play.google.com/store/apps/details?id=" + activity.getPackageName() + "\n\n");
                    activity.startActivity(Intent.createChooser(intent, "choose one"));
                } catch (Exception unused) {
                }
            }
        });
        dialog.show();


    }

}