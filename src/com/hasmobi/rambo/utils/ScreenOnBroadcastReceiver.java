package com.hasmobi.rambo.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

public class ScreenOnBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Debugger.log("ScreenOnBroadcastReceiver->onReceive");

		// check if screen is on
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		Boolean screenOn = pm.isScreenOn();

		if (screenOn) {
			Prefs p = new Prefs(context);
			if (p.isAutoboostEnabled()) {
				RamManager rm = new RamManager(context);
				rm.killBgProcesses(false);
			}

			Debugger.log("Screen is on: " + screenOn.toString());
		}
	}

}
