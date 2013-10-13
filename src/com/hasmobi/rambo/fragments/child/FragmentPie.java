package com.hasmobi.rambo.fragments.child;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.utils.Debugger;
import com.hasmobi.rambo.utils.Prefs;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.Values;
import com.hasmobi.rambo.utils.custom_views.PieView;

public class FragmentPie extends DFragment {
	long totalRam = 0, freeRam = 0;

	Context context;

	// SharedPreferences prefs;

	private Handler handler;
	private Runnable r;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getActivity().getBaseContext();

		appStartLogger();
	}

	private void setupInBackground() {
		startUpdating();
	}

	/**
	 * Some utility checks and actions on each app start
	 */
	private void appStartLogger() {
		final Prefs p = new Prefs(context);
		final SharedPreferences prefs = p.instance();

		int pieStartCount = prefs.getInt("pie_start_count", 0);

		if (pieStartCount == 0) {
			pieFirstStart();
		}

		prefs.edit().putInt("pie_start_count", pieStartCount + 1).commit();
	}

	private void pieFirstStart() {

		// Initial core apps whitelist
		final String[] defaultExcluded = { "system_process",
				"com.hasmobi.rambo", "com.android.phone",
				"com.android.systemui", "android.process.acore",
				"com.android.launcher" };
		final SharedPreferences.Editor excludedList = getActivity()
				.getSharedPreferences("excluded_list", 0).edit();
		for (int i = 0; i + 1 < defaultExcluded.length; i++) {
			// Put them in SharedPreferences
			excludedList.putBoolean(defaultExcluded[i], true);
		}
		excludedList.commit();
		Debugger.log("Default exclude list populated");
	}

	@Override
	public void onResume() {
		super.onResume();

		setupInBackground();
	}

	/**
	 * Start updating the RAM every N seconds
	 */
	private void startUpdating() {
		try {
			// Cancel any previously running RAM updater instances
			handler.removeCallbacks(r);
		} catch (NullPointerException e) {
		}

		final int updateInterval = Values.PIE_UPDATE_INTERVAL; // milliseconds

		handler = new Handler();
		r = new Runnable() {
			public void run() {
				if (fragmentVisible) {
					new freeRamUpdater();
					handler.postDelayed(r, updateInterval);
				}
			}
		};
		handler.post(r);
	}

	@Override
	public void onPause() {
		super.onPause();

		if (handler != null && r != null)
			handler.removeCallbacks(r);
	}

	/**
	 * A class that gets the free and total RAM and sets them to the appropriate
	 * TextViews.
	 * 
	 * @author hasMobi.com
	 * 
	 */
	protected class freeRamUpdater extends AsyncTask<String, String, Void> {

		private RamManager ramManager = null;

		public freeRamUpdater() {
			this.execute();
		}

		@Override
		protected Void doInBackground(String... params) {
			if (!isCancelled()) {
				if (ramManager == null)
					ramManager = new RamManager(context);

				totalRam = ramManager.getTotalRam();
				freeRam = ramManager.getFreeRam();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// Redraw the pie chart
			try {
				PieView pie = (PieView) getView().findViewById(R.id.pie);
				pie.setRam(totalRam, freeRam);
			} catch (Exception e) {
				Debugger.log("Can not update pie");
			}

			if (freeRam > 0 && totalRam > 0) {
				// Do something with the updated values here
			} else {
				Debugger.log("Free or Total RAM not set at the moment. One of them is zero");
			}

		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.xml.fragment_pie, null);

		return v;
	}
}
