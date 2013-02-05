package com.hasmobi.rambo;

import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.Values;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastManager extends BroadcastReceiver {

	private int AUTOBOOST_DELAY_SECONDS = 60 * 15; // 15 minutes by default

	@Override
	public void onReceive(Context context, Intent intent) {
		log("Broadcast received");

		String action = intent.getAction();
		if (action != null) {
			if (action.equalsIgnoreCase(Values.CLEAR_RAM)) {
				RamManager rm = new RamManager(context);
				rm.killBgProcesses();
				log("Broadcast received and memory cleared.");

			} else if (action.equalsIgnoreCase(Values.ACTION_AUTOBOOST_ENABLE)) {

				// Setup a repeating PendingIntent that will clear the RAM every
				// time it runs.

				Intent autoBoostIntent = new Intent(context,
						BroadcastManager.class);
				autoBoostIntent.setAction(Values.CLEAR_RAM);
				PendingIntent pi = PendingIntent.getBroadcast(context, 0,
						autoBoostIntent, PendingIntent.FLAG_CANCEL_CURRENT);

				AlarmManager am = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);

				am.setRepeating(AlarmManager.RTC_WAKEUP,
						System.currentTimeMillis(),
						(AUTOBOOST_DELAY_SECONDS * 1000), pi);

				log("Setting up autoboost to run every "
						+ (AUTOBOOST_DELAY_SECONDS * 1000) + " seconds");
			} else if (action.equalsIgnoreCase(Values.ACTION_DO_AUTOBOOST)) {
				RamManager rm = new RamManager(context);
				boolean silent = true;
				rm.killBgProcesses(silent);
				log("Broadcast received and memory cleared.");
			} else if (action
					.equalsIgnoreCase("android.intent.action.BOOT_COMPLETED")) {
				// Enable autoboost on device boot
				Intent autoBoostIntent = new Intent(context,
						BroadcastManager.class);
				autoBoostIntent.setAction(Values.ACTION_AUTOBOOST_ENABLE);
				PendingIntent pi = PendingIntent.getBroadcast(context, 0,
						autoBoostIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				try {
					pi.send();
				} catch (CanceledException e) {
				}
			}
		}

		refreshWidgets(context);

	}

	private void log(String s) {
		Log.d(Values.DEBUG_TAG, s);
	}

	private void refreshWidgets(Context context) {
		// Refresh the widgets, if any
		Intent updateWidget = new Intent(context, Widget.class);
		updateWidget.setAction(Values.UPDATE_WIDGETS);
		PendingIntent updateWidgetIntent = PendingIntent.getBroadcast(context,
				0, updateWidget, PendingIntent.FLAG_CANCEL_CURRENT);
		try {
			updateWidgetIntent.send();
			log("Widgets update called.");
		} catch (CanceledException e) {
			log(e.getMessage());
		}
	}

}
