package com.hasmobi.rambo.utils;

import android.content.pm.ApplicationInfo;

public class SingleInstalledApp {

	public ApplicationInfo ai = null;

	public SingleInstalledApp(ApplicationInfo provided) {
		this.ai = provided;
	}
}
