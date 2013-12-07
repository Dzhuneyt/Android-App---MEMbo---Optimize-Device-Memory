package com.hasmobi.rambo.fragments.child;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.utils.RamManager;

public class FragmentMainActions extends DFragment {

	Handler h = new Handler();
	Runnable r = null;

	@Override
	public void onResume() {

		if (r == null) {
			r = new Runnable() {

				public void run() {

					View fragmentView = getView();
					if (fragmentView == null) {
						// Oh, oh. The fragment hasn't inflated a view yet
						h.removeCallbacks(r);
						h.postDelayed(r, 1500);
						return;
					}

					// Get running apps count
					ActivityManager am = (ActivityManager) c
							.getSystemService(Context.ACTIVITY_SERVICE);
					PackageManager pm = c.getPackageManager();
					/*
					 * List<RunningTaskInfo> runningTasks = am
					 * .getRunningTasks(100);
					 */
					List<RunningAppProcessInfo> runningProcesses = am
							.getRunningAppProcesses();
					int activeAppsCount = runningProcesses.size();

					int systemApps = 0;

					for (RunningAppProcessInfo process : runningProcesses) {
						String pkgName = process.processName;
						boolean system = false;
						if (pm.getLaunchIntentForPackage(pkgName) == null) {
							system = true;
						}

						if (system) {
							systemApps++;
						}

						/*
						 * log(pkgName + " " + (system ? "is system" :
						 * " is NOT system") + " app");
						 */
					}
					// log("  ");

					if (activeAppsCount > 0) {
						final TextView tvActiveApps = (TextView) getView()
								.findViewById(
										R.id.tvMainActionsLabelRunningApps);
						String label = getActivity().getResources().getString(
								R.string.n_apps_placeholder);
						label = String.format(label, activeAppsCount);
						tvActiveApps.setText(label);
					}

					if (systemApps > 0) {
						final TextView tvSystemApps = (TextView) fragmentView
								.findViewById(R.id.tvSublabelSystemapps);
						if (tvSystemApps != null) {
							String label = getActivity()
									.getResources()
									.getString(
											R.string.main_actions_sublabel_systemapps);
							label = String.format(label, systemApps);
							tvSystemApps.setText(label);
						}
					}

					// Get total/free memory
					final RamManager rm = new RamManager(getActivity()
							.getBaseContext());
					int totalRam = rm.getTotalRam();
					int freeRam = rm.getFreeRam();
					int freePercent = freeRam * 100 / totalRam; // e.g. 20 for
																// 20% free

					final TextView tvActiveApps = (TextView) getView()
							.findViewById(R.id.tvMainActionsLabelTotalRam);
					if (tvActiveApps != null) {
						String label = getActivity().getResources().getString(
								R.string.main_actions_used_free_ram);
						label = String.format(label, freePercent);
						tvActiveApps.setText(label);
					}

					final TextView tvSublabelMBs = (TextView) getView()
							.findViewById(R.id.tvSubLabelMBs);
					if (tvSublabelMBs != null) {
						String label = getActivity().getResources().getString(
								R.string.main_actions_sublabel_mbs);
						label = String.format(label, freeRam, totalRam);
						tvSublabelMBs.setText(label);
					}

					h.postDelayed(r, 5000);
				}

			};
		}
		h.removeCallbacks(r);
		h.post(r);

		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View layout = inflater.inflate(R.xml.fragment_main_actions, null);

		FragmentManager fm = getActivity().getSupportFragmentManager();
		if (fm != null) {
			// Show running apps at the bottom 3/4 part of the screen
			LinearLayout v = (LinearLayout) layout
					.findViewById(R.id.llMainActionsBottom);
			if (v != null) {
				log("replacing fragment");
				v.removeAllViews();
				FragmentTransaction ft = fm.beginTransaction();
				ft.add(R.id.llMainActionsBottom, new FragmentRunningApps());
				ft.commit();
			}

		}

		return layout;
	}

	@Override
	public void handleBroadcast(Context c, Intent i) {
		if (h != null && r != null) {
			h.removeCallbacks(r);
			h.post(r);
		}

		super.handleBroadcast(c, i);
	}

	/**
	 * @deprecated - We use the standard fonts now
	 * @param layout
	 */
	@Deprecated
	private void setupFonts(View layout) {

		final Typeface bold = Typeface.createFromAsset(c.getAssets(),
				"sttransmission_800_extrabold.otf");

		final TextView l5 = (TextView) layout
				.findViewById(R.id.tvMainActionsLabelTotalRam);
		final TextView l7 = (TextView) layout
				.findViewById(R.id.tvMainActionsLabelRunningApps);
		if (l5 != null)
			l5.setTypeface(bold);
		if (l7 != null)
			l7.setTypeface(bold);

	}

}
