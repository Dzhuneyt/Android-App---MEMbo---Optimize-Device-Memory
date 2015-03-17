package com.hasmobi.rambo.utils.custom_views.batteryTips;

import android.content.Context;

import com.hasmobi.rambo.utils.services.WiFiSleepBackgroundBooster;

public class BatteryTipWifiSleep extends ISingleBatteryTip {

	public BatteryTipWifiSleep(Context c){
		super(c);

		this.spName = WiFiSleepBackgroundBooster.SP_NAME;
		this.title = "WiFi Sleep";
		this.description = "Automatically disable WiFi after the screen is turned off. It will be re-enabled as soon as you turn on the screen.";
	}

	@Override
	public void init() {
		/*
		View v = inflate(getContext(), R.layout.tip_scrubber, this);

		TextView tvHeader = (TextView) v.findViewById(R.id.tvHeader);
		TextView tvDescription = (TextView) v.findViewById(R.id.tvDescription);

		tvHeader.setVisibility(View.VISIBLE);
		tvHeader.setText(this.title);

		tvDescription.setVisibility(View.VISIBLE);
		tvDescription.setText(this.description);*/
	}
}
