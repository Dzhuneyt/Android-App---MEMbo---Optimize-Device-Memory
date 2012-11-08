package com.hasmobi.rambo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class RamManager {

	MemoryInfo mi = null;

	String[] excluded = { "system_process", "com.hasmobi.rambo",
			"com.android.phone", "com.android.systemui",
			"android.process.acore", "com.android.launcher" };
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
	}

	public void killBgProcesses() {

		// Get all apps to exclude from the preferences
		final SharedPreferences excludedList = context.getSharedPreferences(
				"excluded_list", 0);
		final Map<String, ?> appsToExclude = excludedList.getAll();

		boolean excludeThis = false;
		int killCount = 0;
		for (RunningAppProcessInfo pid : am.getRunningAppProcesses()) {

			for (Map.Entry<String, ?> entry : appsToExclude.entrySet()) {
				Log.d("mine", entry.getKey());
				Log.d("mine", pid.processName);
				// If process exists in SharedPreferences and it is to be
				// excluded (value=true)
				if (pid.processName.equalsIgnoreCase(entry.getKey())
						&& excludedList.getBoolean(entry.getKey(), false) == true) {
					excludeThis = true;
					Log.d(Values.DEBUG_TAG, "Excluding " + entry.getKey()
							+ " from kill list");
				}
			}

			if (excludeThis == false) {
				// The running process is not in the exclude list, kill it
				am.killBackgroundProcesses(pid.processName);
				killCount++;
				Log.d(Values.DEBUG_TAG, "Killing " + pid.processName);

			} else {
				// Reset the exclusion to default for the next active process
				excludeThis = false;
			}
		}

		String toDisplay = context.getResources().getString(
				R.string.ram_cleared)
				+ "\n" + context.getResources().getString(R.string.apps_killed);
		toDisplay = String.format(toDisplay, killCount);
		Toast.makeText(
				context.getApplicationContext(),
				String.format(
						context.getResources().getString(R.string.ram_cleared)
								+ "\n"
								+ context.getResources().getString(
										R.string.apps_killed), killCount),
				Toast.LENGTH_LONG).show();

		Vibrator v = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);
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
