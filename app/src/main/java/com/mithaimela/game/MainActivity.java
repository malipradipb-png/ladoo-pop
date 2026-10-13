package com.mithaimela.game;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MainActivity extends Activity {

    // ================== AD IDs ==================
    // Abhi ye Google ke TEST IDs hain (inse kamai nahi hoti, sirf jaanch ke liye).
    // AdMob account mein ad units banakar apne asli IDs yahan daalein.
    // App ID AndroidManifest.xml mein badlein.
    static final String BANNER_ID       = "ca-app-pub-3940256099942544/6300978111";
    static final String INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712";
    static final String REWARDED_ID     = "ca-app-pub-3940256099942544/5224354917";

    private WebView web;
    private AdView banner;
    private InterstitialAd interstitial;
    private RewardedAd rewarded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1A0C21);

        web = new WebView(this);
        web.setBackgroundColor(0xFF1A0C21);
        root.addView(web, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        banner = new AdView(this);
        banner.setAdUnitId(BANNER_ID);
        banner.setAdSize(AdSize.BANNER);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        root.addView(banner, bp);

        setContentView(root);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);           // levels aur sitare save rehte hain
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new AdsBridge(), "AndroidAds");
        web.loadUrl("file:///android_asset/index.html");

        MobileAds.initialize(this, status -> {
            banner.loadAd(new AdRequest.Builder().build());
            loadInterstitial();
            loadRewarded();
        });
    }

    private void loadInterstitial() {
        InterstitialAd.load(this, INTERSTITIAL_ID, new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override public void onAdLoaded(@NonNull InterstitialAd ad) { interstitial = ad; }
                    @Override public void onAdFailedToLoad(@NonNull LoadAdError e) { interstitial = null; }
                });
    }

    private void loadRewarded() {
        RewardedAd.load(this, REWARDED_ID, new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override public void onAdLoaded(@NonNull RewardedAd ad) { rewarded = ad; }
                    @Override public void onAdFailedToLoad(@NonNull LoadAdError e) { rewarded = null; }
                });
    }

    /** Game ko batata hai ki ad khatam ho gaya (true/false). */
    private void reply(String fn, boolean value) {
        runOnUiThread(() -> web.evaluateJavascript(
                "window." + fn + "&&window." + fn + "(" + value + ")", null));
    }

    /** Game (JavaScript) yahan se ads mangta hai. */
    private class AdsBridge {
        @JavascriptInterface
        public void showInterstitial() {
            runOnUiThread(() -> {
                if (interstitial == null) { reply("__interDone", false); loadInterstitial(); return; }
                interstitial.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override public void onAdDismissedFullScreenContent() {
                        interstitial = null; loadInterstitial(); reply("__interDone", true);
                    }
                    @Override public void onAdFailedToShowFullScreenContent(@NonNull AdError e) {
                        interstitial = null; loadInterstitial(); reply("__interDone", false);
                    }
                });
                interstitial.show(MainActivity.this);
            });
        }

        @JavascriptInterface
        public void showRewarded() {
            runOnUiThread(() -> {
                if (rewarded == null) { reply("__rewardDone", false); loadRewarded(); return; }
                final boolean[] earned = {false};
                rewarded.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override public void onAdDismissedFullScreenContent() {
                        rewarded = null; loadRewarded(); reply("__rewardDone", earned[0]);
                    }
                    @Override public void onAdFailedToShowFullScreenContent(@NonNull AdError e) {
                        rewarded = null; loadRewarded(); reply("__rewardDone", false);
                    }
                });
                rewarded.show(MainActivity.this, item -> earned[0] = true);
            });
        }
    }

    @Override protected void onPause()  { web.onPause();  banner.pause();  super.onPause(); }
    @Override protected void onResume() { super.onResume(); web.onResume(); banner.resume(); }
    @Override protected void onDestroy() { banner.destroy(); web.destroy(); super.onDestroy(); }
}
