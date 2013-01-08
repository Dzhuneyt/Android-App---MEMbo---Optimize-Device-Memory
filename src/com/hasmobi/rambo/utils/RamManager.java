package com.hasmobi.rambo.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class RamManager {

	MemoryInfo mi = null;

	Context context;
	ActivityManager am;

	public RamManager(Context c) {
		this.context = c;
		am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		mi = new MemoryInfo();
	}

	public void killPackage(String packageName) {
		am.killBackgroundProcesses(packageName);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		// Vibrate after killing?
		if (prefs.getBoolean("vibrate_after_optimize", true)) {
			Vibrator v = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(100);
		}
	}

	public void killBgProcesses() {

		// Get all apps to exclude from the preferences
		final SharedPreferences excludedList = context.getSharedPreferences(
				"excluded_list", 0);

		int killCount = 0;

		for (RunningAppProcessInfo pid : am.getRunningAppProcesses()) {

			if (excludedList.getBoolean(pid.processName, false)) {
				// Process whitelisted, don't kill
				Log.d(Values.DEBUG_TAG, pid.processName + " [EXCLUDE]");
			} else {
				// Kill process
				am.killBackgroundProcesses(pid.processName);
				killCount++;
				Log.d(Values.DEBUG_TAG, pid.processName + "[KILLING]");
			}
		}

		// Notify user that optimization is completed
		Resources res = context.getResources();
		String toDisplay = res.getString(R.string.ram_cleared) + "\n"
				+ res.getString(R.string.apps_killed);
		toDisplay = String.format(toDisplay, killCount);
		Toast.makeText(context, toDisplay, Toast.LENGTH_LONG).show();

		// Should the device vibrate after killing?
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (prefs.getBoolean("vibrate_after_optimize", true)) {
			Vibrator v = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(100);
		}
	}

	public int getFreeRam() {
		am.getMemoryInfo(mi);
		int freeRam = 0;
		try {
			freeRam = (int) (mi.availMem / 1048576L);
		} catch (Exception e) {
		}
		return freeRam;
	}

	public int getTotalRam() {
		int tm = 0;
		try {
			RandomAccessFile r;
			r = new RandomAccessFile("/proc/meminfo", "r");
			String load = r.readLine();
			String[] totrm = load.split(" kB");
			String[] trm = totrm[0].split(" ");
			tm = Integer.parseInt(trm[trm.length - 1]);
			tm = Math.round(tm / 1024);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tm;
	}
}
