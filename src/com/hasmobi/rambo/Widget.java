package com.hasmobi.rambo;

import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
	
	public static final String DEBUG_TAG = "RAMBO Widget 1x1";

	// The actions that this broadcast receiver accepts
	public static final String ACTION_CLEAR_RAM = "ClearRam";
	public static final String ACTION_UPDATE_WIDGET = "UpdateWidget";

	// This is set once when the widget is loaded for the first time
	int totalMemory = 0;

	// Helps to free up and get available memory
	RamManager rm = null;

	// How often should the widget be updated (in minutes)
	int repeatMinutes = 10;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		final int N = appWidgetIds.length;

		// These two are populated on each loop instead of being created over
		// and over to conserve memory.
		int appWidgetId;
		RemoteViews views;

		// Do this for each instance of this widget
		for (int i = 0; i < N; i++) {
			appWidgetId = appWidgetIds[i];

			// Setup layout
			views = new RemoteViews(context.getPackageName(), R.layout.widget);

			// Setup a PendingIntent that clears the memory when started
			Intent intent = new Intent(context, Widget.class);
			intent.setAction(ACTION_CLEAR_RAM);
			PendingIntent clearRamIntent = PendingIntent.getBroadcast(context,
					0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

			// Attach a onClick event to the Boost button
			views.setOnClickPendingIntent(R.id.bWidgetBoost, clearRamIntent);

			// Setup and repeat the update widget procedure
			Intent updateWidget = new Intent(context, Widget.class);
			updateWidget.setAction(ACTION_UPDATE_WIDGET);
			PendingIntent updateWidgetIntent = PendingIntent
					.getBroadcast(context, 0, updateWidget,
							PendingIntent.FLAG_CANCEL_CURRENT);
			final AlarmManager am = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			final Calendar TIME = Calendar.getInstance();
			TIME.set(Calendar.MINUTE, 0);
			TIME.set(Calendar.SECOND, 0);
			TIME.set(Calendar.MILLISECOND, 0);
			// The actual repeating task
			am.setRepeating(AlarmManager.RTC, TIME.getTime().getTime(),
					1000 * repeatMinutes, updateWidgetIntent);

			// Update the widget layout
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (!(rm instanceof RamManager))
			rm = new RamManager(context);

		if (action.equals(ACTION_CLEAR_RAM)) {
			// Clear RAM action
			rm.killBgProcesses();

			// Refresh the UI
			Intent updateWidget = new Intent(context, Widget.class);
			updateWidget.setAction(ACTION_UPDATE_WIDGET);
			PendingIntent updateWidgetIntent = PendingIntent
					.getBroadcast(context, 0, updateWidget,
							PendingIntent.FLAG_CANCEL_CURRENT);
			try {
				updateWidgetIntent.send();
			} catch (CanceledException e) {
				Log.d(DEBUG_TAG, e.getMessage()); 
			}

		} else if (action.equals(ACTION_UPDATE_WIDGET)) {
			// Manual or automatic widget update started

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);

			// Get the view
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget);

			// Set the available/free RAM text to the widget
			remoteViews.setTextViewText(R.id.tvWidgetRam,
					String.valueOf(rm.getFreeRam() + "/" + rm.getTotalRam()));

			// Trigger widget layout update
			appWidgetManager.updateAppWidget(new ComponentName(context,
					Widget.class), remoteViews);
		}
		super.onReceive(context, intent);
	}

}
