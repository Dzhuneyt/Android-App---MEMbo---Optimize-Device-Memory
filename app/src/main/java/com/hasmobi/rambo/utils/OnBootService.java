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

public class OnBootService extends Service {

	private BroadcastReceiver screenOnBroadcastReceiver = null;

	public Handler h = new Handler();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {

		DDebug.log(getClass().toString(), "onStartCommand()");

		this.startScreenOnBoostBroadcast();

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

		if (screenOnBroadcastReceiver != null)
			unregisterReceiver(screenOnBroadcastReceiver);

		DDebug.log(getClass().toString(), "Stopping service OnBootService");
	}

	/**
	 * Create the Broadcast Receiver that will handle the
	 * "Intent.ACTION_USER_PRESENT" global OS screenOnBroadcastReceiver (which
	 * is broadcasted when the user unlocks the screen or just turns it on in
	 * case no lock screen is used)
	 * 
	 * Note that although we have this Receiver always active and receiving that
	 * screenOnBroadcastReceiver, we don't always take action. We only do an
	 * optimization on that screenOnBroadcastReceiver if the user has enabled
	 * the "Screen on autoboost" option in the MEMbo app's settings screen
	 */
	private void startScreenOnBoostBroadcast() {
		if (screenOnBroadcastReceiver == null) {
			screenOnBroadcastReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(final Context context, Intent intent) {
					DDebug.log(getClass().toString(),
							"OnBootService->onReceive: " + intent.getAction());

					// check if screen is on
					PowerManager pm = (PowerManager) context
							.getSystemService(Context.POWER_SERVICE);
					Boolean screenOn = pm.isScreenOn();

					if (screenOn) {
						Prefs p = new Prefs(context);
						if (p.isScreenOnAutoboostEnabled()) {

							// We run the optimization in a new Runnable just in
							// case something breaks
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
			registerReceiver(screenOnBroadcastReceiver, new IntentFilter(
					Intent.ACTION_USER_PRESENT));
		}
	}
}
