package com.hasmobi.rambo.fragments.child;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.hasmobi.lib.DDebug;
import com.hasmobi.rambo.R;
import com.hasmobi.rambo.adapters.ActiveProcessAdapter;
import com.hasmobi.rambo.adapters.SingleProcess;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.utils.Debugger;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.Values;

public class FragmentRunningApps extends DFragment {

	private boolean updateInProgress = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.xml.fragment_running_processes, null);
	}

	@Override
	public void onResume() {
		super.onResume();

		View fragmentView = getView();

		if (fragmentView == null) {
			// Fragment hasn't inflated view yet
			return;
		} else {
			showView(fragmentView
					.findViewById(R.id.tvRunningProcessesPlaceholder));
			hideView(fragmentView.findViewById(R.id.lvRunningProcesses));
			setupListView();
		}

		/*
		 * 
		 * try { // If the ListView was previously populated, restore it for now
		 * final ListView lv = (ListView) getView().findViewById(
		 * R.id.lvRunningProcesses); final TextView tvPlaceholder = (TextView)
		 * getView().findViewById( R.id.tvRunningProcessesPlaceholder); if (lv
		 * != null && tvPlaceholder != null) { if (lv.getAdapter() != null &&
		 * lv.getAdapter().getCount() > 0) {
		 * tvPlaceholder.setVisibility(View.GONE);
		 * lv.setVisibility(View.VISIBLE); } } } catch (Exception e) {
		 * 
		 * }
		 */
	}

	@Override
	public void handleBroadcast(Context c, Intent i) {
		if (fragmentVisible) {
			setupListView();
		}

		super.handleBroadcast(c, i);
	}

	/**
	 * An Async class that updates the running processes list, called via
	 * setupListView()
	 * 
	 */
	class listUpdater extends AsyncTask<Void, Void, Void> {

		List<SingleProcess> listOfProcesses = null;

		ActiveProcessAdapter adapter = null;

		SingleProcess currentProcess = null;

		boolean lvAdapterSet = false;

		// publishEvery controls how often should the AsyncTask call
		// publishProgress(), which causes the ListView's adapter to be filled
		// with the new values we got this far
		int publishEvery = 5, counter = 0;

		public listUpdater() {

			try {
				this.execute();
				updateInProgress = true;
			} catch (IllegalStateException e) {
				DDebug.log(getClass().toString(), "listUpdater()", e);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (adapter == null) {
				adapter = new ActiveProcessAdapter(getActivity(), null);
			}
		}

		@Override
		protected Void doInBackground(Void... v) {
			if (isCancelled())
				return null;

			// Get the excluded apps list
			final SharedPreferences excluded_list = c.getSharedPreferences(
					"excluded_list", 0);
			ActivityManager am = (ActivityManager) c
					.getSystemService(Context.ACTIVITY_SERVICE);
			PackageManager pm = c.getPackageManager();

			final List<RunningAppProcessInfo> runningProcesses = am
					.getRunningAppProcesses();
			if (runningProcesses.size() == 0) {
				// Something went wrong, start a new AsyncTask
				log("No running processes found. Restarting AsyncTask.");
				cancel(true);
				setupListView();
				return null;
			} else {
				// There are some running processes

				RamManager rm = new RamManager(c);
				int totalTakenRam = (rm.getTotalRam() - rm.getFreeRam());

				if (listOfProcesses == null) {
					// This will hold all processes and their infos
					listOfProcesses = new ArrayList<SingleProcess>();
				}

				// Get current package and exclude it from the list
				String currentAppPackageName = "";
				try {
					currentAppPackageName = c.getPackageName();
				} catch (Exception e) {
				}

				// First get the total PSS (private-shared-dirty memory)
				// assigned to all running processes.
				// Then (below) get the PSS used by each process and compare it
				// to the total usage, so we have a more realistic measure of
				// the proportion of the memory used by each process
				float totalPss = 0;
				for (RunningAppProcessInfo pid : runningProcesses) {
					if (!pid.processName
							.equalsIgnoreCase(currentAppPackageName)) {
						final MemoryInfo memoryInfoArray = am
								.getProcessMemoryInfo(new int[] { pid.pid })[0];
						totalPss = totalPss + memoryInfoArray.getTotalPss();
					}
				}

				ApplicationInfo ai = null; // cache

				// Weigh each individual running process' PSS usage to the total
				// PSS usage and use the resulting percentage to estimate how
				// much MB of the total used RAM this process uses
				for (RunningAppProcessInfo pid : runningProcesses) {

					if (!pid.processName
							.equalsIgnoreCase(currentAppPackageName)) {
						// Don't include our app in the list

						if (listOfProcesses == null) {
							// This will hold all processes and their infos
							listOfProcesses = new ArrayList<SingleProcess>();
						}

						try {
							// Get all the information the system has on this
							// package
							ai = pm.getApplicationInfo(pid.processName, 0);
						} catch (NameNotFoundException e) {
							// Can't get app details
							log(e.getMessage());
							ai = null;
						}

						if (ai != null) {
							float appMemoryUsageMB = 0;
							try {
								// Calculate total memory used by the app
								float appPssUsage = am
										.getProcessMemoryInfo(new int[] { pid.pid })[0]
										.getTotalPss();

								float appPssUsagePercent = (appPssUsage * 100)
										/ totalPss;
								appMemoryUsageMB = appPssUsagePercent
										* totalTakenRam / 100;
							} catch (Exception e) {
								log("Unable to get memory usage for "
										+ ai.packageName);
								log(e.getMessage());
							}
							// Get the localized app name
							final String appName = (String) pm
									.getApplicationLabel(ai);
							// Get app icon
							final Drawable appIcon = pm.getApplicationIcon(ai);
							// Is the app in the do-not-kill list
							final boolean isWhitelisted = excluded_list
									.getBoolean(ai.packageName, false);

							currentProcess = new SingleProcess(ai, appName,
									isWhitelisted, appIcon, appMemoryUsageMB);

							// Add the app info to the array adapter
							listOfProcesses.add(currentProcess);

							counter++;
							if (counter == publishEvery) {
								counter = 0;
								this.publishProgress((Void) null);
							}
						}
					}

				}
				return null;
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);

			updateList();
		}

		@Override
		protected void onPostExecute(Void v) {
			updateInProgress = false;

			if (!fragmentVisible) {
				return;
			}

			// One last update of the ListView, just in case we have elements
			// leftover from doInBackground
			updateList();

			final Handler h = new Handler();
			final Runnable r = new Runnable() {
				public void run() {
					setupListView();
				}
			};

			// Update list every 20 seconds
			h.postDelayed(r, 30000);

		}

		private void updateList() {

			if (!fragmentVisible)
				return;

			// Remember scrolled position
			final ListView lv = (ListView) getView().findViewById(
					R.id.lvRunningProcesses);
			final TextView tvPlaceholder = (TextView) getView().findViewById(
					R.id.tvRunningProcessesPlaceholder);

			if (lv == null) {
				// Something went wrong. Try to refresh the list immediately
				setupListView();
				return;
			}

			final int index = lv.getFirstVisiblePosition();
			final View v = lv.getChildAt(0);
			final int top = (v == null) ? 0 : v.getTop();

			if (!lvAdapterSet) {
				lv.setAdapter(adapter);
				lvAdapterSet = true;
			}

			if (listOfProcesses != null && listOfProcesses.size() > 0) {

				ArrayList<SingleProcess> copy = new ArrayList<SingleProcess>(
						listOfProcesses);
				listOfProcesses.clear();

				for (SingleProcess process : copy) {
					adapter.add(process);
				}
			}

			tvPlaceholder.setVisibility(View.GONE);
			lv.setVisibility(View.VISIBLE);

			// Restore scroll position
			try {
				lv.setSelectionFromTop(index, top);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Start an AsyncTask to update the list
	 */
	private void setupListView() {
		if (fragmentVisible) {
			DDebug.log(getClass().toString(), "Updating list of running apps");

			if (!updateInProgress)
				new listUpdater();
		}
	}

}
