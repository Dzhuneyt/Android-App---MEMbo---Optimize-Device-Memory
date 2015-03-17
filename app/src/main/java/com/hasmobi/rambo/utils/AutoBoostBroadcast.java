package com.hasmobi.rambo.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @deprecated
 */
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
			}
		}

	}

	private void optimize(Context c, boolean silent) {
		RamManager rm = new RamManager(c);
		rm.killAll(silent);
	}
}
