package com.hasmobi.rambo.utils;

import android.content.Context;
import android.content.res.Resources;

public class ResManager {

	static public String getString(Context c, int stringId) {
		return ResManager.getString(c, stringId, null);
	}

	static public String getString(Context c, int stringId, String defaultValue) {
		Resources r = c.getResources();

		String result = defaultValue;
		if (r != null) {
			try {
				result = r.getString(stringId);
			} catch (Resources.NotFoundException e) {
			}
		}
		return result;
	}
}
