package com.hasmobi.rambo.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.hasmobi.rambo.R;

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

	class ProcessToKill {
		private int pid;
		private String packageName;
	}

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
			if (v != null)
				v.vibrate(100); // Vibrate for 100ms
		}
	}

	/**
	 * Unused for now since a more practical and perfromance friendly method was
	 * found
	 * 
	 * @param pid
	 *            - The PID of an active process
	 * @return int - Total RAM usage (including PSS, SharedDirty and
	 *         PrivateDirty for the process.
	 */
	private int getMemoryUsageForPid(int pid) {
		int totalMemoryUsage = 0;
		try {
			final android.os.Debug.MemoryInfo memoryInfoArray = am
					.getProcessMemoryInfo(new int[] { pid })[0];
			totalMemoryUsage = memoryInfoArray.nativePss
					+ memoryInfoArray.nativePrivateDirty
					+ memoryInfoArray.nativeSharedDirty;
		} catch (Exception e) {
			log("Can't get memory info for PID: " + pid);
		}
		return totalMemoryUsage;
	}

	public void killBgProcesses() {

		// Get all apps to exclude from the preferences
		final SharedPreferences excludedList = context.getSharedPreferences(
				"excluded_list", 0);

		List<RunningAppProcessInfo> runningProcesses = am
				.getRunningAppProcesses();

		List<ProcessToKill> processesToKillArr = new ArrayList<ProcessToKill>();

		for (RunningAppProcessInfo pid : runningProcesses) {

			if (excludedList.getBoolean(pid.processName, false)) {
				// Process whitelisted, don't kill
				log(pid.processName + " [EXCLUDE]");
			} else {

				// Add each process that is not in a whitelist to a special
				// array (to be killed later).
				ProcessToKill processToKill = new ProcessToKill();
				processToKill.packageName = pid.processName;
				processToKill.pid = pid.pid;

				processesToKillArr.add(processToKill);

			}
		}

		// am.getProcessMemoryInfo(new int[pid.pid]);

		if (processesToKillArr.size() > 0) {

			int killCount = 0;

			int oldFreeRam = new RamManager(context).getFreeRam();

			for (ProcessToKill p : processesToKillArr) {
				// Kill this process
				am.killBackgroundProcesses(p.packageName);
				log(p.packageName + "[KILLING]");
				// Increase the counter by one
				killCount++;
			}

			int newFreeRam = new RamManager(context).getFreeRam();
			int savedRam = newFreeRam - oldFreeRam;

			// Notify user that optimization is completed
			Resources res = context.getResources();
			String toDisplay = res.getString(R.string.ram_cleared) + "\n"
					+ res.getString(R.string.apps_killed);
			toDisplay = String.format(toDisplay, killCount);
			toDisplay = toDisplay
					+ "\n"
					+ String.format(
							res.getString(R.string.memoryclearedamount),
							savedRam);
			Toast.makeText(context, toDisplay, Toast.LENGTH_LONG).show();

		} else {
			Toast.makeText(context, "No processes killed", Toast.LENGTH_LONG)
					.show();
		}

		// Should the device vibrate after killing?
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (prefs.getBoolean("vibrate_after_optimize", true)) {
			Vibrator v = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			if (v != null)
				v.vibrate(100);
		}
	}

	private void log(String s) {
		Log.d(Values.DEBUG_TAG, s);
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
