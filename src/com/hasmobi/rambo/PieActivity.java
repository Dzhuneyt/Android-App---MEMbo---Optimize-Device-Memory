package com.hasmobi.rambo;

import com.hasmobi.rambo.utils.PieView;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.Values;

import android.app.Activity;
import android.app.ActivityManager.MemoryInfo;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PieActivity extends Activity implements OnClickListener {

	long totalRam = 0, freeRam = 0;

	private PieView pie;

	Context context;

	// SharedPreferences prefs;

	private Handler handler;
	private Runnable r;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		try {
			getActionBar().hide();
		} catch (Exception e) {
			log("Can't hide action bar");
			log(e.getMessage());
		}

		context = getBaseContext();

		setupInBackground();
	}

	private void setupInBackground() {
		new Thread() {
			@Override
			public void run() {
				fonts();
				setPiechart();
			}
		}.start();

		startUpdating();

		appStartLogger();
	}

	public void openSettings(View v) {
		Intent i = new Intent(context, ConfigActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}

	private void remindRateDialog() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		final Dialog remindDialog = new Dialog(this);
		remindDialog.setTitle(R.string.remind_rate_dialog_title);
		remindDialog.setContentView(R.layout.dialog_remind_to_rate);
		TextView tvStartCount = (TextView) remindDialog
				.findViewById(R.id.tvRemindStartCount);
		String dialogContents = res(R.string.remind_rate_dialog_content);
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

					goToGooglePlay();
					break;
				}
			}
		}

		bOpenMarket.setOnClickListener(new ButtonListener());
		tvHidePermanently.setOnClickListener(new ButtonListener());
		remindDialog.show();

	}

	private void goToGooglePlay() {
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
								+ getApplicationContext().getPackageName())));
			} catch (Exception ex) {
				// Can't even open regular URLs in browser
				Log.d(Values.DEBUG_TAG, "Can't start browser");
			}
		}
	}

	// Show a Welcome dialog with instructions
	private void welcomeDialog() {
		final Dialog d = new Dialog(this);
		d.setTitle(res(R.string.welcome_dialog_title));
		d.setContentView(R.layout.dialog_welcome);
		d.show();

		// Close the dialog on Close button click
		final Button bClose = (Button) d.findViewById(R.id.bClose);
		bClose.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				d.dismiss();
			}
		});
	}

	/**
	 * Some utility checks and actions on each app start
	 */
	private void appStartLogger() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor prefsEditor = prefs.edit();

		// Get how much times the app is started
		int appStartCount = 1;
		boolean appAlreadyRated = false;

		try {
			appStartCount = (int) prefs.getInt("app_start_count", 1);
		} catch (ClassCastException e) {
		} finally {
			if (appStartCount == 1) {
				appFirstStart();
			} else {
				Toast.makeText(context,
						res(R.string.tap_on_pie_for_running_processes),
						Toast.LENGTH_LONG).show();
			}
			log("App started " + appStartCount + " times");
		}

		try {
			appAlreadyRated = prefs.getBoolean("already_rated_app", false);
		} catch (ClassCastException e) {
		} finally {
			if ((appStartCount == 15 || appStartCount % 50 == 0)
					&& !appAlreadyRated)
				remindRateDialog();
		}

		// Remind the user to rate for the app in Google Play if the app is
		// started exactly 15 times and every 50th start. (unless
		// "Don't show this anymore" is choosed)

		// Increase app start count by one
		prefsEditor.putInt("app_start_count", appStartCount + 1);
		prefsEditor.commit();
	}

	private void appFirstStart() {
		// First start instructions on how to work with this app
		welcomeDialog();

		// Initial core apps whitelist
		final String[] defaultExcluded = { "system_process",
				"com.hasmobi.rambo", "com.android.phone",
				"com.android.systemui", "android.process.acore",
				"com.android.launcher" };
		final SharedPreferences.Editor excludedList = getSharedPreferences(
				"excluded_list", 0).edit();
		for (int i = 0; i + 1 < defaultExcluded.length; i++) {
			// Put them in SharedPreferences
			excludedList.putBoolean(defaultExcluded[i], true);
		}
		excludedList.commit();
		log("Default exclude list populated");
	}

	/**
	 * Get a resource from the package by a specified Resource ID
	 * 
	 * @param resID
	 *            - the resource ID
	 * @return String - the string that was retrieved for the supplied ID or a
	 *         blank string if not found
	 */
	private String res(int resID) {
		String found = "";
		try {
			found = getResources().getString(resID);
		} catch (NotFoundException e) {
			log("Resource with ID " + resID + " not found.");
		}
		return found;
	}

	@Override
	protected void onStart() {
		startUpdating();
		log("Ram updating renewed. (onStart)");

		super.onStart();
	}

	@Override
	protected void onResume() {
		startUpdating();
		log("Ram updating renewed. (onResume)");

		super.onResume();
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

		final int updateInterval = getPieUpdateInterval();

		handler = new Handler();
		r = new Runnable() {
			public void run() {
				new freeRamUpdater().execute();
				handler.postDelayed(r, updateInterval);
			}
		};
		handler.post(r);
	}

	/**
	 * Gets the pie update interval from preferences in milliseconds
	 * 
	 * @return int - milliseconds
	 */
	private int getPieUpdateInterval() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		int _updateIntervalSeconds;
		try {
			_updateIntervalSeconds = Integer.valueOf(prefs.getString(
					"pie_update_interval", "1"));
		} catch (NumberFormatException e) {
			_updateIntervalSeconds = 1;
		}
		int updateIntervalMillis = _updateIntervalSeconds * 1000;
		return updateIntervalMillis;
	}

	@Override
	protected void onPause() {
		try {
			handler.removeCallbacks(r);
			log("Ram updating stopped. (onPause)");
		} catch (NullPointerException e) {

		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		try {
			handler.removeCallbacks(r);
			log("Ram updating stopped. (onStop)");
		} catch (NullPointerException e) {

		}
		super.onStop();
	}

	/**
	 * Feedback was chosen from the options menu. Opens the default email client
	 * and populates app version and recepient's email.
	 */
	private void feedbackDialog() {
		String versionName = "n/a";
		int versionCode = 0;
		try {
			final PackageInfo v = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			versionName = v.versionName;
			versionCode = v.versionCode;
		} catch (NameNotFoundException e) {
			log("Can't find app version name and code for feedback.");
		}
		final String receivers[] = { Values.FEEDBACK_EMAIL };
		final String emailSubject = "Feedback for RAMbo";
		final String emailBody = "App version " + versionName + " ("
				+ versionCode + ")\n";
		final Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SEND);
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receivers);
		emailIntent.setType("plain/text");
		emailIntent
				.putExtra(android.content.Intent.EXTRA_SUBJECT, emailSubject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);
		try {
			startActivity(Intent.createChooser(emailIntent, getResources()
					.getString(R.string.feedback)));
		} catch (Exception e) {
			toast(res(R.string.no_email_app));
			log("Can't find email client for feedback");
		}
	}

	/**
	 * Apply custom fonts
	 */
	private void fonts() {
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
			log(e.getMessage());
		}
	}

	/**
	 * Setup the Pie chart
	 */
	private void setPiechart() {
		final LinearLayout pieContainer = (LinearLayout) findViewById(R.id.pie_container_id);
		pie = new PieView(this);
		pie.setClickable(true);
		pie.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(context, ProcessesActivity.class));
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

	private void toast(String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
			startActivity(new Intent(this, ConfigActivity.class));
			break;
		case R.id.menuRunningProcesses:
			startActivity(new Intent(this, ProcessesActivity.class));
			break;
		case R.id.menuUpdateApp:
			goToGooglePlay();
			break;
		case R.id.menuFeedback:
			feedbackDialog();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	/**
	 * A class that gets the free and total RAM and sets them to the appropriate
	 * TextViews.
	 * 
	 * @author hasMobi.com
	 * 
	 */
	protected class freeRamUpdater extends AsyncTask<String, String, Void> {

		long availableMegs, percent = 100;

		@Override
		protected void onPreExecute() {
			// Reads the total available RAM on the device.
			// Only needed once. It's run once on the UI thread.
			readTotalRam();
		}

		@Override
		protected Void doInBackground(String... params) {
			if (!isCancelled()) {
				final RamManager ramManager = new RamManager(context);

				freeRam = ramManager.getFreeRam();
				percent = ((freeRam * 100) / totalRam);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			pie.setRam(totalRam, freeRam); // Also redraws the pie chart

			if (freeRam != 0 && totalRam != 0) {
				final TextView tvFree = (TextView) findViewById(R.id.tvFree);
				final TextView tvTaken = (TextView) findViewById(R.id.tvTaken);
				tvFree.setText(res(R.string.free) + " " + freeRam + " MB ("
						+ percent + "%)");
				tvTaken.setText(res(R.string.taken) + " "
						+ (totalRam - freeRam) + " MB (" + (100 - percent)
						+ "%)");
				log("Free and Total RAM set");
			} else {
				log("Free or Total RAM not set at the moment");
				new freeRamUpdater().execute();
			}

		}

		private void readTotalRam() {
			final RamManager ramManager = new RamManager(context);

			final TextView tvTotal = (TextView) findViewById(R.id.tvTotal);
			try {
				totalRam = ramManager.getTotalRam();
				tvTotal.setText(res(R.string.total) + " "
						+ String.valueOf(totalRam) + " MB");
			} catch (Exception e) {
				tvTotal.setText("Total RAM: Unable to find");
				log("Unable to find total RAM. See readTotalRam()");
			}
		}

	}

	public void onClick(View clicked) {
		final View v = clicked;
		switch (v.getId()) {
		case R.id.bOptimize:
			final RamManager ramManager = new RamManager(context);

			ramManager.killBgProcesses();

			((Button) v).setText(res(R.string.optimizing));
			// Simulate a background color change for 300ms
			v.setBackgroundColor(getResources().getColor(
					R.color.optimizeButtonBGclicked));
			v.postDelayed(new Runnable() {
				public void run() {
					v.setBackgroundColor(getResources().getColor(
							R.color.optimizeButtonBG));
					((Button) v).setText(res(R.string.quick_optimize));
				}
			}, 300);
			log("Optimization button clicked");
			break;
		case R.id.appTitle:
			final Intent i = new Intent(context, StartActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(i);
			finish();
			break;
		}
	}
}
