package com.hasmobi.rambo.utils.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.hasmobi.rambo.MainActivity;
import com.hasmobi.rambo.R;
import com.hasmobi.rambo.lib.DResources;
import com.hasmobi.rambo.utils.Prefs;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.Values;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NotificationIconService extends Service {

	private BroadcastReceiver mBroadcastReceiver = null;

	private static String ACTION_BOOST_NOW = "kill_apps";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (new Prefs(getBaseContext()).isNotificationIconEnabled()) {
			startNotificationNew();

			if (mBroadcastReceiver == null) {
				mBroadcastReceiver = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						if (intent.getAction().equals(RamManager.ACTION_RAM_MANAGER)) {
							// Ram cleared. Update UI/notification
							startNotificationNew();
						} else if (intent.getAction().equals(ACTION_BOOST_NOW)) {
							RamManager rm = new RamManager(context);
							rm.killAll(false);
							startNotificationNew();
						}
					}
				};

				IntentFilter filter = new IntentFilter();
				filter.addAction(RamManager.ACTION_RAM_MANAGER);
				filter.addAction(ACTION_BOOST_NOW);

				registerReceiver(mBroadcastReceiver, filter);
			}
		} else {
			stopSelf();
		}

		return START_STICKY;
	}

	public void startNotification() {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext())
				.setSmallIcon(R.drawable.notify_icon)
				.setContentTitle(DResources.getString(this, R.string.memory_usage))
				.setContentText(
						DResources.getString(this, R.string.notification_text));

		mBuilder.setAutoCancel(false);
		mBuilder.setOngoing(true);

		mBuilder.setTicker(null);

		mBuilder.setContentIntent(PendingIntent.getBroadcast(getBaseContext(), 0,
				new Intent(ACTION_BOOST_NOW), PendingIntent.FLAG_UPDATE_CURRENT));

		RamManager rm = new RamManager(getBaseContext());
		int totalRam = rm.getTotalRam();
		int freeRam = rm.getFreeRam();
		int takenRam = totalRam - freeRam;

		mBuilder.setProgress(totalRam, takenRam, false);

		startForeground(Values.notificationID, mBuilder.build());

		PendingIntent pi = PendingIntent.getService(getBaseContext(), 0, new Intent(getBaseContext(), NotificationIconService.class), PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getBaseContext()
				.getSystemService(Context.ALARM_SERVICE);

		am.set(AlarmManager.RTC, System.currentTimeMillis() + 15000,
				pi);
	}

	public boolean startNotificationNew() {
		Context c = getBaseContext();

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c)
				.setSmallIcon(R.drawable.notify_icon)
				.setContentTitle(DResources.getString(c, R.string.memory_usage))
				.setContentText(
						DResources.getString(c, R.string.notification_text));

		mBuilder.setAutoCancel(false);
		mBuilder.setOngoing(true);

		Intent i = new Intent(getBaseContext(), MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent piStartApp = PendingIntent.getActivity(c, 0,
				i, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(piStartApp);

		final RemoteViews remoteViews = new RemoteViews(c.getPackageName(),
				R.layout.notificaiton_icon);

		PendingIntent piOptimize = PendingIntent.getBroadcast(c, 0,
				new Intent(ACTION_BOOST_NOW), PendingIntent.FLAG_UPDATE_CURRENT);

		remoteViews.setOnClickPendingIntent(R.id.bOptimize, piOptimize);

		RamManager rm = new RamManager(c);
		int totalRam = rm.getTotalRam();
		int freeRam = rm.getFreeRam();

		final ActivityManager as = (ActivityManager) getBaseContext()
				.getSystemService(Context.ACTIVITY_SERVICE);

		int activeAppsCount = as
				.getRunningAppProcesses().size();

		String activeAppsLabel = getResources().getString(R.string.active_apps_label);
		activeAppsLabel = String.format(activeAppsLabel, activeAppsCount);

		remoteViews.setTextViewText(R.id.tvRunningApps, activeAppsLabel);

		Prefs p = new Prefs(c);
		long lastOptimizeTimestamp = p.getLastOptimizeTimestamp();

		if (lastOptimizeTimestamp == 0) {
			remoteViews.setViewVisibility(R.id.tvLastOptimizeTimestamp, View.GONE);
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(lastOptimizeTimestamp);

			Calendar currentTime = Calendar.getInstance();

			boolean optimizedToday = calendar.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR) &&
					calendar.get(Calendar.DAY_OF_YEAR) == currentTime.get(Calendar.DAY_OF_YEAR);

			DateFormat formatter;
			if(!optimizedToday){
				formatter = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			}else{
				formatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
			}

			String lastOptimizeLabel = String.format(getResources().getString(R.string.last_optimized_toolbar), formatter.format(calendar.getTime()));
			remoteViews.setTextViewText(R.id.tvLastOptimizeTimestamp, lastOptimizeLabel);
		}

		mBuilder.setContent(remoteViews);

		// mBuilder.setLargeIcon(c.getResources().getDrawable(R.drawable.ic_launcher));

		startForeground(Values.notificationID, mBuilder.build());

		PendingIntent pi = PendingIntent.getService(getBaseContext(), 0, new Intent(getBaseContext(), NotificationIconService.class), PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getBaseContext()
				.getSystemService(Context.ALARM_SERVICE);

		am.set(AlarmManager.RTC, System.currentTimeMillis() + 15000,
				pi);

		return true;
	}

	@Override
	public void onDestroy() {

		// Cancel the notification
		stopForeground(true);

		// Cancel the recurring notification updater
		PendingIntent pi = PendingIntent.getService(getBaseContext(), 0, new Intent(getBaseContext(), NotificationIconService.class), PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) getBaseContext()
				.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi);

		if (mBroadcastReceiver != null) {
			unregisterReceiver(mBroadcastReceiver);
		}

		super.onDestroy();
	}
}
