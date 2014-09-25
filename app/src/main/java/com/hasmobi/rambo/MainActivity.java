package com.hasmobi.rambo;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.hasmobi.rambo.fragments.child.FragmentMainActionsNew;
import com.hasmobi.rambo.lib.DApp;
import com.hasmobi.rambo.lib.DDebug;
import com.hasmobi.rambo.lib.DException;
import com.hasmobi.rambo.lib.DResources;
import com.hasmobi.rambo.supers.DFragmentActivity;
import com.hasmobi.rambo.utils.AutoBoostBroadcast;
import com.hasmobi.rambo.utils.ChangeLog;
import com.hasmobi.rambo.utils.FeedbackManager;
import com.hasmobi.rambo.utils.NotificationIcon;
import com.hasmobi.rambo.utils.Prefs;
import com.hasmobi.rambo.utils.TermsOfUse;
import com.hasmobi.rambo.utils.TypefaceSpan;
import com.hasmobi.rambo.utils.Values;
import com.hasmobi.rambo.utils.services.BackgroundSyncSleep;
import com.hasmobi.rambo.utils.services.WiFiSleep;

public class MainActivity extends DFragmentActivity {

    private InterstitialAd onBackPressedInterestial = null;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getActionBar() != null) {

            // Apply a custom TypeFace to the ActionBar title
            try {
                SpannableString s = new SpannableString(DResources.getString(c,
                        R.string.app_name));
                s.setSpan(new TypefaceSpan(this, "notosansregular.ttf"), 0,
                        s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                getActionBar().setTitle(s);
            } catch (Exception e) {
                log("Can not apply typeface to ActionBar title");
                log(e.getMessage());
            }
        }

        // Start the Notification icon if enabled in Settings
        NotificationIcon.notify(c);

        // Enables/disables the screen-on autobooster if needed
        enableAutoboost();

        showChangelog();

        new TermsOfUse(this).showIfNeeded();

        loadAds();

        // Fix for orientation change on some devices where Fragments defined in
        // the XML layout do not inflate a view on orientation change, so we
        // need to use some other layout like LinearLayout and replace it with
        // the Fragment on Activity create
        setupFragments();

        // Start the service that will
        // handle WiFi sleep
        startService(new Intent(this, WiFiSleep.class));
        startService(new Intent(this, BackgroundSyncSleep.class));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (adView != null) {
            adView.resume();
            adView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Log.d(getClass().toString(), "Back button pressed");

        FragmentManager fm = getSupportFragmentManager();

        if (fm != null) {

            Log.d(getClass().toString(), "Entries in backstack count: " + fm.getBackStackEntryCount());

            // Only show interestial if we are indeed exiting the app
            // Not going back from one fragment to another
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                if (onBackPressedInterestial != null
                        && onBackPressedInterestial.isLoaded())
                    Log.d(getClass().toString(), "Showing interestial");
                onBackPressedInterestial.show();
            }
        }

        super.onBackPressed();
    }

    /**
     * Prepare/setup/load the Admob ad request and interestial
     */
    private void loadAds() {
        // Display a warning notification if Google Play Services is not
        // installed or out of date on the device
        /* GooglePlayServicesUtil.showErrorNotification(
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this),
                this);*/

        // Look up the AdView as a resource and load a request.
        final LinearLayout adHolder = (LinearLayout) this
                .findViewById(R.id.adHolder);

        final Builder adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("A8CBBC91149E6975F4D95A9B210F5BDC")
                .addTestDevice("B3EEABB8EE11C2BE770B684D95219ECB");

        AdRequest ar = adRequest.build();

        if (adHolder != null) {
            adHolder.setVisibility(View.GONE);
            adHolder.removeAllViews();

            // Prepare the AdView
            AdView av = new AdView(this);

            av.setAdSize(AdSize.SMART_BANNER);
            av.setAdUnitId(Values.ADMOB_AD_UNIT_ID);
            av.setBackgroundColor(Color.TRANSPARENT);
            av.setVisibility(View.GONE);
            adHolder.addView(av);

            this.adView = av;

            adView.loadAd(ar);

            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    //adHolder.setVisibility(View.VISIBLE);
                }
            });

        }

        onBackPressedInterestial = new InterstitialAd(c);
        onBackPressedInterestial.setAdUnitId(Values.ADMOB_AD_UNIT_ID);
        onBackPressedInterestial.loadAd(ar);
    }

    /**
     * Initial setup and loading of fragments programmatically so they fill
     * their wrapping FrameLayout elements. This is done, because on some
     * devices declaring fragments using their fully qualified class name in XML
     * doesn't work
     */
    private void setupFragments() {
        FragmentManager fm = getSupportFragmentManager();

        FrameLayout fl = (FrameLayout) findViewById(R.id.fMain);
        if (fl != null)
            fl.removeAllViews();

        if (fm != null) {
            FragmentTransaction ft = fm.beginTransaction();
            Fragment existing = fm.findFragmentByTag("main");
            if (existing != null) {
                ft.remove(existing);
            }
            ft.add(R.id.fMain, new FragmentMainActionsNew(), "main");
            ft.commit();
        }

        fl = (FrameLayout) findViewById(R.id.fToolbar);
        fl.setVisibility(View.GONE);

        /*
        // For now hide the footer
		if (fl != null)
			fl.removeAllViews();

		if (fm != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.fToolbar, new FragmentToolbar());
			ft.commit();
		}*/
    }

    /**
     * Enables/disables the autobooster that runs every time the screen is
     * turned on or the keyboard is unlocked, depending on the Setting
     */
    private void enableAutoboost() {
        final Prefs p = new Prefs(c);
        if (p.isScreenOnAutoboostEnabled()) {
            final Intent i = new Intent(c, AutoBoostBroadcast.class);
            i.setAction(AutoBoostBroadcast.ACTION_SCREENON_AUTOBOOST_ENABLED);
            final PendingIntent pi = PendingIntent.getBroadcast(c, 0, i,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            try {
                pi.send();
            } catch (CanceledException e) {
                log("Can not start autobooster due to an exception.");
                log(e.getMessage());
            }
        }
    }

    /**
     * Show the Change Log dialog with "Ok", "More..." buttons if needed (only
     * on first start per each app version code)
     */
    private void showChangelog() {
        try {
            ChangeLog cl = new ChangeLog(this);
            if (cl.firstRun())
                cl.getLogDialog().show();
        } catch (Exception e) {
            DDebug.log(getClass().toString(),
                    "Can not display changelog dialog for some reason", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                // Replace current fragment with Home fragment
                FragmentManager fm = getSupportFragmentManager();

                // Clear the backstack
                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }

                FrameLayout fragmentHolder = (FrameLayout) this.findViewById(R.id.fMain);
                if (fragmentHolder != null) {
                    fragmentHolder.removeAllViews();
                } else {
                    DDebug.log(getClass().toString(), "Can not go to fragment home. Holder view not found");
                }

                if (fm != null) {
                    FragmentTransaction ft = fm.beginTransaction();
                    Fragment exists = fm.findFragmentByTag("main");
                    if (exists != null) {
                        ft.remove(exists);
                    }
                    ft.replace(R.id.fMain, new FragmentMainActionsNew(), "main").commit();
                } else {
                    DDebug.log(getClass().toString(), "Can not go to home fragment. Parent view not found");
                }
                return true;
            case R.id.action_feedback:
                new FeedbackManager(c).sendNewFeedbackEmail();
                return true;
            case R.id.action_update:
                try {
                    DApp.openGooglePlayStore(c);
                } catch (DException e) {
                    DDebug.log(getClass().toString(),
                            "Can not open Google Play store", e);
                    DDebug.toast(c, "Google Play store not installed on device");
                }
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, ConfigActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
