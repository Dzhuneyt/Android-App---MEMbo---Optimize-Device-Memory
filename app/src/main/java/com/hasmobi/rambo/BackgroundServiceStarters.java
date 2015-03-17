package com.hasmobi.rambo;

import android.app.IntentService;
import android.content.Intent;

import com.hasmobi.rambo.utils.services.BackgroundSyncSleepBackgroundBooster;
import com.hasmobi.rambo.utils.services.NotificationIconService;
import com.hasmobi.rambo.utils.services.ScreenOnBackgroundBooster;
import com.hasmobi.rambo.utils.services.WiFiSleepBackgroundBooster;

public class BackgroundServiceStarters extends IntentService {

	public BackgroundServiceStarters() {
		super("BackgroundServiceStarters");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();

			// Start the services for "sleep" policies to save battery
			// Note that even though the services are started every time
			// they might immediately self-kill if the specific feature
			// is not enabled by the user in the app UI
			startService(new Intent(this, WiFiSleepBackgroundBooster.class));
			startService(new Intent(this, BackgroundSyncSleepBackgroundBooster.class));

			// Start the Notification icon if enabled in Settings
			startService(new Intent(this, NotificationIconService.class));

			startService(new Intent(this, ScreenOnBackgroundBooster.class));
		}
	}
}
