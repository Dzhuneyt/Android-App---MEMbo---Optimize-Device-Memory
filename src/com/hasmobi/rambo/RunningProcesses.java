package com.hasmobi.rambo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.os.Bundle;
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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RunningProcesses extends ListActivity {

	List<ApplicationInfo> packages = null;

	// Active process update interval in seconds
	private int listUpdateInterval = 3;

	PackageManager pm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_running_processes_new);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().hide();

		pm = getApplicationContext().getPackageManager();

		// Refresh the active process list every N seconds
		final Handler handler = new Handler();
		Runnable runnable = new Runnable() {
			public void run() {
				setupListView();
				handler.postDelayed(this, listUpdateInterval * 3000);
			}
		};
		runnable.run();

		final TextView appTitle = (TextView) findViewById(R.id.appTitle);
		final Typeface face = Typeface.createFromAsset(getAssets(),
				"sttransmission_800_extrabold.otf");
		appTitle.setTypeface(face);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (packages != null) {
			// All the info the system has on the clicked application
			final ApplicationInfo fullPackageInfo = packages.get(position);
			final String selectedAppName = (String) pm
					.getApplicationLabel(fullPackageInfo);
			final String selectedPackageName = fullPackageInfo.packageName;

			// Setup the dialog
			final Dialog dialog = new Dialog(this);
			dialog.setTitle(selectedAppName);
			dialog.setContentView(R.layout.dialog_process_selected);

			// Get all whitelisted apps from preferences
			final SharedPreferences excludedPrefs = getSharedPreferences(
					Values.EXCLUDED_LIST_FILE, 0);
			final Map<String, ?> excludedAppsList = excludedPrefs.getAll();

			// Listener when dialog action is picked
			ListView listProcessActions = (ListView) dialog
					.findViewById(R.id.listProcessActions);
			listProcessActions
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
								RamManager ramManager = new RamManager(
										getBaseContext());
								ramManager.killPackage(selectedPackageName);

								// Notify user that the app is killed
								toast(getResources().getString(
										R.string.app_killed));

								// Force reload of the list immediately
								setupListView();
								break;
							case 2:
								// Whitelist/Blacklist selected. If the app is
								// whitelisted, we remove it from whitelist. If
								// it is not in whitelist, we add it there.
								Editor excludedEditor = excludedPrefs.edit();
								if (excludedAppsList
										.containsKey(selectedPackageName)) {
									excludedEditor.remove(selectedPackageName);
									toast(getResources().getString(
											R.string.app_whitelist_removed));
								} else {
									excludedEditor.putBoolean(
											selectedPackageName, true);
									toast(getResources().getString(
											R.string.app_whitelisted));
								}
								excludedEditor.commit();
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

			dialogOptions.add(getResources().getString(R.string.switch_to));
			dialogOptions.add(getResources().getString(R.string.kill_app));

			if (excludedAppsList.containsKey(selectedPackageName)) {
				dialogOptions.add(getResources().getString(
						R.string.remove_from_whitelist));
			} else {
				dialogOptions.add(getResources().getString(
						R.string.add_to_whitelist));
			}

			// Add option "Open AppInfo screen"
			dialogOptions.add(getResources().getString(R.string.app_info));

			// Show the actions in the dialog
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, android.R.id.text1,
					dialogOptions);
			listProcessActions.setAdapter(adapter);

			// Finally, show the dialog
			dialog.show();
		} else {
			// Something went wrong, fail safe
			toast("Process not found");
		}

		super.onListItemClick(l, v, position, id);
	}

	private void toast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}

	private void setupListView() {

		// Remember scroll position and restore to it later
		ListView thisList = getListView();
		int index = thisList.getFirstVisiblePosition();
		View v = thisList.getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();

		ActivityManager am = (ActivityManager) getBaseContext()
				.getSystemService(Context.ACTIVITY_SERVICE);

		packages = new ArrayList<ApplicationInfo>();

		ApplicationInfo ai = null;

		SharedPreferences excluded_list = getSharedPreferences("excluded_list",
				0);

		List<SingleProcess> listOfProcesses = new ArrayList<SingleProcess>();

		for (RunningAppProcessInfo pid : am.getRunningAppProcesses()) {
			try {
				ai = pm.getApplicationInfo(pid.processName, 0);
			} catch (final NameNotFoundException e) {
				e.printStackTrace();
				ai = null;
			} finally {
				if (ai != null) {
					String appName = (String) pm.getApplicationLabel(ai);
					Drawable appIcon = pm.getApplicationIcon(ai);
					boolean isWhitelisted = excluded_list.getBoolean(
							pid.processName, false);
					listOfProcesses.add(new SingleProcess(appName,
							isWhitelisted, appIcon));

					packages.add(ai);
					ai = null;
				}
			}
		}

		// Convert the list to an ArrayAdapter
		// ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		// android.R.layout.simple_list_item_1, android.R.id.text1,
		// packageNames);

		setListAdapter(new ActiveProcessAdapter(this, listOfProcesses));

		// Restore exact scroll position from memory
		thisList.setSelectionFromTop(index, top);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_running_processes_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// A class that holds each active process object for easier iteration
	public class Process {
		public Drawable icon;
		public String name;
		public String packageName;

		public Process() {
			super();
		}

		public Process(String name, Drawable icon) {
			this.icon = icon;
			this.name = name;
		}

		public Process(String packageName, String name, Drawable icon) {
			this.packageName = packageName;
			this.icon = icon;
			this.name = name;
		}
	}

}
