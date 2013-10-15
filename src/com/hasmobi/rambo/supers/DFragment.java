package com.hasmobi.rambo.supers;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.hasmobi.rambo.utils.Debugger;
import com.hasmobi.rambo.utils.RamManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

public class DFragment extends Fragment {
	public boolean fragmentVisible = false;

	public Context c;

	private Tracker tracker;

	// Placeholder property ID.
	private static final String GA_PROPERTY_ID = "UA-1704294-170";

	// Dispatch period in seconds.
	private static final int GA_DISPATCH_PERIOD = 5;

	private BroadcastReceiver broadcast = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		c = getActivity().getBaseContext();

		super.onCreate(savedInstanceState);

		// this.tracker = EasyTracker.getInstance(this.getActivity());
		// this.tracker.getLogger().setLogLevel(LogLevel.VERBOSE);

		broadcast = new BroadcastReceiver() {

			@Override
			public void onReceive(Context c, Intent i) {
				handleBroadcast(c, i);
			}

		};

		c.registerReceiver(broadcast, new IntentFilter(
				RamManager.ACTION_RAM_MANAGER));
	}

	@Override
	public void onPause() {
		fragmentVisible = false;

		Debugger.log(getClass().getSimpleName() + " onPause()");
		if (broadcast != null)
			c.unregisterReceiver(broadcast);

		super.onPause();
	}

	@Override
	public void onResume() {
		fragmentVisible = true;

		if (broadcast == null) {
			broadcast = new BroadcastReceiver() {
				@Override
				public void onReceive(Context c, Intent i) {
					handleBroadcast(c, i);
				}
			};
		}

		c.registerReceiver(broadcast, new IntentFilter(
				RamManager.ACTION_RAM_MANAGER));

		super.onResume();

		Debugger.log(getClass().getSimpleName() + " onResume()");

		// Log the fragment view event to Google Analytics
		// this.tracker.set(Fields.SCREEN_NAME, getClass().getSimpleName());
		// this.tracker.send(MapBuilder.createAppView().build());
	}

	public void handleBroadcast(Context c, Intent i) {
		Debugger.log(getClass().getSimpleName() + " handle broadcast");
		handleBroadcast(c, i);
	}

	public void hideView(View v) {
		if (v != null)
			v.setVisibility(View.GONE);
	}

	public void showView(View v) {
		if (v != null)
			v.setVisibility(View.VISIBLE);
	}

	public void log(String s) {
		Debugger.log(s);
	}

}
