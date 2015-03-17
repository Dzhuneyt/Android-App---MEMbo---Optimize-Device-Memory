package com.hasmobi.rambo.utils.custom_views.batteryTips;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

abstract public class ISingleBatteryTip extends LinearLayout {

	public String title;
	public String description;
	public String spName;

	public Context context;

	public ISingleBatteryTip(Context context) {
		super(context);

		init();
	}

	public ISingleBatteryTip(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	public ISingleBatteryTip(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		init();
	}

	public abstract void init();
}
