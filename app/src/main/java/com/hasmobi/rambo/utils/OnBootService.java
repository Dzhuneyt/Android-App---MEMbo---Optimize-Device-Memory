package com.hasmobi.rambo.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import com.hasmobi.rambo.lib.DDebug;
import com.hasmobi.rambo.utils.services.ScreenOnBoost;

public class OnBootService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
	                          final int startId) {

		DDebug.log(getClass().getSimpleName(), "onStartCommand()");

		startService(new Intent(this, ScreenOnBoost.class));

		return Service.START_NOT_STICKY;
	}

	// unregister the Receiver when the Service gets stopped (destroyed)
	@Override
	public void onDestroy() {
		super.onDestroy();

		DDebug.log(getClass().toString(), "Stopping service OnBootService");
	}
}
