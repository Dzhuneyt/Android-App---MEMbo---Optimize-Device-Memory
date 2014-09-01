package com.hasmobi.rambo.lib;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class DActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (preferences.getBoolean("first_start", true)) {
			appFirstStart();
		}
	}

	/**
	 * Called on the app's very first start. Overriding classes should implement
	 * this method (and remember to always call the super implementation)
	 */
	private void appFirstStart() {

	}

}
