package com.hasmobi.rambo;

import java.util.ArrayList;
import java.util.List;

import com.hasmobi.rambo.utils.ActiveProcessAdapter;
import com.hasmobi.rambo.utils.FeedbackManager;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.SingleProcess;
import com.hasmobi.rambo.utils.Values;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.app.ActionBar;
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
import android.content.res.Resources.NotFoundException;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
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

public class ProcessesActivity extends ListActivity {

	List<SingleProcess> listOfProcesses;

	Context c;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);

		setContentView(R.layout.activity_running_processes);

		init();
	}

	protected void init() {
		// Hide the actionbar
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			getActionBar().hide();
		}

		// Get the Activity context
		c = getBaseContext();

		// Temporarily show "Loading..." while the list is loading
		setListAdapter(new ArrayAdapter<String>(c,
				android.R.layout.simple_list_item_1,
				new String[] { res(R.string.loading) }));

		setupListView();

		setupFonts();

		setupRefreshButtonListener();
	}

	/**
	 * Apply custom fonts
	 */
	protected void setupFonts() {
		final TextView appTitle = (TextView) findViewById(R.id.appTitle);
		final Typeface face = Typeface.createFromAsset(getAssets(),
				"sttransmission_800_extrabold.otf");
		appTitle.setTypeface(face);
	}

	/**
	 * Click listener to update running processes when refresh button is clicked
	 */
	private void setupRefreshButtonListener() {
		// An image/button that will refresh the running processes list
		final ImageView img = (ImageView) findViewById(R.id.bRefreshProcesses);
		img.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setupListView();
			}
		});
	}

	/**
	 * A process name was clicked, show dialog with options
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (Values.DEBUG_MODE) {
			log("Packages size: " + listOfProcesses.size());
			log("Clicked position: " + (position + 1));
		}

		// If running processes count is larger or equal to the clicked position
		if (listOfProcesses.size() > 0
				&& listOfProcesses.size() >= (position + 1)) {
			final PackageManager pm = c.getPackageManager();

			// Get full details on the given process
			final SingleProcess sp = listOfProcesses.get(position);

			final String selectedAppName = sp.name;
			final String selectedPackageName = sp.appInfo.packageName;

			// Get all whitelisted apps from preferences
			final SharedPreferences excludedPrefs = getSharedPreferences(
					Values.EXCLUDED_LIST_FILE, 0);

			boolean isAppWhitelisted = false;
			try {
				isAppWhitelisted = excludedPrefs.getBoolean(
						selectedPackageName, false);
			} catch (ClassCastException e) {
				log("Can't retrive app whitelisted info for "
						+ selectedPackageName + ". Preference not a boolean.");
				log(e.getMessage());
			}

			// Dialog to hold a list of actions available for this process
			final Dialog d = new Dialog(this);
			d.setTitle(selectedAppName);
			d.setContentView(R.layout.dialog_process_selected);

			// When one of the dialog actions is picked
			ListView actionsList = (ListView) d
					.findViewById(R.id.listProcessActions);
			actionsList.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> adapter, View v,
						int position, long id) {
					switch (position) {
					case 0:
						// Switch to app selected
						try {
							startActivity(pm
									.getLaunchIntentForPackage(selectedPackageName));
						} catch (Exception e) {
							toast(res(R.string.background_only_app));
							if (Values.DEBUG_MODE) {
								log("Can't find launch intent for package "
										+ selectedPackageName);
							}
						}
						break;
					case 1:
						// Kill app selected
						if (excludedPrefs
								.getBoolean(selectedPackageName, false)) {
							// App in whitelist, do nothing
							toast(res(R.string.cant_kill_whitelisted_app));
						} else {
							// App not in whitelist, kill it
							RamManager ramManager = new RamManager(c);
							ramManager.killPackage(selectedPackageName);

							// Notify user that the app is killed
							toast(res(R.string.app_killed));
						}

						setupListView();
						break;
					case 2:
						// Whitelist/Blacklist selected. If the app is
						// whitelisted, we remove it from whitelist. If
						// it is not in whitelist, we add it there.
						Editor excludedEditor = excludedPrefs.edit();
						if (excludedPrefs
								.getBoolean(selectedPackageName, false)) {
							// Remove app from Whitelist
							excludedEditor.remove(selectedPackageName);
							toast(res(R.string.app_whitelist_removed));
						} else {
							// Add app to Whitelist
							excludedEditor
									.putBoolean(selectedPackageName, true);
							toast(res(R.string.app_whitelisted));
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
									Uri.parse("package:" + selectedPackageName)));
						} catch (Exception e) {
							toast("Unable to open App Info screen");
						}
						break;
					}
					// Always close the dialog after choosing any option
					d.dismiss();
				}

			});

			// Make to list the dialog options before showing them
			ArrayList<String> dialogOptions = new ArrayList<String>();

			Resources res = getResources();

			// First dialog option - Switch to this app
			dialogOptions.add(res.getString(R.string.switch_to));

			// Second dialog option - Kill app if not in whitelist
			dialogOptions.add(res.getString(R.string.kill_app));

			// Third dialog option - Add to whitelist if not already, remove
			// from whitelist if already whitelisted
			dialogOptions.add(isAppWhitelisted ? res
					.getString(R.string.remove_from_whitelist) : res
					.getString(R.string.add_to_whitelist));

			// Fourth dialog option - "Open AppInfo screen"
			dialogOptions.add(res.getString(R.string.app_info));

			// Add the actions to the dialog
			ArrayAdapter<String> actionsAdapter = new ArrayAdapter<String>(c,
					android.R.layout.simple_list_item_1, android.R.id.text1,
					dialogOptions);
			actionsList.setAdapter(actionsAdapter);

			// Finally, show the dialog
			d.show();
		} else {
			// Something went wrong, fail safe
			toast(res(R.string.process_not_found));
		}

		super.onListItemClick(l, v, position, id);
	}

	private void toast(String message) {
		Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
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
			toast(res(R.string.updating));
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
			final SharedPreferences excluded_list = getSharedPreferences(
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
				new listUpdater().execute();
				this.cancel(true);
				return null;
			} else {
				// There are some running processes
				for (RunningAppProcessInfo pid : runningProcesses) {
					if (Values.DEBUG_MODE) {
						log("PID: " + pid.pid + " - Process: "
								+ pid.processName);
					}
					ai = null; // Fail safe
					try {
						// Get all the information the system has on this app
						ai = pm.getApplicationInfo(pid.processName, 0);
					} catch (NameNotFoundException e) {
						// Can't get app details
						log(e.getMessage());
						ai = null;
					}

					if (ai != null) {
						int totalMemoryUsage = 0;
						try {
							final MemoryInfo memoryInfoArray = am
									.getProcessMemoryInfo(new int[] { pid.pid })[0];
							// Calculate total memory used by the app
							totalMemoryUsage = memoryInfoArray.nativePss
									+ memoryInfoArray.nativePrivateDirty
									+ memoryInfoArray.nativeSharedDirty;
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
				if (Values.DEBUG_MODE) {
					log("End of active processes list");
					log("Currently active processes: " + listOfProcesses.size());
				}
				return null;
			}
		}

		@Override
		protected void onPostExecute(Void r) {
			// Remember scrolled position
			final ListView thisList = getListView();
			final int index = thisList.getFirstVisiblePosition();
			final View v = thisList.getChildAt(0);
			final int top = (v == null) ? 0 : v.getTop();

			if (listOfProcesses.size() == 0) {
				toast(res(R.string.no_running_processes));
			} else {
				// Populate the list
				setListAdapter(new ActiveProcessAdapter(ProcessesActivity.this,
						listOfProcesses));

				// Restore scroll position
				thisList.setSelectionFromTop(index, top);

				if (Values.DEBUG_MODE) {
					log("List updated");
				}
				toast(res(R.string.list_updated));
			}

		}

	}

	/**
	 * Get a string resource by given ID
	 * 
	 * @param int resID - the ID of the resource
	 * @return String - The found string or blank string if not found
	 */
	private String res(int resID) {
		String found = "";
		try {
			found = getResources().getString(resID);
		} catch (NotFoundException e) {

		}
		return found;
	}

	private void log(String s) {
		Log.d(Values.DEBUG_TAG, s);
	}

	/**
	 * Start an AsyncTask to update the list
	 */
	private void setupListView() {
		new listUpdater().execute();
	}

	/**
	 * Go to the start activity
	 */
	public void goHome(View v) {
		Intent i = new Intent(c, StartActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		FeedbackManager fm = new FeedbackManager(c);

		switch (item.getItemId()) {
		case R.id.menuSettings:
			Intent i = new Intent(c, ConfigActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(i);
			break;
		case R.id.menuUpdateApp:
			fm.goToGooglePlay();
			break;
		case R.id.menuFeedback:
			fm.feedbackDialog();
			break;
		case R.id.menuQuit:
			finish();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

}
