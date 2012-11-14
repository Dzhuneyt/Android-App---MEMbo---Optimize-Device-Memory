package com.hasmobi.rambo;

import java.util.ArrayList;
import java.util.List;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.os.Handler;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RunningProcesses extends ListActivity {

	List<ApplicationInfo> packages = null;
	List<SingleProcess> listOfProcesses;

	Context c;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_running_processes);

		init();
	}

	protected void init() {
		// Hide the actionbar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().hide();

		// Get the Activity context
		c = getBaseContext();

		// When nothing to display in the list, just show "Loading..." at first
		ArrayAdapter<String> loadingAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				new String[] { "Loading.." });
		setListAdapter(loadingAdapter);

		setupListView();

		setupFonts();

		setupRefreshButtonListener();
	}

	protected void setupFonts() {
		final TextView appTitle = (TextView) findViewById(R.id.appTitle);
		final Typeface face = Typeface.createFromAsset(getAssets(),
				"sttransmission_800_extrabold.otf");
		appTitle.setTypeface(face);
	}

	private void setupRefreshButtonListener() {
		// An image/button that will refresh the running processes list
		ImageView img = (ImageView) findViewById(R.id.bRefreshProcesses);
		img.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setupListView();
			}
		});
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (Values.DEBUG_MODE) {
			Log.d(Values.DEBUG_TAG, "Packages size: " + listOfProcesses.size());
			Log.d(Values.DEBUG_TAG, "Clicked position: " + (position + 1));
		}

		if (packages.isEmpty() == false && listOfProcesses.size() > 0
				&& listOfProcesses.size() >= (position + 1)) {
			final PackageManager pm = getApplicationContext()
					.getPackageManager();
			SingleProcess sp = listOfProcesses.get(position);
			// final ApplicationInfo fullPackageInfo = packages.get(position);
			// All the info the system has on the clicked application

			final String selectedAppName = sp.name;
			final String selectedPackageName = sp.appInfo.packageName;

			// Setup the dialog
			final Dialog dialog = new Dialog(this);
			dialog.setTitle(selectedAppName);
			dialog.setContentView(R.layout.dialog_process_selected);

			// Get all whitelisted apps from preferences
			final SharedPreferences excludedPrefs = getSharedPreferences(
					Values.EXCLUDED_LIST_FILE, 0);

			// Listener when dialog action is picked
			ListView singleProcessDialogActions = (ListView) dialog
					.findViewById(R.id.listProcessActions);
			singleProcessDialogActions
					.setOnItemClickListener(new OnItemClickListener() {

						public void onItemClick(AdapterView<?> adapter, View v,
								int position, long id) {
							switch (position) {
							case 0:
								// Switch to app selected
								Intent LaunchIntent = pm
										.getLaunchIntentForPackage(selectedPackageName);
								if (LaunchIntent != null) {
									startActivity(LaunchIntent);
								} else {
									toast(getResources().getString(
											R.string.background_only_app));
								}
								break;
							case 1:
								// Kill app selected
								if (excludedPrefs.getBoolean(
										selectedPackageName, false)) {
									// App not in whitelist, kill it
									RamManager ramManager = new RamManager(c);
									ramManager.killPackage(selectedPackageName);

									// Notify user that the app is killed
									toast(getResources().getString(
											R.string.app_killed));
								} else {
									// App in whitelist, do nothing
									toast(getResources().getString(
											R.string.cant_kill_whitelisted_app));
								}

								setupListView();
								break;
							case 2:
								// Whitelist/Blacklist selected. If the app is
								// whitelisted, we remove it from whitelist. If
								// it is not in whitelist, we add it there.
								Editor excludedEditor = excludedPrefs.edit();
								if (excludedPrefs.getBoolean(
										selectedPackageName, false)) {
									// Remove app from Whitelist
									excludedEditor.remove(selectedPackageName);
									toast(getResources().getString(
											R.string.app_whitelist_removed));
								} else {
									// Add app to Whitelist
									excludedEditor.putBoolean(
											selectedPackageName, true);
									toast(getResources().getString(
											R.string.app_whitelisted));
								}
								excludedEditor.commit();

								// Force reload of the list immediately
								setupListView();
								break;
							case 3:
								// Open the AppInfo screen, if supported
								try {
									startActivity(new Intent(
											android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
											Uri.parse("package:"
													+ selectedPackageName)));
								} catch (Exception e) {
									toast("Unable to open App Info screen");
								}
								break;
							}
							// Always close the dialog after choosing any option
							dialog.dismiss();
						}

					});

			// Make to list the dialog options before showing them
			ArrayList<String> dialogOptions = new ArrayList<String>();

			Resources res = getResources();

			dialogOptions.add(res.getString(R.string.switch_to));
			dialogOptions.add(res.getString(R.string.kill_app));

			if (excludedPrefs.getBoolean(selectedPackageName, false)) {
				// App is whitelisted
				dialogOptions
						.add(res.getString(R.string.remove_from_whitelist));
			} else {
				// App is not in whitelist
				dialogOptions.add(res.getString(R.string.add_to_whitelist));
			}

			// Add option "Open AppInfo screen"
			dialogOptions.add(res.getString(R.string.app_info));

			// Show the actions in the dialog
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, android.R.id.text1,
					dialogOptions);
			singleProcessDialogActions.setAdapter(adapter);

			// Finally, show the dialog
			dialog.show();
		} else {
			// Something went wrong, fail safe
			toast(getResources().getString(R.string.process_not_found));
		}

		super.onListItemClick(l, v, position, id);
	}

	private void toast(String message) {
		Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
	}

	class listUpdater extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			toast(getResources().getString(R.string.updating));
			if (Values.DEBUG_MODE) {
				Log.d(Values.DEBUG_TAG, "List updating");
			}
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... v) {
			if (isCancelled()) {
				return null;
			}

			// Get the excluded apps list
			final SharedPreferences excluded_list = getSharedPreferences(
					"excluded_list", 0);
			ActivityManager am = (ActivityManager) c
					.getSystemService(Context.ACTIVITY_SERVICE);
			PackageManager pm = c.getPackageManager();

			// This will hold all processes and their infos
			packages = new ArrayList<ApplicationInfo>();
			listOfProcesses = new ArrayList<SingleProcess>();

			ApplicationInfo ai = null;

			if (Values.DEBUG_MODE) {
				Log.d(Values.DEBUG_TAG,
						"Going through all running processes...");
			}

			List<RunningAppProcessInfo> runningProcesses = am
					.getRunningAppProcesses();
			if (runningProcesses.size() == 0) {
				// Something went wrong, start a new AsyncTask
				if (Values.DEBUG_MODE) {
					Log.d(Values.DEBUG_TAG,
							"No running processes found. Restarting AsyncTask.");
				}
				new listUpdater().execute();
				this.cancel(true);
				return null;
			} else {
				// There are some running processes
				for (RunningAppProcessInfo pid : runningProcesses) {
					if (Values.DEBUG_MODE) {
						Log.d(Values.DEBUG_TAG, "PID: " + pid.pid
								+ " - Process: " + pid.processName);
					}
					ai = null; // Fail safe
					try {
						// Get all the information the system has on this app
						ai = pm.getApplicationInfo(pid.processName, 0);
					} catch (NameNotFoundException e) {
						// Can't get app details
						Log.d(Values.DEBUG_TAG, e.getMessage());
						ai = null;
					}

					if (ai != null) {
						int totalMemoryUsage = 0;
						try {
							MemoryInfo memoryInfoArray = am
									.getProcessMemoryInfo(new int[] { pid.pid })[0];
							// Calculate total memory used by the app
							totalMemoryUsage = memoryInfoArray.nativePss
									+ memoryInfoArray.nativePrivateDirty
									+ memoryInfoArray.nativeSharedDirty;
						} catch (Exception e) {
							e.printStackTrace();
							Log.d(Values.DEBUG_TAG,
									"Unable to get memory usage for "
											+ ai.packageName);
						}
						// Get the localized app name
						String appName = (String) pm.getApplicationLabel(ai);
						// Get app icon
						Drawable appIcon = pm.getApplicationIcon(ai);
						// Is the app in the do-not-kill list
						boolean isWhitelisted = excluded_list.getBoolean(
								ai.packageName, false);

						// Add the app info to the array adapter
						listOfProcesses.add(new SingleProcess(ai, appName,
								isWhitelisted, appIcon, totalMemoryUsage));
						packages.add(ai);
					}

				}
				if (Values.DEBUG_MODE) {
					Log.d(Values.DEBUG_TAG, "End of active processes list");
					Log.d(Values.DEBUG_TAG, "Currently active processes: "
							+ listOfProcesses.size());
				}
				return null;
			}
		}

		@Override
		protected void onPostExecute(Void r) {
			// Remember scrolled position
			ListView thisList = getListView();
			int index = thisList.getFirstVisiblePosition();
			View v = thisList.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();

			if (listOfProcesses.size() == 0) {
				toast(getResources().getString(R.string.no_running_processes));
			} else {
				// Populate the list
				setListAdapter(new ActiveProcessAdapter(RunningProcesses.this,
						listOfProcesses));

				// Restore scroll position
				thisList.setSelectionFromTop(index, top);

				if (Values.DEBUG_MODE) {
					Log.d(Values.DEBUG_TAG, "List updated");
				}
				toast(getResources().getString(R.string.list_updated));
			}

		}

	}

	private void setupListView() {
		// Start an AsyncTask to update the list
		new listUpdater().execute();
	}

	public void goHome(View v) {
		Intent i = new Intent(c, Main.class);
		startActivity(i);
		// NavUtils.navigateUpFromSameTask(this);
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// NavUtils.navigateUpFromSameTask(this);
			Intent i = new Intent(c, Main.class);
			startActivity(i);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
