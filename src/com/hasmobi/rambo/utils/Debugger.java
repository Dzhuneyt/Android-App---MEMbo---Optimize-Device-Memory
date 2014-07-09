package com.hasmobi.rambo.utils;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

import com.hasmobi.lib.DDebug;

public class Debugger {

	private Context context;

	public Debugger(Context context) {
		this.context = context;
	}

	public void debugCurrentlyExcluded() {
		final SharedPreferences excludedList = context.getSharedPreferences(
				"excluded_list", 0);
		final Map<String, ?> appsToExclude = excludedList.getAll();
		DDebug.log(null, "CURRENTLY EXCLUDED APPS LIST START:");
		for (Map.Entry<String, ?> entry : appsToExclude.entrySet()) {
			if (entry.getValue().equals(true)) {
				DDebug.log(null, entry.getKey());
			}

		}
		DDebug.log(null, "END OF CURRENTLY EXCLUDED APPS LIST");
	}

	/**
	 * Called when the app tries to open Google Play Marketplace (e.g. the user
	 * choose to rate the app), but it fails. The app then tries to open the
	 * Marketplace URL in a regular browser. If that fails too, this method is
	 * called.
	 */
	public void canNotGoToGooglePlay() {
		// longToast(context.getText(R.string.toast_can_not_open_googleplay));
	}

	public String getAppVersionName(Context c) {
		String versionName = null;
		try {
			versionName = c.getPackageManager().getPackageInfo(
					c.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			DDebug.log(null, e.getMessage(), e);
		}

		return versionName;
	}
}
