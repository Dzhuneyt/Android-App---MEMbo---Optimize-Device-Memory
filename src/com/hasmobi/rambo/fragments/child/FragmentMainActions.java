package com.hasmobi.rambo.fragments.child;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.utils.Debugger;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.Values;

public class FragmentMainActions extends DFragment {

	Context c;

	boolean fragmentActive = false;

	ActiveAppsLabel ActiveAppsLabel = null;
	AvailableMemoryLabel AvailableMemoryLabel = null;
	WhiteListedAppsLabel WhiteListedAppsLabel = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		c = getActivity().getBaseContext();

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		this.fragmentActive = true;

		ActiveAppsLabel = new ActiveAppsLabel();
		ActiveAppsLabel.start();

		AvailableMemoryLabel = new AvailableMemoryLabel();
		AvailableMemoryLabel.start();

		WhiteListedAppsLabel = new WhiteListedAppsLabel();
		WhiteListedAppsLabel.start();

		super.onResume();
	}

	@Override
	public void onPause() {
		this.fragmentActive = false;

		ActiveAppsLabel.stop();
		AvailableMemoryLabel.stop();
		WhiteListedAppsLabel.stop();

		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View layout = inflater.inflate(R.xml.fragment_main_actions, null);

		setupActionClickListeners(layout);

		setupFonts(layout);

		return layout;
	}

	private void setupActionClickListeners(View v) {

		OnClickListener listener = new OnClickListener() {

			public void onClick(View v) {
				Fragment newFragment = null;
				switch (v.getId()) {
				case R.id.actOpenPie:
					newFragment = new FragmentPie();
					break;
				case R.id.actOpenRunningApps:
					newFragment = new FragmentRunningApps();
					break;
				case R.id.actOpenSettings:
					newFragment = new FragmentSettings();
					break;
				case R.id.actOpenWhitelist:
					newFragment = new FragmentWhitelist();
					break;
				}
				try {
					final FragmentTransaction ft = getFragmentManager()
							.beginTransaction();
					ft.replace(R.id.fMain, newFragment, "ReplacementFragment");
					ft.commit();
				} catch (Exception e) {
					Debugger.log(e.getMessage());
					Debugger d = new Debugger(c);
					d.toast("Can not open new Fragment. Please, contact us at feedback@hasmobi.com");
				}
			}

		};

		final Button bActiveApps = (Button) v
				.findViewById(R.id.actOpenRunningApps);
		final Button bWhitelist = (Button) v
				.findViewById(R.id.actOpenWhitelist);
		final Button bSettings = (Button) v.findViewById(R.id.actOpenSettings);
		final Button bPie = (Button) v.findViewById(R.id.actOpenPie);

		bActiveApps.setOnClickListener(listener);
		bWhitelist.setOnClickListener(listener);
		bSettings.setOnClickListener(listener);
		bPie.setOnClickListener(listener);
	}

	private void setupFonts(View layout) {
		final Typeface face = Typeface.createFromAsset(c.getAssets(),
				"notosansregular.ttf");

		// Setup fonts for the labels below the buttons
		final TextView l1 = (TextView) layout
				.findViewById(R.id.tvMainActionsLabelPie);
		final TextView l2 = (TextView) layout
				.findViewById(R.id.tvMainActionsLabelRunningApps);
		final TextView l3 = (TextView) layout
				.findViewById(R.id.tvMainActionsLabelSettings);
		final TextView l4 = (TextView) layout
				.findViewById(R.id.tvMainActionsLabelWhitelist);
		if (l1 != null)
			l1.setTypeface(face);
		if (l2 != null)
			l2.setTypeface(face);
		if (l3 != null)
			l3.setTypeface(face);
		if (l4 != null)
			l4.setTypeface(face);

	}

	/**
	 * A self contained class that will auto-update the running processes list
	 * every N seconds and update a TextView
	 * 
	 */
	private class ActiveAppsLabel {
		Handler h = null;
		Runnable r = null;

		/**
		 * Setup the Handler and Runnable in constructor. They are not runned
		 * until you call start()
		 */
		public ActiveAppsLabel() {
			h = new Handler();
			r = new Runnable() {

				public void run() {
					if (fragmentActive) {

						ActivityManager am = (ActivityManager) c
								.getSystemService(Context.ACTIVITY_SERVICE);
						List<RunningAppProcessInfo> runningProcesses = am
								.getRunningAppProcesses();
						int activeAppsCount = runningProcesses.size();

						if (activeAppsCount > 0) {
							updateLabel(activeAppsCount);
						}

						h.postDelayed(this, 5000);
					}
				}

			};

		}

		public void start() {
			if (h == null) {
				h = new Handler();
			}
			if (r != null) {
				h.removeCallbacks(r);
			}
			h.post(r);
		}

		public void stop() {
			if (h != null && r != null)
				h.removeCallbacks(r);
		}

		private void updateLabel(int activeAppsCount) {
			try {
				final TextView tvActiveApps = (TextView) getView()
						.findViewById(R.id.tvMainActionsLabelRunningApps);
				String label = getActivity().getResources().getString(
						R.string.n_apps_placeholder);
				label = String.format(label, activeAppsCount);
				tvActiveApps.setText(label);
			} catch (Exception e) {
				Debugger.log("Can not update main actions label for running processes count. Error: "
						+ e.getMessage());
			}
		}

	}

	/**
	 * A self contained class that will auto-update the available RAM (in
	 * percents) every N seconds and update a TextView
	 * 
	 */
	private class AvailableMemoryLabel {
		Handler h = null;
		Runnable r = null;

		/**
		 * Setup the Handler and Runnable in constructor. They are not runned
		 * until you call start()
		 */
		public AvailableMemoryLabel() {
			final RamManager rm = new RamManager(getActivity().getBaseContext());

			h = new Handler();
			r = new Runnable() {

				public void run() {
					if (fragmentActive) {

						int freeRam = rm.getFreeRam();
						int totalRam = rm.getTotalRam();

						int freeRamPercent = freeRam * 100 / totalRam;

						if (freeRamPercent > 0 && freeRamPercent <= 100) {
							updateLabel(freeRamPercent);
						}

						h.postDelayed(this, 3000);
					}
				}

			};

		}

		public void start() {
			if (h == null) {
				h = new Handler();
			}
			if (r != null) {
				h.removeCallbacks(r);
			}
			h.post(r);
		}

		public void stop() {
			if (h != null && r != null)
				h.removeCallbacks(r);
		}

		private void updateLabel(int freeRamPercent) {
			try {
				final TextView tvActiveApps = (TextView) getView()
						.findViewById(R.id.tvMainActionsLabelPie);
				String label = getActivity().getResources().getString(
						R.string.available_percent_ram_label);
				label = String.format(label, freeRamPercent);
				tvActiveApps.setText(label);
			} catch (Exception e) {
				Debugger.log("Can not update main actions label for free ram percentage. Error: "
						+ e.getMessage());
			}
		}

	}

	/**
	 * A self contained class that will auto-update the available RAM (in
	 * percents) every N seconds and update a TextView
	 * 
	 */
	private class WhiteListedAppsLabel {
		Handler h = null;
		Runnable r = null;

		/**
		 * Setup the Handler and Runnable in constructor. They are not runned
		 * until you call start()
		 */
		public WhiteListedAppsLabel() {

			final SharedPreferences excludedPrefs = getActivity()
					.getSharedPreferences(Values.EXCLUDED_LIST_FILE, 0);

			h = new Handler();
			r = new Runnable() {

				public void run() {
					if (fragmentActive) {
						int count = 0;

						try {
							count = excludedPrefs.getAll().size();
						} catch (NullPointerException e) {

						}

						updateLabel(count);
						h.postDelayed(this, 10000);
					}
				}

			};

		}

		public void start() {
			if (h == null) {
				h = new Handler();
			}
			if (r != null) {
				h.removeCallbacks(r);
			}
			h.post(r);
		}

		public void stop() {
			if (h != null && r != null)
				h.removeCallbacks(r);
		}

		private void updateLabel(int whitelistedAppsCount) {
			try {
				final TextView tvActiveApps = (TextView) getView()
						.findViewById(R.id.tvMainActionsLabelWhitelist);
				String label = getActivity().getResources().getString(
						R.string.n_apps_placeholder);
				label = String.format(label, whitelistedAppsCount);
				tvActiveApps.setText(label);
			} catch (Exception e) {
				Debugger.log("Can not update main actions label for whitelisted apps count. Error: "
						+ e.getMessage());
			}
		}

	}
}
