package com.hasmobi.rambo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {

	private SharedPreferences prefs;

	public Prefs(Context c) {
		prefs = PreferenceManager.getDefaultSharedPreferences(c);
	}

	public boolean save(String name, Object value) {
		SharedPreferences.Editor e = prefs.edit();

		if (value instanceof String) {
			e.putString(name, (String) value);
		} else if (value instanceof Boolean) {
			e.putBoolean(name, (Boolean) value);
		} else if (value instanceof Float) {
			e.putFloat(name, (Float) value);
		} else if (value instanceof Long) {
			e.putLong(name, (Long) value);
		} else if (value instanceof Integer) {
			e.putInt(name, (Integer) value);
		} else {
			e.commit();
			return false;
		}

		return e.commit();
	}

	public SharedPreferences instance() {
		return prefs;
	}

	public static SharedPreferences instance(Context c) {
		Prefs p = new Prefs(c);
		return p.instance();
	}

	public int getAppStartCount() {
		return prefs.getInt("app_start_count", 1);
	}

	public boolean isAppRated() {
		return prefs.getBoolean("already_rated_app", false);
	}

	/**
	 * Check if the "screen on autoboost" is enabled
	 * @return
	 */
	public boolean isAutoboostEnabled() {
		return prefs.getBoolean("enable_autoboost", false);
	}

	/**
	 * Check if the autostart of the app on device boot is enabled
	 * @return
	 */
	public boolean isAutostartEnabled() {
		return prefs.getBoolean("start_on_boot", true);
	}

	/**
	 * Has the user checked to enable the apps- Notification icon?
	 * @return
	 */
	public boolean isNotificationIconEnabled() {
		return prefs.getBoolean("notification_icon", true);
	}

	/**
	 * Get the update interval for widgets in seconds
	 */
	public int getWidgetUpdateInterval() {
		return 30;
	}

	/**
	 * Get a generic timestamp of when was the last optimization
	 * @return
	 */
	public long getLastOptimizeTimestamp() {
		return prefs.getLong("last_memory_optimizitaion_timestamp", 0);
	}

	/**
	 * Save a generic timestamp of when was the last optimization
	 * @param timestamp
	 */
	public void saveLastOptimizeTimestamp(long timestamp) {
		this.save("last_memory_optimizitaion_timestamp", timestamp);
	}
}
