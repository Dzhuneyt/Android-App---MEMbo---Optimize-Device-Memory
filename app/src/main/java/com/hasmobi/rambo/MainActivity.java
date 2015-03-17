package com.hasmobi.rambo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

//import com.facebook.AppEventsLogger;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hasmobi.rambo.fragments.child.FragmentMainActionsNew;
import com.hasmobi.rambo.lib.DApp;
import com.hasmobi.rambo.lib.DDebug;
import com.hasmobi.rambo.lib.DException;
import com.hasmobi.rambo.lib.DResources;
import com.hasmobi.rambo.supers.DFragmentActivity;
import com.hasmobi.rambo.utils.FeedbackManager;
import com.hasmobi.rambo.utils.RemindToRateDialog;
import com.hasmobi.rambo.utils.Values;

public class MainActivity extends DFragmentActivity {

	private InterstitialAd onBackPressedInterestial = null;
	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setActionBarTitle(DResources.getString(c,
				R.string.app_name), "notosansregular.ttf");

		showChangelog();
		loadAds();

		// Fix for orientation change on some devices where Fragments defined in
		// the XML layout do not inflate a view on orientation change, so we
		// need to use some other layout like LinearLayout and replace it with
		// the Fragment on Activity create
		initFragmentsWorkaround();
	}

	@Override
	public void onResume() {
		super.onResume();

		startService(new Intent(this, BackgroundServiceStarters.class));

		if (adView != null) {
			adView.resume();
			adView.setVisibility(View.VISIBLE);
		}

		// Logs 'install' and 'app activate' App Events.
		//AppEventsLogger.activateApp(this);
	}


	@Override
	protected void onPause() {
		if (adView != null) {
			adView.pause();
		}
		// Logs 'app deactivate' App Event.
		//AppEventsLogger.deactivateApp(this);

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
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

				// Nothing to go back to. It is now time to exit the app
				// but first do something below

				// Remind to rate dialog. Hidden for next 30 days
				// if the user taps the button to rate (opens Google Play)
				RemindToRateDialog d = new RemindToRateDialog() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (rateButtonClicked) {
							// Track in Google Analytics that
							// the user clicked on the button
							// to go to Google Play (to rate the app)
							if (analyticsEnabled) {
								Tracker t = ((DFragmentActivity) getActivity()).t;
								if (t != null) {
									// Build and send an Event.
									t.send(new HitBuilders.EventBuilder()
											.setCategory("Remind To Rate Dialog")
											.setAction("Opened Google Play")
											.build());
								}
							}
						}
						super.onDismiss(dialog);
					}
				};

				boolean rateDialogShown = d.showIfNeeded(getBaseContext(), getSupportFragmentManager());

				if (rateDialogShown) {
					// Prevent default action
					return;
				} else {
					// We didn't show the Rate the app
					// dialog, so show the interestial ad if present
					// (but never both, this will be annoying)
					if (onBackPressedInterestial != null
							&& onBackPressedInterestial.isLoaded()) {
						Log.d(getClass().toString(), "Showing interestial");
						onBackPressedInterestial.show();
					}
				}
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
					adHolder.setVisibility(View.VISIBLE);
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
	private void initFragmentsWorkaround() {
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
	}

	private void showChangelog() {
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

				FragmentTransaction ft = fm.beginTransaction();
				Fragment exists = fm.findFragmentByTag("main");
				if (exists != null) {
					ft.remove(exists);
				}
				ft.replace(R.id.fMain, new FragmentMainActionsNew(), "main").commit();
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
