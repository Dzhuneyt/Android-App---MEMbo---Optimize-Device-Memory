package com.hasmobi.rambo.utils.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.hasmobi.rambo.utils.BatteryTipAdapter;

public class BackgroundSyncSleep extends Service {

    public static String SP_NAME = "background_sync_sleep";

    private String ACTION_DISABLE_SYNC = "com.hasmobi.rambo.ACTION_DISABLE_SYNC";
    private String ACTION_ENABLE_SYNC = "com.hasmobi.rambo.ACTION_ENABLE_SYNC";

    private BroadcastReceiver broadcastReceiver;

    // Set to true when the current service class
    // has disabled the Sync so that next time the screen
    // is turned on we know that we should enabled it.
    // Otherwise don't enable Sync because the user
    // may not want it enabled and we don't want to do it
    // without his consent
    private boolean syncDisabledByCurrentService = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String strAction = intent.getAction();

                    if (strAction.equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)) {
                        // Screen locked
                        Log.d(getClass().toString(), "Screen off broadcast received");

                        SharedPreferences sp = getSharedPreferences(BatteryTipAdapter.SP_NAME_BATTERY_PREFS, Context.MODE_PRIVATE);
                        int secondsToSleep = sp.getInt(SP_NAME, 0);

                        if (secondsToSleep > 0) {
                            // Schedule the Sync to be disabled after X seconds
                            // unless the screen is turned on by that time
                            // in which case the intent will be cancelled

                            AlarmManager scheduler = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, new Intent(ACTION_DISABLE_SYNC), 0);
                            long now = System.currentTimeMillis();
                            long interval = secondsToSleep * 1000;
                            scheduler.set(AlarmManager.RTC, now + interval, pendingIntent);

                            Log.d(getClass().toString(), "Scheduled to disable Sync");
                        }
                    } else if (strAction.equalsIgnoreCase(Intent.ACTION_SCREEN_ON)) {
                        // Screen unlocked

                        AlarmManager scheduler = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, new Intent(ACTION_DISABLE_SYNC), 0);
                        scheduler.cancel(pendingIntent);

                        Log.d(getClass().toString(), "Screen turn on broadcast received. Any scheduled Sync disablers cancelled");

                        if (syncDisabledByCurrentService) {
                            sendBroadcast(new Intent(ACTION_ENABLE_SYNC));
                            Log.d(getClass().toString(), "Also enabling Sync");
                        }

                    } else if (strAction.equalsIgnoreCase(ACTION_DISABLE_SYNC)) {
                        // Disable WIFI here immediately
                        Log.d(getClass().toString(), "Sync disabled");
                        if (ContentResolver.getMasterSyncAutomatically()) {
                            // Disable master sync if it's enabled currently
                            ContentResolver.setMasterSyncAutomatically(false);
                            syncDisabledByCurrentService = true;
                        }
                    } else if (strAction.equalsIgnoreCase(ACTION_ENABLE_SYNC)) {
                        // Enable WIFI here immediately
                        if (syncDisabledByCurrentService) {
                            Log.d(getClass().toString(), "Sync enabled");
                            // Only enable it if it was disabled by the current service
                            ContentResolver.setMasterSyncAutomatically(true);
                        }
                    }
                }
            };

            try {
                registerReceivers();
            } catch (IllegalStateException e) {
            }
        }

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    private void registerReceivers() {
        final IntentFilter theFilter = new IntentFilter();
        /** System Defined Broadcast */
        theFilter.addAction(Intent.ACTION_SCREEN_ON);
        theFilter.addAction(Intent.ACTION_SCREEN_OFF);
        theFilter.addAction(ACTION_DISABLE_SYNC);
        theFilter.addAction(ACTION_ENABLE_SYNC);

        registerReceiver(broadcastReceiver, theFilter);
    }
}
