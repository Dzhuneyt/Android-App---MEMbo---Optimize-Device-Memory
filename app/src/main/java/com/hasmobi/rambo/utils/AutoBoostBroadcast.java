package com.hasmobi.rambo.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.hasmobi.rambo.utils.services.OnBootService;

public class AutoBoostBroadcast extends BroadcastReceiver {

	// Broadcast action that kills apps immediately - once
	public static String ACTION_BOOST_ONETIME = "boost";

	// Broadcast action that kills apps immediately - once, without displaying a
	// Toast afterwards
	public static String ACTION_BOOST_SILENT = "silent_boost";

	/**
	 * Broadcast action that enables autoboost - a feature that will kill apps
	 * at a scheduled interval
	 * 
	 * @deprecated
	 */
	public static String ACTION_AUTOBOOST_ENABLE = "autoboost_enable";

	/**
	 * Broadcast action that disables autoboost - a feature that will kill apps
	 * at a scheduled interval
	 * 
	 * @deprecated
	 */
	public static String ACTION_AUTOBOOST_DISABLE = "autoboost_disable";

	public static String ACTION_SCREENON_AUTOBOOST_ENABLED = "screenon_autoboost_on";
	public static String ACTION_SCREENON_AUTOBOOST_DISABLED = "screenon_autoboost_off";

	// Intent filter/action that the OS sends when the system has booted on
	// This is only sent by the system, so no activity should use it to
	// broadcast (hence, its private status)
	private static String ACTION_BOOT_COMPLETED = Intent.ACTION_BOOT_COMPLETED;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(Values.DEBUG_TAG, "onReceive");

		// What kind of action was broadcasted?
		String action = intent.getAction();

		if (action != null) {
			if (action.equalsIgnoreCase(ACTION_BOOST_ONETIME)) {
				// kill apps now - once
				this.optimize(context, false);
			} else if (action.equalsIgnoreCase(ACTION_BOOST_SILENT)) {
				// kill apps now - once. Don't display a Toast
				this.optimize(context, true);
			} else if (action.equalsIgnoreCase(ACTION_AUTOBOOST_ENABLE)) {
				// enable autoboost
				this.scheduledAutoBoost(context, true);
			} else if (action.equalsIgnoreCase(ACTION_AUTOBOOST_DISABLE)) {
				// disable autoboost
				this.scheduledAutoBoost(context, false);
			} else if (action.equalsIgnoreCase(ACTION_BOOT_COMPLETED)) {
				// on system boot
				Prefs p = new Prefs(context);

				if (p.isAutostartEnabled()) {
					screenOnAutoboost(context, p.isScreenOnAutoboostEnabled());
				}

			} else if (action
					.equalsIgnoreCase(ACTION_SCREENON_AUTOBOOST_ENABLED)) {
				screenOnAutoboost(context, true);
			} else if (action
					.equalsIgnoreCase(ACTION_SCREENON_AUTOBOOST_DISABLED)) {
				screenOnAutoboost(context, false);
			}
		}

	}

	private void optimize(Context c, boolean silent) {
		RamManager rm = new RamManager(c);
		rm.killBgProcesses(silent);
	}

	/**
	 * Enables/disables a scheduled autobooster that will boost every X seconds
	 * (defined in Values.AUTOBOOST_DELAY_SECONDS). Currently not implemented.
	 * To be implemented in a better way using a Service, since right now it
	 * dies along with the activity.
	 * 
	 * @param context
	 * @param newState
	 * @deprecated
	 */
	private void scheduledAutoBoost(Context context, boolean newState) {
		Intent autoBoostIntent = new Intent(context, AutoBoostBroadcast.class);
		autoBoostIntent.setAction(ACTION_BOOST_ONETIME);

		PendingIntent pi = PendingIntent.getBroadcast(context, 0,
				autoBoostIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		if (newState) {
			am.setRepeating(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis(),
					(Values.AUTOBOOST_DELAY_SECONDS * 1000), pi);
		} else {
			am.cancel(pi);
		}
	}

	/**
	 * Starts/stops the service that runs in the background and triggers a
	 * memory boost every time the device screen is turned on (or the keyboard
	 * is unlocked)
	 * 
	 * @param context
	 * @param enabledOrDisable
	 *            - the new state of the service - true=start, false=stop
	 */
	private void screenOnAutoboost(Context context, boolean enabledOrDisable) {
		// Start the autobooster that boosts on each device screen on
		Intent i = new Intent(context, OnBootService.class);
		if (enabledOrDisable) {
			context.startService(i);
		} else {
			context.stopService(i);
		}
	}
}
