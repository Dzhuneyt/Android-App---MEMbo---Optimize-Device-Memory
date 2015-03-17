package com.hasmobi.rambo.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.lib.DDebug;
import com.hasmobi.rambo.lib.DHardware;
import com.hasmobi.rambo.lib.DResources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class RamManager {

    static public String ACTION_RAM_MANAGER = "ram_manager_run";

    private class ProcessToKill {
        // A class object to keep processes to be killed
        private String packageName;
    }

    Context context = null;
    Prefs prefs;

    /**
     * Initialize this class
     *
     * @param c - Activity or Context object
     */
    public RamManager(Context c) {
        this.context = c;
        this.prefs = new Prefs(c);
    }

    /**
     * Kill a single background activity by its package name
     *
     * @param packageName
     */
    public void killPackage(String packageName) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(packageName);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        // Vibrate after killing?
        DHardware.vibrate(context, 100);

        this.broadcast();
    }

	/**
	 * Kill all processes in a non-silent (vibrating) manner
	 *
	 * @return
	 */
    public int killBgProcesses() {
        return this.killAll(false);
    }

    /**
     * Kill all background apps available to be killed.
     *
     * @param silent - Whether or not to display a Toast and vibrate after killing
     * @return
     */
    public int killAll(boolean silent) {

        int killCount = 0;

        // Get all apps to exclude from the preferences
        final SharedPreferences excludedList = context.getSharedPreferences(
                "excluded_list", 0);

        final ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        final List<RunningAppProcessInfo> runningProcesses = am
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

                processesToKillArr.add(processToKill);
            }
        }

        if (processesToKillArr.size() > 0) {

            final int oldFreeRam = new RamManager(context).getFreeRam();

            for (ProcessToKill p : processesToKillArr) {
                // Kill this process
                am.killBackgroundProcesses(p.packageName);
                log(p.packageName + "[KILLING]");
                // Increase the counter by one
                killCount++;
            }

            final int newFreeRam = new RamManager(context).getFreeRam();
            int savedRam = newFreeRam - oldFreeRam;

            savedRam = ((savedRam < 0) ? 0 : savedRam);

            logSavedRam(savedRam);

            // Notify user that optimization is completed
            if (!silent) {
                String toDisplay = DResources.getString(context,
                        R.string.memory_optimized_toast);
                toDisplay = String.format(toDisplay, killCount, savedRam);

                Toast.makeText(context.getApplicationContext(), toDisplay,
                        Toast.LENGTH_LONG).show();
            }

        } else {
            log("No apps to kill");
            try {
                if (!silent) {
                    Toast.makeText(
                            context.getApplicationContext(),
                            context.getResources().getString(
                                    R.string.no_apps_killed),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // Can't get resources or can't display toast
                log("Can not get resources or can not display toast");
            }
        }

        // Should the device vibrate after killing?
        final SharedPreferences p = prefs.instance();
        DHardware.vibrate(context, 100);

        this.broadcast();

        prefs.saveLastOptimizeTimestamp(System.currentTimeMillis());

        return killCount;
    }

    private void log(String s) {
        Log.d(Values.DEBUG_TAG, s);
    }

    /*
     * Log the increment number of MBs of RAM this app has cleared. Will be used
     * later to show it to the user for various marketing reasons.
     */
    private void logSavedRam(int savedRam) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor e = prefs.edit();

        int totalSavedUntilNow = 0;

        try {
            totalSavedUntilNow = prefs.getInt("total_memory_saved", 0);
        } catch (Exception ex) {

        }

        totalSavedUntilNow = totalSavedUntilNow + savedRam;

        e.putInt("total_memory_saved", totalSavedUntilNow);

        final String firstBoostTimestamp = prefs.getString(
                "first_boost_timestamp", null);
        if (firstBoostTimestamp == null) {
            long currentTime = System.currentTimeMillis();
            e.putString("first_boost_timestamp", "" + currentTime);
        }

        e.commit();
    }

    /*
     * Gets the available memory on the device in MB or 0 on error
     */
    public int getFreeRam() {
        MemoryInfo mi = new MemoryInfo();
        final ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        am.getMemoryInfo(mi);
        int freeRam = 0;
        try {
            freeRam = (int) (mi.availMem / 1048576L);
        } catch (Exception e) {
        }
        return freeRam;
    }

    /*
     * Gets the total available memory on the device in MB or 0 on error
     */
    public int getTotalRam() {
        int tm = 0;
        try {
            final RandomAccessFile r = new RandomAccessFile("/proc/meminfo",
                    "r");
            String load = r.readLine();
            String[] totrm = load.split(" kB");
            String[] trm = totrm[0].split(" ");
            tm = Integer.parseInt(trm[trm.length - 1]);
            tm = Math.round(tm / 1024);
            r.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tm;
    }

    /**
     * Issues a generic app-wide broadcast when a full memory optimization is
     * done. Useful for Widgets or other classes that depend on this action to
     * capture and update accordingly
     */
    private void broadcast() {
        Intent i = new Intent(ACTION_RAM_MANAGER);
        context.sendBroadcast(i);

        DDebug.log(getClass().toString(), "Sending " + ACTION_RAM_MANAGER
                + " broadcast by " + this.getClass().getSimpleName());
    }
}
