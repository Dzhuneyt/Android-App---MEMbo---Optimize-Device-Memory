package com.hasmobi.rambo.utils;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class Debugger {

	private Context context;

	public Debugger(Context context) {
		this.context = context;
	}

	public void debugCurrentlyExcluded() {
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

	public static void log(String s) {
		if (Values.DEBUG_MODE)
			Log.d(Values.DEBUG_TAG, s);
	}

	public void toast(String s) {
		this.shortToast(s);
	}

	public void shortToast(String s) {
		// display in short period of time
		Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
	}

	public void longToast(String s) {
		// display in long period of time
		Toast.makeText(context, s, Toast.LENGTH_LONG).show();
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
}
