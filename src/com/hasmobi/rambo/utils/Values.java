package com.hasmobi.rambo.utils;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public abstract class Values {

	// Used for logging
	public static final String DEBUG_TAG = "RAM-BO";

	// Name of the SharedPreferences file for the whitelisted apps
	public static final String EXCLUDED_LIST_FILE = "excluded_list";

	// Intent actions to used mainly by the Widgets and BroadcastReceivers
	public static final String CLEAR_RAM = "ClearRam";
	public static final String UPDATE_WIDGETS = "UpdateWidgets";
	public static final String ACTION_AUTOBOOST_ENABLE = "AutoRamClear";
	public static final String ACTION_DO_AUTOBOOST = "AutoRamClearRun";

	public static final int notificationID = 10001;

	public static final Boolean DEBUG_MODE = true;

	public static final String FEEDBACK_EMAIL = "feedback@hasmobi.com";

	public static final int REMIND_RATE_EVERY_N_START = 15;

	public static final int AUTOBOOST_DELAY_SECONDS = 60 * 5;

	public static final int PIE_UPDATE_INTERVAL = 1500; // milliseconds

	/**
	 * Returns the current version code of the app
	 * @param c
	 * @return
	 */
	public static int getCurrentAppVersionCode(Context c) {
		int currentVerCode = 0;
		try {
			currentVerCode = c.getPackageManager().getPackageInfo(
					c.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return currentVerCode;
	}
}
