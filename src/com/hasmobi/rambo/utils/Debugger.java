package com.hasmobi.rambo.utils;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

	private void log(String s) {
		Log.d(Values.DEBUG_TAG, s);
	}
}
