package com.hasmobi.rambo.utils.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;

import com.hasmobi.rambo.utils.Prefs;
import com.hasmobi.rambo.utils.RamManager;

/**
 * A service that is started on device boot by OnBootService
 * or manually when the user changes the "Screen on autoboost"
 * policy in the app's settings. The service will check if the
 * "Screen on autoboost" feature is enabled by the user and
 * register a BroadcastReceiver that will be called when the screen
 * is turned on or unlocked and close apps. If the feature is disabled
 * while this service is started, the service will stop itself.
 */
public class ScreenOnBoost extends Service {

	BroadcastReceiver broadcastReceiver;
	boolean registered = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (broadcastReceiver == null) {
			broadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {

					// check if screen is on
					PowerManager pm = (PowerManager) context
							.getSystemService(Context.POWER_SERVICE);
					Boolean screenOn = pm.isInteractive();

					if (screenOn && intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
						RamManager rm = new RamManager(
								getApplicationContext());
						rm.killBgProcesses(false);
					}
				}
			};
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Prefs p = new Prefs(this.getApplicationContext());
		if (!registered && p.isScreenOnAutoboostEnabled()) {
			registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
			registered = true;
		} else {
			stopSelf();
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (broadcastReceiver != null && registered) {
			unregisterReceiver(broadcastReceiver);
			registered = false;
		}
		super.onDestroy();
	}
}
