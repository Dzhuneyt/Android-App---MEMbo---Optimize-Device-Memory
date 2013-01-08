package com.hasmobi.rambo;

import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.Values;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastManager extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action != null) {
			if (action.equalsIgnoreCase(Values.CLEAR_RAM)) {
				RamManager rm = new RamManager(context);
				rm.killBgProcesses();
				log("Broadcast received and memory cleared.");
			}
		}

		refreshWidgets(context);

	}

	private void log(String s) {
		Log.d(Values.DEBUG_TAG, s);
	}

	private void refreshWidgets(Context context) {
		// Refresh the widgets, if any
		Intent updateWidget = new Intent(context, Widget.class);
		updateWidget.setAction(Values.UPDATE_WIDGETS);
		PendingIntent updateWidgetIntent = PendingIntent.getBroadcast(context,
				0, updateWidget, PendingIntent.FLAG_CANCEL_CURRENT);
		try {
			updateWidgetIntent.send();
			log("Widgets update called.");
		} catch (CanceledException e) {
			log(e.getMessage());
		}
	}

}
