package com.hasmobi.rambo.lib;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class DDebug {

	Context c;

	public DDebug() {

	}

	public DDebug(Context c) {
		this.c = c;
	}

	public static void log(String category, String message) {
		Log.d(category, message);
	}

	public static void log(String category, String message, Throwable throwable) {
		Log.d(category, message, throwable);
	}

	/**
	 * Displays a Toast with custom message for a short duration
	 * 
	 * @param c
	 * @param s
	 */
	public static void toast(Context c, String s) {
		Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Displays a Toast with a custom message for a specifiable duration
	 * 
	 * @param c
	 * @param s
	 * @param length
	 *            One of Toast.LENGTH_LONG or Toast.LENGTH_SHORT
	 * @return
	 */
	public static boolean toast(Context c, String s, int length) {
		if (length != Toast.LENGTH_LONG && length != Toast.LENGTH_SHORT) {
			// Invalid toast length specified
			// Must be one of Toast.LENGTH_LONG or Toast.LENGTH_SHORT
			return false;
		} else {
			Toast.makeText(c, s, length).show();
			return true;
		}
	}

	public boolean toast(String s) {
		if (c == null) {
			return false;
		} else {
			DDebug.toast(c, s);
			return true;
		}
	}

	public boolean toast(String s, int length) {
		if (length != Toast.LENGTH_LONG && length != Toast.LENGTH_SHORT) {
			// Invalid toast length specified
			// Must be one of Toast.LENGTH_LONG or Toast.LENGTH_SHORT
			return false;
		} else {
			DDebug.toast(c, s, length);
			return true;
		}
	}
}
