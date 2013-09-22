package com.hasmobi.rambo;

import com.hasmobi.rambo.utils.AutoBoostBroadcast;
import com.hasmobi.rambo.utils.Prefs;
import com.hasmobi.rambo.utils.RamManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {

	public static String ACTION_UPDATE_WIDGETS = "update_widgets";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		// These two are populated on each loop instead of being created over
		// and over to conserve memory.
		int appWidgetId;

		RamManager rm = new RamManager(context);

		// Do this for each instance of this widget
		for (int i = 0; i < appWidgetIds.length; i++) {
			appWidgetId = appWidgetIds[i];

			// Setup layout
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget);

			// Get the free/available RAM and set it to the TextViews
			views.setTextViewText(R.id.tvWidgetRam,
					String.valueOf(rm.getFreeRam() + "/" + rm.getTotalRam()));

			// Setup a PendingIntent that clears the memory when started
			Intent intent = new Intent(context, AutoBoostBroadcast.class);
			intent.setAction(AutoBoostBroadcast.ACTION_BOOST);
			PendingIntent clearRamIntent = PendingIntent.getBroadcast(context,
					0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			// Attach a onClick event to the Boost button
			views.setOnClickPendingIntent(R.id.bWidgetBoost, clearRamIntent);

			// Update the widget layout
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

		this.setupNextWidgetUpdate(context);

	}

	/**
	 * Setup the widget updater following this policy for getting the update
	 * interval: 1) shared preferences if set or 2) 5 seconds by default. Note
	 * that widget updates will only occur when the screen is on (to save
	 * battery)
	 * 
	 * @param c
	 *            Context
	 */
	private void setupNextWidgetUpdate(Context c) {

		Prefs p = new Prefs(c);
		final AlarmManager am = (AlarmManager) c
				.getSystemService(Context.ALARM_SERVICE);

		Intent updateWidget = new Intent(c, Widget.class);
		updateWidget.setAction(ACTION_UPDATE_WIDGETS);

		PendingIntent updateWidgetIntent = PendingIntent.getBroadcast(c, 0,
				updateWidget, PendingIntent.FLAG_UPDATE_CURRENT);

		am.set(AlarmManager.RTC,
				System.currentTimeMillis()
						+ (p.getWidgetUpdateInterval() * 1000),
				updateWidgetIntent);
	}

	/**
	 * This class is also a broadcast receiver. It usually handles scheduled
	 * widget update requests (we schedule them instead of just relying on the
	 * OS to update the widgets, because the OS is not very reliable on its
	 * timing)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action != null) {
			if (action.equalsIgnoreCase(ACTION_UPDATE_WIDGETS)) {
				// Manual or automatic widget update started (e.g. from
				// scheduled
				// AlarmManager)

				Intent i = new Intent();
				i.setAction("android.appwidget.action.APPWIDGET_UPDATE");
				// Use an array and EXTRA_APPWIDGET_IDS instead of
				// AppWidgetManager.EXTRA_APPWIDGET_ID,
				// since it seems the onUpdate() is only fired on that:
				AppWidgetManager am = AppWidgetManager.getInstance(context);
				int ids[] = am.getAppWidgetIds(new ComponentName(context,
						Widget.class));

				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

				onUpdate(context, am, ids);
			}
		}

		setupNextWidgetUpdate(context);

		super.onReceive(context, intent);
	}

}
