package com.hasmobi.rambo.utils.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hasmobi.rambo.utils.services.NotificationIconService;
import com.hasmobi.rambo.utils.services.ScreenOnBoost;

public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			context.startService(new Intent(context, ScreenOnBoost.class));

			context.startService(new Intent(context, NotificationIconService.class));
		}
	}
}
