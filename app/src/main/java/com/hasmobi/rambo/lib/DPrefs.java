package com.hasmobi.rambo.lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DPrefs {
	public SharedPreferences prefs;
	private Context c;

	public DPrefs(Context c) {
		this.c = c;
		prefs = PreferenceManager.getDefaultSharedPreferences(c);
	}

	public void flush() {
		if (prefs != null)
			prefs.edit().clear().commit();
	}

	public void reload() throws DException {
		if (this.c == null) {
			throw new DException("Context not provided for "
					+ this.getClass().toString());

		} else {
			prefs = PreferenceManager.getDefaultSharedPreferences(this.c);
		}
	}
}
