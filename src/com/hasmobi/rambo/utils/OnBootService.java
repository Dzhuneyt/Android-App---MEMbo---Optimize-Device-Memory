package com.hasmobi.rambo.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;

public class OnBootService extends Service {
	private static final String TAG = OnBootService.class.getSimpleName();
	private BroadcastReceiver mPowerButtonReceiver = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {

		Debugger.log("onStartCommand on OnBootService service");

		if (mPowerButtonReceiver == null) {
			mPowerButtonReceiver = new ScreenOnBroadcastReceiver();
			registerReceiver(mPowerButtonReceiver, new IntentFilter(
					Intent.ACTION_USER_PRESENT));
		}
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Debugger.log("Starting OnBootService service");
	}

	// unregister the Receiver when the Service gets stopped (destroyed)
	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mPowerButtonReceiver != null)
			unregisterReceiver(mPowerButtonReceiver);

		Debugger.log("Stopping service OnBootService");
	}
}
