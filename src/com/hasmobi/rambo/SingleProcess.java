package com.hasmobi.rambo;

import android.graphics.drawable.Drawable;

public class SingleProcess {

	public String name;
	public boolean whitelisted = false;
	public Drawable icon;

	public SingleProcess(String name, boolean whitelisted, Drawable icon) {
		this.name = name;
		this.whitelisted = whitelisted;
		this.icon = icon;
	}

	public String getName() {
		return this.name;
	}

	public boolean getIcon() {
		return this.whitelisted;
	}
}
