package com.hasmobi.rambo.lib;

import android.content.Context;
import android.content.res.Resources;

public class DResources {

	static public String getString(Context c, int stringId) {
		return DResources.getString(c, stringId, null);
	}

	static public String getString(Context c, int stringId, String defaultValue) {
		final Resources r = c.getResources();

		String result = null;
		if (r != null) {
			try {
				result = r.getString(stringId);
			} catch (Resources.NotFoundException e) {
			}
		}

		return result == null ? defaultValue : result;
	}
}
