package com.hasmobi.rambo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
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

import com.hasmobi.rambo.utils.ActiveProcessAdapter;
import com.hasmobi.rambo.utils.Debugger;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.SingleProcess;
import com.hasmobi.rambo.utils.Values;

public class FragmentRunningApps extends DFragment {

	List<SingleProcess> listOfProcesses;

	Context c;

	boolean fragmentVisible = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		c = getActivity().getBaseContext();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.xml.fragment_running_processes, null);

		return v;
	}

	@Override
	public void onResume() {
		fragmentVisible = true;

		init();

		super.onResume();
	}

	@Override
	public void onPause() {
		fragmentVisible = false;

		super.onPause();
	}

	protected void init() {
		setupListView();
	}

	/**
	 * An Async class that updates the running processes list, called via
	 * setupListView()
	 * 
	 * @author hasMobi.com
	 * 
	 */
	class listUpdater extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			if (Values.DEBUG_MODE) {
				log("List updating");
			}
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... v) {
			if (isCancelled()) {
				return null;
			}

			// Get the excluded apps list
			final SharedPreferences excluded_list = c.getSharedPreferences(
					"excluded_list", 0);
			ActivityManager am = (ActivityManager) c
					.getSystemService(Context.ACTIVITY_SERVICE);
			PackageManager pm = c.getPackageManager();

			// This will hold all processes and their infos
			listOfProcesses = new ArrayList<SingleProcess>();

			ApplicationInfo ai = null;

			if (Values.DEBUG_MODE) {
				log("Going through all running processes...");
			}

			List<RunningAppProcessInfo> runningProcesses = am
					.getRunningAppProcesses();
			if (runningProcesses.size() == 0) {
				// Something went wrong, start a new AsyncTask
				if (Values.DEBUG_MODE) {
					log("No running processes found. Restarting AsyncTask.");
				}
				setupListView();
				this.cancel(true);
				return null;
			} else {
				// There are some running processes

				RamManager rm = new RamManager(c);
				int totalTakenRam = (rm.getTotalRam() - rm.getFreeRam());

				// Get current package and exclude it from the list
				String currentAppPackageName = "";
				try {
					currentAppPackageName = c.getPackageName();
				} catch (Exception e) {
				}

				// First get the total Pss assigned to all running processes
				float totalPss = 0;
				for (RunningAppProcessInfo pid : runningProcesses) {
					if (!pid.processName
							.equalsIgnoreCase(currentAppPackageName)) {
						final MemoryInfo memoryInfoArray = am
								.getProcessMemoryInfo(new int[] { pid.pid })[0];
						totalPss = totalPss + memoryInfoArray.getTotalPss();
					}
				}

				// Then loop through all running processes, get info for each
				// and weigh its individual PSS to the total PSS. This way, we
				// get a realistic estimate of what portion of the total used
				// RAM this app takes.
				for (RunningAppProcessInfo pid : runningProcesses) {

					ai = null; // Fail safe
					try {
						// Get all the information the system has on this app
						ai = pm.getApplicationInfo(pid.processName, 0);
					} catch (NameNotFoundException e) {
						// Can't get app details
						log(e.getMessage());
						ai = null;
					}

					if (ai != null
							&& !pid.processName
									.equalsIgnoreCase(currentAppPackageName)) {
						float totalMemoryUsage = 0;
						try {
							final MemoryInfo memoryInfoArray = am
									.getProcessMemoryInfo(new int[] { pid.pid })[0];
							// Calculate total memory used by the app
							float appPss = memoryInfoArray.getTotalPss();

							float weightedPss = (appPss * 100) / totalPss;
							totalMemoryUsage = weightedPss * totalTakenRam
									/ 100;
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
						final boolean isWhitelisted = excluded_list.getBoolean(
								ai.packageName, false);

						// Add the app info to the array adapter
						listOfProcesses.add(new SingleProcess(ai, appName,
								isWhitelisted, appIcon, totalMemoryUsage));

					}

				}
				return null;
			}
		}

		@Override
		protected void onPostExecute(Void nothing) {
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

			if (listOfProcesses.size() > 0) {
				// Populate the actual ListView

				ActiveProcessAdapter adapter = new ActiveProcessAdapter(
						getActivity(), listOfProcesses);

				lv.setAdapter(adapter);

				tvPlaceholder.setVisibility(View.GONE);
				lv.setVisibility(View.VISIBLE);

				// Restore scroll position
				try {
					lv.setSelectionFromTop(index, top);
				} catch (Exception e) {

				}
			} else {
				Debugger.log("No running processes found");

			}

			Handler h = new Handler();
			Runnable r = new Runnable() {
				public void run() {
					setupListView();
				}
			};
			h.postDelayed(r, 5000);

		}
	}

	private void log(String s) {
		if (s != null && s.length() > 0)
			Debugger.log(s);
	}

	/**
	 * Start an AsyncTask to update the list
	 */
	private void setupListView() {
		if (fragmentVisible)
			new listUpdater().execute();
	}

}
