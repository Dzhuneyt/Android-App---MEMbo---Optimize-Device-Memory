package com.hasmobi.rambo.fragments.child;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
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

public class FragmentMainActions extends DFragment {

	ActiveAppsLabel ActiveAppsLabel = null;
	TotalMemoryLabel AvailableMemoryLabel = null;
	FreeMemoryLabel FreeMemoryLabel = null;

	@Override
	public void onResume() {
		ActiveAppsLabel = new ActiveAppsLabel();
		ActiveAppsLabel.start();

		AvailableMemoryLabel = new TotalMemoryLabel();
		AvailableMemoryLabel.start();

		FreeMemoryLabel = new FreeMemoryLabel();
		FreeMemoryLabel.start();

		super.onResume();
	}

	@Override
	public void onPause() {
		ActiveAppsLabel.stop();
		AvailableMemoryLabel.stop();
		FreeMemoryLabel.stop();

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

	public void handleBroadcast(Context c, Intent i) {
		if (fragmentVisible) {
			if (ActiveAppsLabel == null) {
				ActiveAppsLabel = new ActiveAppsLabel();
			} else {
				ActiveAppsLabel.stop();
			}
			ActiveAppsLabel.start();

			if (AvailableMemoryLabel == null) {
				AvailableMemoryLabel = new TotalMemoryLabel();
			} else {
				AvailableMemoryLabel.stop();
			}
			AvailableMemoryLabel.start();

			if (FreeMemoryLabel == null) {
				FreeMemoryLabel = new FreeMemoryLabel();
			} else {
				FreeMemoryLabel.stop();
			}
			FreeMemoryLabel.start();
		}

		super.handleBroadcast(c, i);
	}

	private void setupActionClickListeners(View v) {

		OnClickListener listener = new OnClickListener() {

			public void onClick(View v) {
				Fragment newFragment = null;
				switch (v.getId()) {
				case R.id.actOpenRunningApps:
					newFragment = new FragmentRunningApps();
					break;
				case R.id.actOpenWhitelist:
					newFragment = new FragmentWhitelist();
					break;
				}
				try {
					final FragmentTransaction ft = getFragmentManager()
							.beginTransaction();
					ft.replace(R.id.fMain, newFragment, "ReplacementFragment");
					ft.addToBackStack(null);
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

		bActiveApps.setOnClickListener(listener);
		bWhitelist.setOnClickListener(listener);
	}

	private void setupFonts(View layout) {

		final Typeface bold = Typeface.createFromAsset(c.getAssets(),
				"sttransmission_800_extrabold.otf");

		final TextView l5 = (TextView) layout
				.findViewById(R.id.tvMainActionsLabelTotalRam);
		final TextView l6 = (TextView) layout
				.findViewById(R.id.tvMainActionsLabelAvailableRam);
		final TextView l7 = (TextView) layout
				.findViewById(R.id.tvMainActionsLabelRunningApps);
		if (l5 != null)
			l5.setTypeface(bold);
		if (l6 != null)
			l6.setTypeface(bold);
		if (l7 != null)
			l7.setTypeface(bold);

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
					if (fragmentVisible) {

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
	private class TotalMemoryLabel {
		Handler h = null;
		Runnable r = null;

		/**
		 * Setup the Handler and Runnable in constructor. They are not runned
		 * until you call start()
		 */
		public TotalMemoryLabel() {
			final RamManager rm = new RamManager(getActivity().getBaseContext());

			h = new Handler();
			r = new Runnable() {

				public void run() {
					if (fragmentVisible) {

						int totalRam = rm.getTotalRam();

						updateLabel(totalRam);

						h.postDelayed(this, 12000);
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

		private void updateLabel(int totalRam) {
			try {
				final TextView tvActiveApps = (TextView) getView()
						.findViewById(R.id.tvMainActionsLabelTotalRam);
				String label = getActivity().getResources().getString(
						R.string.total_ram_label);
				label = String.format(label, totalRam);
				tvActiveApps.setText(label);
			} catch (Exception e) {
				Debugger.log("Can not update main actions label for total ram. Error: "
						+ e.getMessage());
			}
		}

	}

	/**
	 * A self contained class that will auto-update the available RAM (in
	 * percents) every N seconds and update a TextView
	 * 
	 */
	private class FreeMemoryLabel {
		Handler h = null;
		Runnable r = null;

		/**
		 * Setup the Handler and Runnable in constructor. They are not runned
		 * until you call start()
		 */
		public FreeMemoryLabel() {
			final RamManager rm = new RamManager(getActivity().getBaseContext());

			h = new Handler();
			r = new Runnable() {

				public void run() {
					if (fragmentVisible) {

						int freeRam = rm.getFreeRam();

						updateLabel(freeRam);

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

		private void updateLabel(int freeRam) {
			try {
				final TextView tvActiveApps = (TextView) getView()
						.findViewById(R.id.tvMainActionsLabelAvailableRam);
				String label = getActivity().getResources().getString(
						R.string.free_ram_label);
				label = String.format(label, freeRam);
				tvActiveApps.setText(label);
			} catch (Exception e) {
				Debugger.log("Can not update main actions label for free ram. Error: "
						+ e.getMessage());
			}
		}

	}

}
