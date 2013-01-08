package com.hasmobi.rambo;

import com.hasmobi.rambo.utils.Values;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class StartActivity extends Activity {

	Context c;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		c = getBaseContext();

		setup();
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
		boolean displayIcon = prefs.getBoolean("notification_icon", false);

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
		getMenuInflater().inflate(R.menu.activity_start, menu);
		return true;
	}

}
