package com.hasmobi.rambo.adapters;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

public class SingleProcess {

	public String name;
	public boolean whitelisted = false;
	public Drawable icon;
	public float memoryUsage;
	public ApplicationInfo ai;

	public SingleProcess(String name, boolean whitelisted, Drawable icon) {
		this.name = name;
		this.whitelisted = whitelisted;
		this.icon = icon;
	}

	public SingleProcess(String appName, boolean isWhitelisted,
			Drawable appIcon, int totalMemoryUsage) {
		this.name = appName;
		this.whitelisted = isWhitelisted;
		this.icon = appIcon;
		this.memoryUsage = totalMemoryUsage;
	}

	public SingleProcess(ApplicationInfo ai, String appName,
			boolean isWhitelisted, Drawable appIcon, float totalMemoryUsage) {
		this.ai = ai;
		this.name = appName;
		this.whitelisted = isWhitelisted;
		this.icon = appIcon;
		this.memoryUsage = totalMemoryUsage;
	}

	public String getName() {
		return this.name;
	}

	public boolean getIcon() {
		return this.whitelisted;
	}
}
