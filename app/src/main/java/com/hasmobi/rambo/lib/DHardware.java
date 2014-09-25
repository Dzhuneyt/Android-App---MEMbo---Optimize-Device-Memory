package com.hasmobi.rambo.lib;

import android.content.Context;
import android.os.Vibrator;

public class DHardware {

	/**
	 * Vibrate the device for the specified amount of seconds
	 * 
	 * @param c
	 * @param duration
	 * @return
	 */
	static public boolean vibrate(Context c, int duration) {
		final Vibrator v = (Vibrator) c
				.getSystemService(Context.VIBRATOR_SERVICE);
		if (v != null)
			v.vibrate(duration);
		return true;
	}
}
