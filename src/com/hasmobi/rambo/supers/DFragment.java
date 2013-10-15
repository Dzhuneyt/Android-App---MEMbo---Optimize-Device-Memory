package com.hasmobi.rambo.supers;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.hasmobi.rambo.utils.Debugger;

import android.content.Context;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {

		c = getActivity().getBaseContext();

		super.onCreate(savedInstanceState);

		this.tracker = EasyTracker.getInstance(this.getActivity());
		// this.tracker.getLogger().setLogLevel(LogLevel.VERBOSE);
	}

	@Override
	public void onPause() {
		fragmentVisible = false;
		super.onPause();
	}

	@Override
	public void onResume() {
		fragmentVisible = true;
		super.onResume();

		Debugger.log(getClass().getSimpleName() + " onResume()");

		// Log the fragment view event to Google Analytics
		this.tracker.set(Fields.SCREEN_NAME, getClass().getSimpleName());
		this.tracker.send(MapBuilder.createAppView().build());
	}

	public void hideView(View v) {
		if (v != null)
			v.setVisibility(View.GONE);
	}

	public void showView(View v) {
		if (v != null)
			v.setVisibility(View.VISIBLE);
	}

}
