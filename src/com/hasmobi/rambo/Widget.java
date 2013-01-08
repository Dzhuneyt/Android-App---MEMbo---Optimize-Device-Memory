package com.hasmobi.rambo;

import java.util.Calendar;

import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.Values;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {

	// How often should the widget be updated (in minutes, overwritten by
	// SharedPreferences value if set below)
	int widgetsUpdateInterval = 5;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		// These two are populated on each loop instead of being created over
		// and over to conserve memory.
		int appWidgetId;
		RemoteViews views;

		// Do this for each instance of this widget
		for (int i = 0; i < appWidgetIds.length; i++) {
			appWidgetId = appWidgetIds[i];

			// Setup layout
			views = new RemoteViews(context.getPackageName(), R.layout.widget);

			// Setup a PendingIntent that clears the memory when started
			Intent intent = new Intent(context, BroadcastManager.class);
			intent.setAction(Values.CLEAR_RAM);
			PendingIntent clearRamIntent = PendingIntent.getBroadcast(context,
					0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

			// Attach a onClick event to the Boost button
			views.setOnClickPendingIntent(R.id.bWidgetBoost, clearRamIntent);

			// Setup and repeat the update widget procedure
			Intent updateWidget = new Intent(context, Widget.class);
			updateWidget.setAction(Values.UPDATE_WIDGETS);
			PendingIntent updateWidgetIntent = PendingIntent
					.getBroadcast(context, 0, updateWidget,
							PendingIntent.FLAG_CANCEL_CURRENT);
			final AlarmManager am = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			final Calendar TIME = Calendar.getInstance();
			TIME.set(Calendar.MINUTE, 0);
			TIME.set(Calendar.SECOND, 0);
			TIME.set(Calendar.MILLISECOND, 0);

			try {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(context);
				widgetsUpdateInterval = prefs.getInt("widget_update_interval",
						5);
			} catch (Exception e) {
			}

			// The actual repeating task
			am.setRepeating(AlarmManager.RTC, TIME.getTime().getTime(),
					1000 * widgetsUpdateInterval, updateWidgetIntent);

			// Update the widget layout
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Values.UPDATE_WIDGETS)) {
			// Manual or automatic widget update started

			RamManager rm = new RamManager(context);

			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget);

			// Set the available/free RAM text to the widget
			remoteViews.setTextViewText(R.id.tvWidgetRam,
					String.valueOf(rm.getFreeRam() + "/" + rm.getTotalRam()));

			// Trigger widget layout update
			AppWidgetManager.getInstance(context).updateAppWidget(
					new ComponentName(context, Widget.class), remoteViews);
		}

		super.onReceive(context, intent);
	}

}
