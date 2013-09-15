package com.hasmobi.rambo.utils;

import com.hasmobi.rambo.Widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoBoostBroadcast extends BroadcastReceiver {

	// Broadcast action that kills apps immediately - once
	public static String ACTION_BOOST = "boost";

	// Broadcast action that enables autoboost - a feature that will kill apps
	// at a scheduled interval
	public static String ACTION_AUTOBOOST_ENABLE = "autoboost_enable";

	// Broadcast action that disables autoboost - a feature that will kill apps
	// at a scheduled interval
	public static String ACTION_AUTOBOOST_DISABLE = "autoboost_disable";

	// Intent filter/action that the OS sends when the system has booted on
	// This is only sent by the system, so no activity should use it to
	// broadcast (hence, its private status)
	private String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(Values.DEBUG_TAG, "onReceive");

		// What kind of action was broadcasted?
		String action = intent.getAction();

		if (action != null) {
			if (action.equalsIgnoreCase(ACTION_BOOST)) {
				// kill apps now - once
				this.optimize(context);
			} else if (action.equalsIgnoreCase(ACTION_AUTOBOOST_ENABLE)) {
				// enable autoboost
				this.enableAutoBoost(context);
			} else if (action.equalsIgnoreCase(ACTION_AUTOBOOST_DISABLE)) {
				// disable autoboost
				this.disableAutoBoost(context);
			} else if (action.equalsIgnoreCase(ACTION_BOOT_COMPLETED)) {
				// on system boot

				Prefs p = new Prefs(context);
				if (p.isAutoboostEnabled()) {
					this.enableAutoBoost(context);
				} else {
					this.disableAutoBoost(context);
				}

			}
		}

	}

	private void optimize(Context c) {
		RamManager rm = new RamManager(c);
		rm.killBgProcesses();

		// Update widgets, because the "Boost" may have been initiated by a
		// widget button click
		Intent updateWidget = new Intent(c, Widget.class);
		updateWidget.setAction(Widget.ACTION_UPDATE_WIDGETS);

		PendingIntent updateWidgetIntent = PendingIntent.getBroadcast(c, 0,
				updateWidget, PendingIntent.FLAG_CANCEL_CURRENT);
		try {
			updateWidgetIntent.send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}

	private void disableAutoBoost(Context context) {
		Intent autoBoostIntent = new Intent(context, AutoBoostBroadcast.class);
		autoBoostIntent.setAction(ACTION_BOOST);

		PendingIntent pi = PendingIntent.getBroadcast(context, 0,
				autoBoostIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		am.cancel(pi);
	}

	private void enableAutoBoost(Context context) {
		Intent autoBoostIntent = new Intent(context, AutoBoostBroadcast.class);
		autoBoostIntent.setAction(ACTION_BOOST);

		PendingIntent pi = PendingIntent.getBroadcast(context, 0,
				autoBoostIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				(Values.AUTOBOOST_DELAY_SECONDS * 1000), pi);
	}
}
