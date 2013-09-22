package com.hasmobi.rambo.utils;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.view.View.MeasureSpec;
import android.widget.RemoteViews;

import com.hasmobi.rambo.R;

public class NotificationIcon extends BroadcastReceiver {

	static public void notify(Context c) {
		Prefs p = new Prefs(c);
		if (p.isNotificationIconEnabled()) {
			NotificationIcon.startNotification(c);
		} else {
			NotificationIcon.stopNotification(c);
		}
	}

	static public boolean startNotification(Context c) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c)
				.setSmallIcon(R.drawable.notify_icon)
				.setContentTitle(ResManager.getString(c, R.string.memory_usage))
				.setContentText(
						ResManager.getString(c, R.string.notification_text));

		mBuilder.setAutoCancel(false);
		mBuilder.setOngoing(true);

		Intent clearMemoryIntent = new Intent(c, AutoBoostBroadcast.class);
		clearMemoryIntent.setAction(AutoBoostBroadcast.ACTION_BOOST);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0,
				clearMemoryIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(pendingIntent);

		RamManager rm = new RamManager(c);
		int totalRam = rm.getTotalRam();
		int freeRam = rm.getFreeRam();
		int takenRam = totalRam - freeRam;

		mBuilder.setProgress(totalRam, takenRam, false);

		NotificationManager mNotificationManager = (NotificationManager) c
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(Values.notificationID, mBuilder.build());

		Intent i = new Intent(c, NotificationIcon.class);
		PendingIntent pi = PendingIntent.getBroadcast(c, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = (AlarmManager) c
				.getSystemService(Context.ALARM_SERVICE);

		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 5000,
				pi);

		return true;
	}

	/**
	 * Implementing a custom View as Notification content (unused for now)
	 */
	static public boolean startNotificationNew(Context c) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c)
				.setSmallIcon(R.drawable.notify_icon)
				.setContentTitle(ResManager.getString(c, R.string.memory_usage))
				.setContentText(
						ResManager.getString(c, R.string.notification_text));

		mBuilder.setAutoCancel(false);
		mBuilder.setOngoing(true);

		Intent clearMemoryIntent = new Intent(c, AutoBoostBroadcast.class);
		clearMemoryIntent.setAction(AutoBoostBroadcast.ACTION_BOOST);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0,
				clearMemoryIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(pendingIntent);

		final RemoteViews remoteViews = new RemoteViews(c.getPackageName(),
				R.layout.notificaiton_icon);

		remoteViews.setOnClickPendingIntent(R.id.button1, pendingIntent);

		RamManager rm = new RamManager(c);
		int totalRam = rm.getTotalRam();
		int freeRam = rm.getFreeRam();
		int takenRam = totalRam - freeRam;

		final PieView pie = new PieView(c);
		pie.setRam(totalRam, freeRam);

		pie.setDrawingCacheEnabled(true);
		pie.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		pie.layout(0, 0, pie.getMeasuredWidth(), pie.getMeasuredHeight());
		pie.buildDrawingCache(true);

		Bitmap bitmap = Bitmap.createBitmap(pie.getDrawingCache());
		pie.setDrawingCacheEnabled(false);

		remoteViews.setImageViewBitmap(R.id.ivPie, bitmap);

		mBuilder.setContent(remoteViews);

		mBuilder.setLargeIcon(pie.getDrawingCache());

		NotificationManager mNotificationManager = (NotificationManager) c
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(Values.notificationID, mBuilder.build());

		Intent i = new Intent(c, NotificationIcon.class);
		PendingIntent pi = PendingIntent.getBroadcast(c, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = (AlarmManager) c
				.getSystemService(Context.ALARM_SERVICE);

		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 5000,
				pi);

		return true;
	}

	static public boolean stopNotification(Context c) {
		NotificationManager notificationManager = (NotificationManager) c
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(Values.notificationID);

		Intent i = new Intent(c, NotificationIcon.class);
		PendingIntent pi = PendingIntent.getBroadcast(c, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager alarmManager = (AlarmManager) c
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pi);
		return true;
	}

	@Override
	public void onReceive(Context c, Intent i) {
		// Enable or disable notification icon (depending on if it's enabled in
		// preferences)
		NotificationIcon.notify(c);
	}

}
