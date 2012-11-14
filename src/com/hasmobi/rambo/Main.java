package com.hasmobi.rambo;

import java.util.Map;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Main extends Activity {

	long totalRam = 0, freeRam = 0, percent = 100;

	LinearLayout pieContainer;
	private PieView pie;

	private AsyncTask<String, String, Void> memoryMonitor = null;

	Context context;

	SharedPreferences prefs;

	private static final int notificationID = 10001;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		getActionBar().hide();

		context = getBaseContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		setupInBackground();

		if (Values.DEBUG_MODE) {
			debugCurrentlyExcluded();
		}
	}

	final private void debugCurrentlyExcluded() {
		final SharedPreferences excludedList = context.getSharedPreferences(
				"excluded_list", 0);
		final Map<String, ?> appsToExclude = excludedList.getAll();
		log("CURRENTLY EXCLUDED APPS LIST START:");
		for (Map.Entry<String, ?> entry : appsToExclude.entrySet()) {
			if (entry.getValue().equals(true)) {
				log(entry.getKey());
			}

		}
		log("END OF CURRENTLY EXCLUDED APPS LIST");
	}

	private void setupInBackground() {
		new Thread() {
			@Override
			public void run() {
				setStyles();
				setPiechart();
				sendNotifiction();
			}
		}.start();

		TextView tvUpdateIntervalMessage = (TextView) findViewById(R.id.tvUpdateIntervals);
		int updateInterval = 1;
		try {
			updateInterval = Integer.valueOf(prefs.getString(
					"pie_update_interval", "1"));
			String set = getResources().getQuantityString(
					R.plurals.ram_update_intervals, updateInterval,
					updateInterval);
			tvUpdateIntervalMessage.setText(set);
		} catch (Exception e) {
			tvUpdateIntervalMessage.setVisibility(View.GONE);
			log("Can't find update interval. ");
			log("Update interval: " + updateInterval);
		}

		if (memoryMonitor == null) {
			memoryMonitor = new freeRamUpdater().executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR, "");
		} else {
			memoryMonitor.cancel(true);
			memoryMonitor = new freeRamUpdater().executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR, "");
		}

		appStartLogger();
	}

	private void remindRateDialog() {
		final Dialog remindDialog = new Dialog(this);
		remindDialog.setTitle(R.string.remind_rate_dialog_title);
		remindDialog.setContentView(R.layout.dialog_remind_to_rate);
		TextView tvStartCount = (TextView) remindDialog
				.findViewById(R.id.tvRemindStartCount);
		String dialogContents = getResources().getString(
				R.string.remind_rate_dialog_content);
		dialogContents = String.format(dialogContents,
				prefs.getInt("app_start_count", 1));
		tvStartCount.setText(dialogContents);
		Button bOpenMarket = (Button) remindDialog
				.findViewById(R.id.bOpenMarket);
		TextView tvHidePermanently = (TextView) remindDialog
				.findViewById(R.id.tvRemindHidePermanently);

		class ButtonListener implements OnClickListener {
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.tvRemindHidePermanently:
					// Close the dialog
					remindDialog.dismiss();

					// Remember option to never display the Rate Reminder dialog
					SharedPreferences.Editor prefs = PreferenceManager
							.getDefaultSharedPreferences(context).edit();
					prefs.putBoolean("already_rated_app", true);
					prefs.commit();
					break;
				case R.id.bOpenMarket:
					// Close the dialog
					remindDialog.dismiss();

					Uri uri = Uri.parse("market://details?id="
							+ getApplicationContext().getPackageName());
					Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
					try {
						// If Google Play app is installed
						startActivity(goToMarket);
					} catch (Exception e) {
						try {
							// Alternatively, open it in the browser
							startActivity(new Intent(
									Intent.ACTION_VIEW,
									Uri.parse("https://play.google.com/store/apps/details?id="
											+ getApplicationContext()
													.getPackageName())));
						} catch (Exception ex) {
							// Can't even open regular URLs in browser
							Log.d(Values.DEBUG_TAG, "Can't start browser");
						}
					}
					break;
				}
			}
		}

		bOpenMarket.setOnClickListener(new ButtonListener());
		tvHidePermanently.setOnClickListener(new ButtonListener());
		remindDialog.show();

	}

	// Show a Welcome dialog with instructions
	private void welcomeDialog() {
		final Dialog d = new Dialog(this);
		d.setTitle(getResources().getString(R.string.welcome_dialog_title));
		d.setContentView(R.layout.dialog_welcome);
		d.show();

		// Close the dialog on Close button click
		Button bClose = (Button) d.findViewById(R.id.bClose);
		bClose.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				d.dismiss();
			}
		});
	}

	private void appStartLogger() {
		if (Values.DEBUG_MODE) {
			welcomeDialog(); // TODO remove this
		}
		SharedPreferences.Editor prefsEditor = prefs.edit();
		int appStartCount = prefs.getInt("app_start_count", 1);
		Log.d(Values.DEBUG_TAG, "App started " + appStartCount + " times");
		if (appStartCount == 1) {
			// Initial core apps whitelist
			String[] defaultExcluded = { "system_process", "com.hasmobi.rambo",
					"com.android.phone", "com.android.systemui",
					"android.process.acore", "com.android.launcher" };
			SharedPreferences.Editor excludedList = getSharedPreferences(
					"excluded_list", 0).edit();
			for (int i = 0; i + 1 < defaultExcluded.length; i++) {
				// Put them in SharedPreferences
				excludedList.putBoolean(defaultExcluded[i], true);
			}
			excludedList.commit();
			Log.d(Values.DEBUG_TAG, "Default exclude list populated");

			welcomeDialog(); // First start instructions
		}

		// If the app is started 15 times (or every 50th time from there on) and
		// the user has not choosed to permanently hide the rate reminder dialog
		// - show it
		if ((appStartCount == 15 || appStartCount % 50 == 0)
				&& prefs.getBoolean("already_rated_app", false) == false) {
			// Remind the user to give 5 stars on the 15th start and every 50th
			// start later on
			remindRateDialog();
		}

		// Increase app start count by one
		prefsEditor.putInt("app_start_count", appStartCount + 1);
		prefsEditor.commit();
	}

	@SuppressWarnings("deprecation")
	private void sendNotifiction() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		boolean displayIcon = prefs.getBoolean("notification_icon", false);

		if (displayIcon) {
			Log.d(Values.DEBUG_TAG, "Notification icon [ACTIVATE]");
			Notification notification = new Notification(
					R.drawable.notify_icon, getResources().getString(
							R.string.app_name), System.currentTimeMillis());
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.flags |= Notification.FLAG_NO_CLEAR;

			Intent clearMemoryIntent = new Intent(this, BroadcastManager.class);
			clearMemoryIntent.setAction(Values.CLEAR_RAM);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
					clearMemoryIntent, 0);

			notification.setLatestEventInfo(getBaseContext(), getResources()
					.getString(R.string.notification_title), getResources()
					.getString(R.string.notification_text), pendingIntent);
			notificationManager.notify(notificationID, notification);
		} else {
			notificationManager.cancel(notificationID);
			Log.d(Values.DEBUG_TAG, "Notification icon [CANCEL]");
		}

	}

	@Override
	protected void onStart() {
		if (memoryMonitor == null) {
			Log.d(Values.DEBUG_TAG, "Ram updating renewed. (onStart)");
			memoryMonitor = new freeRamUpdater().executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR, "");
		}

		super.onStart();
	}

	@Override
	protected void onResume() {
		if (memoryMonitor == null) {
			Log.d(Values.DEBUG_TAG, "Ram updating renewed. (onResume)");
			memoryMonitor = new freeRamUpdater().executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR, "");
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (memoryMonitor != null) {
			Log.d(Values.DEBUG_TAG, "Ram updating stopped. (onPause)");
			memoryMonitor.cancel(true);
			memoryMonitor = null;
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		if (memoryMonitor != null) {
			Log.d(Values.DEBUG_TAG, "Ram updating stopped. (onStop)");
			memoryMonitor.cancel(true);
			memoryMonitor = null;
		}
		super.onStop();
	}

	public class freeRamUpdater extends AsyncTask<String, String, Void> {

		TextView tvFree, tvTaken;
		MemoryInfo mi;
		long availableMegs;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			tvFree = (TextView) findViewById(R.id.tvFree);
			tvTaken = (TextView) findViewById(R.id.tvTaken);
			mi = new MemoryInfo();

			// Reads the total available RAM on the device.
			// Only needed once. It's run once on the UI thread.
			readTotalRam();
		}

		@Override
		protected Void doInBackground(String... params) {
			if (isCancelled())
				return null;

			while (!isCancelled()) {

				Integer updateInterval = 1;
				try {
					// Get the pie update interval from preferences
					updateInterval = Integer.parseInt(prefs.getString(
							"pie_update_interval", "1"));
				} catch (Exception e) {
					Log.d(Values.DEBUG_TAG,
							"Can't get update interval from preferences. Reason: "
									+ e.getMessage());
				}

				ActivityManager am = (ActivityManager) context
						.getSystemService(Context.ACTIVITY_SERVICE);
				am.getMemoryInfo(mi);
				availableMegs = (mi.availMem / 1048576L);
				freeRam = availableMegs;
				percent = ((freeRam * 100) / totalRam);

				// Push progress to UI
				publishProgress(String.valueOf(availableMegs),
						String.valueOf(percent));

				try {
					Thread.sleep(1000 * updateInterval);
				} catch (InterruptedException e) {
					Log.d(Values.DEBUG_TAG,
							"Can't sleep. Reason: " + e.getMessage());
					return null;
				}

			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);

			pie.setRam(totalRam, freeRam); // Also redraws the pie chart

			if (values[0] != null && values[1] != null) {
				tvFree.setText("Free: " + values[0] + " MB (" + values[1]
						+ "%)");
				tvTaken.setText("Taken: " + (totalRam - freeRam) + " MB");
			} else {
				Log.d(Values.DEBUG_TAG,
						"Free or Total RAM not set at the moment");
			}
		}

		private void readTotalRam() {
			RamManager ramManager = new RamManager(context);

			final TextView tvTotal = (TextView) findViewById(R.id.tvTotal);
			try {
				totalRam = ramManager.getTotalRam();
				tvTotal.setText("Total RAM: " + String.valueOf(totalRam)
						+ " MB");
				Log.d(Values.DEBUG_TAG, "Total RAM found and set.");
			} catch (Exception e) {
				tvTotal.setText("Total RAM: Unable to find");
				Log.d(Values.DEBUG_TAG,
						"Unable to find total RAM. See readTotalRam()");
			}
		}

	}

	// Optimize button clicked
	public void optimizeHandler(final View v) {

		RamManager ramManager = new RamManager(getBaseContext());

		ramManager.killBgProcesses();

		((Button) v).setText(getResources().getString(R.string.optimizing));
		// Simulate a background color change for 300ms
		v.setBackgroundColor(getResources().getColor(
				R.color.optimizeButtonBGclicked));
		v.postDelayed(new Runnable() {
			public void run() {
				v.setBackgroundColor(getResources().getColor(
						R.color.optimizeButtonBG));
				((Button) v).setText(getResources().getString(
						R.string.quick_optimize));
			}
		}, 300);
		Log.d(Values.DEBUG_TAG, "Optimization button clicked");
	}

	private void setStyles() {
		try {
			final TextView tv = (TextView) findViewById(R.id.appTitle);
			final Button bOptimize = (Button) findViewById(R.id.bOptimize);
			final Typeface face = Typeface.createFromAsset(getAssets(),
					"sttransmission_800_extrabold.otf");
			tv.setTypeface(face);
			bOptimize.setTypeface(face);
			log("Custom styles applied");
		} catch (Exception e) {
			log("Unable to apply custom fonts");
		}
	}

	// Setup the Pie Chart
	private void setPiechart() {
		pieContainer = (LinearLayout) findViewById(R.id.pie_container_id);
		pie = new PieView(this);
		pie.setClickable(true);
		pie.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent running = new Intent(context, RunningProcesses.class);
				startActivity(running);
				log("Pie clicked");
			}
		});
		pieContainer.removeAllViews();
		pieContainer.addView(pie);
		log("Pie setup completed");
	}

	private void log(String s) {
		Log.d(Values.DEBUG_TAG, s);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menuSettings:
			log("Opening settings activity");
			Intent settingsIntent = new Intent(this, AppSettings.class);
			startActivity(settingsIntent);
			break;
		case R.id.menuRunningProcesses:
			log("Opening running processes activity");
			Intent runningProcessesIntent = new Intent(this,
					RunningProcesses.class);
			startActivity(runningProcessesIntent);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

}
