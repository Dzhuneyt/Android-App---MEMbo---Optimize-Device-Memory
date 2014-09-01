package com.hasmobi.rambo.lib;

import android.app.Activity;
import android.view.View;

public class DView {

	/**
	 * Inflates a View in the given context, given the provided resource XML ID.
	 * Note that the new View will not be attached to any root using this
	 * function.
	 * 
	 * @param c
	 * @param layoutRes
	 * @return
	 */
	static public View inflateView(Activity c, int layoutRes) {
		return c.getLayoutInflater().inflate(layoutRes, null);
	}
}
