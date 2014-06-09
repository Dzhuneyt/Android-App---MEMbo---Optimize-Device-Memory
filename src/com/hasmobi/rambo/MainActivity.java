package com.hasmobi.rambo;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.hasmobi.lib.DApp;
import com.hasmobi.lib.DDebug;
import com.hasmobi.lib.DException;
import com.hasmobi.rambo.fragments.FragmentToolbar;
import com.hasmobi.rambo.fragments.child.FragmentMainActions;
import com.hasmobi.rambo.supers.DFragmentActivity;
import com.hasmobi.rambo.utils.AutoBoostBroadcast;
import com.hasmobi.rambo.utils.ChangeLog;
import com.hasmobi.rambo.utils.FeedbackManager;
import com.hasmobi.rambo.utils.NotificationIcon;
import com.hasmobi.rambo.utils.Prefs;
import com.hasmobi.rambo.utils.ResManager;
import com.hasmobi.rambo.utils.TermsOfUse;
import com.hasmobi.rambo.utils.TypefaceSpan;

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
				SpannableString s = new SpannableString(ResManager.getString(c,
						R.string.app_name));
				s.setSpan(new TypefaceSpan(this, "notosansregular.ttf"), 0,
						s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				getActionBar().setTitle(s);
			} catch (Exception e) {
				log("Can not apply typeface to ActionBar title");
				log(e.getMessage());
			}
		}

		// Fix for orientation change on some devices where Fragments defined in
		// the XML layout do not inflate a view on orientation change, so we
		// need to use some other layout like LinearLayout and replace it with
		// the Fragment on Acitity create
		setupFragments();

		sendNotification();

		enableAutoboost();

		showChangelog();

		showTOS();

		// Look up the AdView as a resource and load a request.
		this.adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice(
						"B3EEABB8EE11C2BE770B684D95219ECB|A8CBBC91149E6975F4D95A9B210F5BDC")
				.addTestDevice("A8CBBC91149E6975F4D95A9B210F5BDC")
				.addTestDevice("B3EEABB8EE11C2BE770B684D95219ECB").build();
		if (adView != null) {
			// Hide the ad block while it loads
			adView.setVisibility(View.GONE);

			adView.loadAd(adRequest);

			adView.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
					adView.setVisibility(View.VISIBLE);
				}
			});
		}

		onBackPressedInterestial = new InterstitialAd(c);
		onBackPressedInterestial.setAdUnitId("a1507c306fdcc36");
		onBackPressedInterestial.loadAd(adRequest);

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
		if (onBackPressedInterestial != null
				&& onBackPressedInterestial.isLoaded())
			onBackPressedInterestial.show();

		super.onBackPressed();
	}

	private void setupFragments() {
		FragmentManager fm = getSupportFragmentManager();

		FrameLayout fl;

		fl = (FrameLayout) findViewById(R.id.fMain);
		fl.removeAllViews();
		if (fm != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.fMain, new FragmentMainActions());
			ft.commit();
		}

		fl = (FrameLayout) findViewById(R.id.fToolbar);
		fl.removeAllViews();
		if (fm != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.fToolbar, new FragmentToolbar());
			ft.commit();
		}
	}

	private void sendNotification() {
		NotificationIcon.notify(c);
	}

	private void enableAutoboost() {
		final Prefs p = new Prefs(c);
		if (p.isAutoboostEnabled()) {
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

	private void showTOS() {
		TermsOfUse tos = new TermsOfUse(this);

		if (!tos.accepted())
			tos.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FeedbackManager fm = new FeedbackManager(c);

		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_feedback:
			fm.feedbackDialog();
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
