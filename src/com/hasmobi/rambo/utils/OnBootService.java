package com.hasmobi.rambo.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import com.hasmobi.lib.DDebug;

public class OnBootService extends Service {

	private BroadcastReceiver broadcast = null;

	public Handler h = new Handler();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {

		DDebug.log(getClass().toString(), "onStartCommand()");

		if (broadcast == null) {
			broadcast = new BroadcastReceiver() {

				@Override
				public void onReceive(final Context context, Intent intent) {
					DDebug.log(getClass().toString(),
							"OnBootService->onReceive (received broadcast)");

					// check if screen is on
					PowerManager pm = (PowerManager) context
							.getSystemService(Context.POWER_SERVICE);
					Boolean screenOn = pm.isScreenOn();

					if (screenOn) {
						Prefs p = new Prefs(context);
						if (p.isAutoboostEnabled()) {
							h.post(new Runnable() {

								public void run() {
									RamManager rm = new RamManager(
											getApplicationContext());
									rm.killBgProcesses(false);
								}

							});

						}

						DDebug.log(this.getClass().toString(), "Screen is on: "
								+ screenOn.toString());
					}
				}

			};
			registerReceiver(broadcast, new IntentFilter(
					Intent.ACTION_USER_PRESENT));
		}
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	// unregister the Receiver when the Service gets stopped (destroyed)
	@Override
	public void onDestroy() {
		super.onDestroy();

		if (broadcast != null)
			unregisterReceiver(broadcast);

		DDebug.log(getClass().toString(), "Stopping service OnBootService");
	}
}
