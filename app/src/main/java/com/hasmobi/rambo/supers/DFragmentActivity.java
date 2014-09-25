package com.hasmobi.rambo.supers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.hasmobi.rambo.R;
import com.hasmobi.rambo.lib.DDebug;
import com.hasmobi.rambo.utils.TermsOfUse;
import com.hasmobi.rambo.utils.Values;

public class DFragmentActivity extends FragmentActivity {

	public Context c;

	public Tracker t;
	boolean analyticsEnabled = false;

	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		c = getBaseContext();

		DDebug.log(getClass().toString(), "onCreate()");

		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);

		if (Values.DEBUG_MODE)
			analytics.setDryRun(true);

		this.t = analytics.newTracker(R.xml.global_tracker);
	}


	@Override
	public void onResume() {
		super.onResume();

		try {
			analyticsEnabled = this.getSharedPreferences("settings", 0)
					.getBoolean(TermsOfUse.PREF_NAME_ANALYTICS, false);
		} catch (Exception e) {
		}

		if (analyticsEnabled) {
			if (!Values.DEBUG_MODE && t != null) {
				t.setScreenName(getClass().getSimpleName());

				// Send a screen view.
                // Don't track activities
				// t.send(new HitBuilders.AppViewBuilder().build());
			}
		}
	}

	public boolean hideView(int res) {
		View v = (View) findViewById(res);
		if (v != null) {
			v.setVisibility(View.GONE);
			return true;
		}

		return false;
	}

	public boolean showView(int res) {
		View v = (View) findViewById(res);
		if (v != null) {
			v.setVisibility(View.VISIBLE);
			return true;
		}

		return false;
	}

	public void log(String message) {
		if (message.length() > 0)
			Log.d(Values.DEBUG_TAG + " " + this.getClass().toString(), message);
	}

}
