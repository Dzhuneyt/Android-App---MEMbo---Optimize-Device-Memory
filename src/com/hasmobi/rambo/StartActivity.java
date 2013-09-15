package com.hasmobi.rambo;

import com.hasmobi.rambo.utils.FeedbackManager;
import com.hasmobi.rambo.utils.Values;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StartActivity extends Activity {

	Context c;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		c = getBaseContext();

		setup();

		remindToRate();

		startAutoBoost();
	}

	private void startAutoBoost() {
		Intent autoBoostIntent = new Intent(c, BroadcastManager.class);
		autoBoostIntent.setAction(Values.ACTION_AUTOBOOST_ENABLE);
		PendingIntent pi = PendingIntent.getBroadcast(c, 0, autoBoostIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		try {
			pi.send();
		} catch (CanceledException e) {
			log("Can not start autobooster due to an exception.");
			log(e.getMessage());
		}

		log("autoboosting");

	}

	public void openMemoryManagement(View v) {
		Intent i = new Intent(c, PieActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}

	public void openRunningApps(View v) {
		Intent i = new Intent(c, ProcessesActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}

	public void openSettings(View v) {
		Intent i = new Intent(c, ConfigActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}

	public void openAbout(View v) {
		Intent i = new Intent(c, AboutActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}

	protected void setup() {
		if (getActionBar() != null) {
			getActionBar().hide();
		}

		setFonts();

		sendNotification();
	}

	protected void remindToRate() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(c);

		int appStartCount = 1;

		// Get the app start count from preferences
		appStartCount = prefs.getInt("app_start_count", 1);

		log("App started " + appStartCount + " times");

		// If the user has already rated the app, don't annoy him with the
		// reminder dialog over and over. Values.REMIND_RATE_EVERY_N_START is
		// how often (in app start counts) will he be reminded to rate the app.
		boolean appAlreadyRated = prefs.getBoolean("already_rated_app", false);
		if (Values.DEBUG_MODE) {
			log("Debug mode active. Show remind dialog.");
			showRemindRateDialog();

		} else {
			if (!appAlreadyRated) {
				log("App not rated yet");
				if ((appStartCount == 1 || appStartCount
						% Values.REMIND_RATE_EVERY_N_START == 0)) {
					showRemindRateDialog();
				}
			} else {
				log("App already rated. Don't show reminder dialog");
			}

		}

		// Log the new app start count for the next start
		final SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.putInt("app_start_count", appStartCount + 1);
		prefsEditor.commit();
	}

	private void showRemindRateDialog() {
		final Dialog remindDialog = new Dialog(this);
		remindDialog.setTitle(R.string.remind_rate_dialog_title);
		remindDialog.setContentView(R.layout.dialog_remind_to_rate);
		TextView tvStartCount = (TextView) remindDialog
				.findViewById(R.id.tvRemindStartCount);
		String dialogContents = res(R.string.remind_rate_dialog_content);
		tvStartCount.setText(dialogContents);
		Button bOpenMarket = (Button) remindDialog
				.findViewById(R.id.bOpenMarket);
		TextView tvHidePermanently = (TextView) remindDialog
				.findViewById(R.id.tvRemindHidePermanently);

		class ButtonListener implements OnClickListener {
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.tvRemindHidePermanently:
					// Remember option to never display the Rate Reminder dialog
					final SharedPreferences.Editor prefsEditor = PreferenceManager
							.getDefaultSharedPreferences(c).edit();
					prefsEditor.putBoolean("already_rated_app", true);
					prefsEditor.commit();
					break;
				case R.id.bOpenMarket:
					goToGooglePlay();
					break;
				}

				remindDialog.dismiss();
			}
		}

		// Setup the click listeners for the buttons inside the remind dialog
		bOpenMarket.setOnClickListener(new ButtonListener());
		tvHidePermanently.setOnClickListener(new ButtonListener());

		remindDialog.show();

	}

	private void goToGooglePlay() {
		Uri uri = Uri.parse("market://details?id=" + c.getPackageName());
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
								+ c.getPackageName())));
			} catch (Exception ex) {
				// Can't even open regular URLs in browser
				log("Can't start browser");
			}
		}
	}

	private void setFonts() {
		final TextView appTitle = (TextView) findViewById(R.id.appTitle);
		final Typeface face = Typeface.createFromAsset(getAssets(),
				"sttransmission_800_extrabold.otf");
		appTitle.setTypeface(face);

		TextView tvMemoryManagement = (TextView) findViewById(R.id.tvMemoryManagement);
		TextView tvRunningProcesses = (TextView) findViewById(R.id.tvRunningProcesses);
		TextView tvSettings = (TextView) findViewById(R.id.tvSettings);
		TextView tvAbout = (TextView) findViewById(R.id.tvAbout);

		tvMemoryManagement.setTypeface(face);
		tvRunningProcesses.setTypeface(face);
		tvSettings.setTypeface(face);
		tvAbout.setTypeface(face);
	}

	@Override
	protected void onResume() {
		sendNotification();
		super.onResume();
	}

	/**
	 * If enabled in preferences (disabled by default), show a notification in
	 * the notification bar or cancel/remove notification if it has been
	 * disabled in preferences.
	 */
	@SuppressWarnings("deprecation")
	private void sendNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean displayIcon = prefs.getBoolean("notification_icon", true);

		if (displayIcon) {
			log("Notification icon [ACTIVATE]");
			Notification notification = new Notification(
					R.drawable.notify_icon, res(R.string.app_name),
					System.currentTimeMillis());
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.flags |= Notification.FLAG_NO_CLEAR;

			Intent clearMemoryIntent = new Intent(this, BroadcastManager.class);
			clearMemoryIntent.setAction(Values.CLEAR_RAM);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
					clearMemoryIntent, 0);

			notification.setLatestEventInfo(c,
					res(R.string.notification_title),
					res(R.string.notification_text), pendingIntent);
			notificationManager.notify(Values.notificationID, notification);
		} else {
			notificationManager.cancel(Values.notificationID);
			log("Notification icon [CANCEL]");
		}

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
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

}
