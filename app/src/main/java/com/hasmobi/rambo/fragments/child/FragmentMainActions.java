package com.hasmobi.rambo.fragments.child;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.blocks.RunningAppsBlockFragment;
import com.hasmobi.rambo.lib.DDebug;
import com.hasmobi.rambo.lib.DResources;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.utils.FontHelper;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.TypefaceSpan;
import com.hasmobi.rambo.utils.custom_views.StatsBlock;

public class FragmentMainActions extends DFragment {

	StatsBlock statsBlockRam, statsBlockBattery, statsBlockDisk;

	private BroadcastReceiver batteryChangedBroadcastHandler;
	private RamChangeListener ramChangeListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View layout = inflater.inflate(R.layout.fragment_main_new, null);

		final Typeface custom_font = Typeface.createFromAsset(getActivity().getBaseContext().getAssets(), "comfortaa-regular.ttf");

		RelativeLayout holderRAMFragment = (RelativeLayout) layout.findViewById(R.id.holderRAMFragment);

		getActivity().getSupportFragmentManager().beginTransaction()
				.add(R.id.holderRAMFragment, new RunningAppsBlockFragment())
				.commit();

		statsBlockRam = (StatsBlock) layout.findViewById(R.id.statsBlockRam);

		// https://code.google.com/p/syspower/source/browse/trunk/android-files/src/org/spot/android/collectors/AndroidPowerCollector.java
		statsBlockBattery = (StatsBlock) layout.findViewById(R.id.statsBlockBattery);
		statsBlockDisk = (StatsBlock) layout.findViewById(R.id.statsBlockDisk);

		statsBlockDisk.setHeader(DResources.getString(c, R.string.disk_usage));
		statsBlockBattery.setHeader(DResources.getString(c, R.string.battery_usage));
		statsBlockRam.setHeader(DResources.getString(c, R.string.memory_usage));

		statsBlockDisk.setDetailsButtonLabel(DResources.getString(c, R.string.breakdown));
		statsBlockBattery.setDetailsButtonLabel(DResources.getString(c, R.string.save_battery));
		statsBlockRam.setDetailsButtonLabel(DResources.getString(c, R.string.running_processes));

		statsBlockRam.setDetailsButtonClickAction(new Runnable() {
			@Override
			public void run() {
				goToFragment(new FragmentRunningApps(), true);
			}
		});

		statsBlockDisk.setDetailsButtonClickAction(new Runnable() {
			@Override
			public void run() {
				goToFragment(new FragmentDiskUsage(), true);
			}
		});
		statsBlockBattery.setDetailsButtonClickAction(new Runnable() {
			@Override
			public void run() {
				goToFragment(new FragmentBatteryNew(), true);
			}
		});


		if (getActivity().getActionBar() != null) {
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
			SpannableString s = new SpannableString(DResources.getString(c,
					R.string.app_name));
			s.setSpan(new TypefaceSpan(getActivity(), FontHelper.ACTIONBAR_TITLE), 0,
					s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			getActivity().getActionBar().setTitle(s);
		}

		return layout;
	}

	@Override
	public void handleBroadcast(Context c, Intent i) {
		super.handleBroadcast(c, i);
	}

	@Override
	public void onPause() {
		super.onPause();

		if (batteryChangedBroadcastHandler != null) {
			this.getActivity().getBaseContext().unregisterReceiver(batteryChangedBroadcastHandler);
		}
		if (ramChangeListener != null) {
			ramChangeListener.stop();
		}
	}

	@Override
	public void onResume() {
		super.onResume();


		// Start listening for battery level changes on app start
		if (batteryChangedBroadcastHandler == null)
			batteryChangedBroadcastHandler = new BatteryChangedBroadcastReceiver();
		if (statsBlockBattery != null) {
			statsBlockBattery.setLoading(true);
		}
		this.getActivity().getBaseContext().registerReceiver(batteryChangedBroadcastHandler, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		// Start updating free/taken RAM stats block
		if (ramChangeListener == null)
			ramChangeListener = new RamChangeListener();

		if (statsBlockRam != null) {
			statsBlockRam.setLoading(true);
		}
		ramChangeListener.start();

		statsBlockDisk.setLoading(true);
		new DiskSpaceUsageCalculator().calculate();
	}

	/**
	 * A broadcast receiver subclass that should update the StatsBlock related to the battery
	 */
	class BatteryChangedBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Battery level changed. Get the current level
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

			float batteryPct = level / (float) scale;
			int intAvailablePercent = (int) (batteryPct * 100);

			if (statsBlockBattery != null) {
				statsBlockBattery.setLoading(false);
				statsBlockBattery.setPieValues(100, (100 - intAvailablePercent));
				statsBlockBattery.setFreeString(intAvailablePercent + " %");
				statsBlockBattery.setTakenString((100 - intAvailablePercent) + " %");
			}
		}
	}

	class RamChangeListener {

		int updateIntervalSeconds = 3;

		Handler h = new Handler();
		Runnable r;
		RamManager rm;

		public void start() {
			r = new Runnable() {
				@Override
				public void run() {

					if (rm == null)
						rm = new RamManager(getActivity().getBaseContext());

					int availableRam = rm.getFreeRam();
					int totalRam = rm.getTotalRam();

					if (statsBlockRam != null) {
						statsBlockRam.setLoading(false);
						statsBlockRam.setPieValues(totalRam, availableRam);
						statsBlockRam.setFreeString(availableRam + " MB");
						statsBlockRam.setTakenString((totalRam - availableRam) + " MB");
					}

					h.postDelayed(r, 1000 * updateIntervalSeconds);
				}
			};
			h.post(r);
		}

		public void stop() {
			if (r != null)
				h.removeCallbacks(r);
		}
	}

	class DiskSpaceUsageCalculator {

		private long availableBytesInternal, totalBytesInternal, availableBytesSD, totalBytesSD;

		public void calculate() {
			this.recalculate();

			long totalBytes = (totalBytesInternal + totalBytesSD);
			long totalAvailableBytes = (availableBytesInternal + availableBytesSD);

			statsBlockDisk.setPieValues(totalBytes, totalAvailableBytes);

			String freeString = this.bytesToMb(totalAvailableBytes);
			String takenString = this.bytesToMb(totalBytes - totalAvailableBytes);
			statsBlockDisk.setFreeString(freeString);
			statsBlockDisk.setTakenString(takenString);
			statsBlockDisk.setLoading(false);
		}

		@SuppressWarnings("deprecation")
		private void recalculate() {

			// Calculate usage in SD card
			StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
			int sdkInt = Build.VERSION.SDK_INT;

			if (sdkInt < Build.VERSION_CODES.JELLY_BEAN_MR2) {
				int blockSize = statFs.getBlockSize();
				availableBytesSD = ((long) statFs.getAvailableBlocks()) * blockSize;
				totalBytesSD = ((long) statFs.getBlockCount()) * blockSize;
			} else {
				availableBytesSD = statFs.getAvailableBytes();
				totalBytesSD = statFs.getTotalBytes();
			}

			// Calculate usage in internal memory
			statFs.restat(Environment.getDataDirectory().getPath());
			if (sdkInt < Build.VERSION_CODES.JELLY_BEAN_MR2) {
				int blockSize = statFs.getBlockSize();
				availableBytesInternal = ((long) statFs.getAvailableBlocks()) * blockSize;
				totalBytesInternal = ((long) statFs.getBlockCount()) * blockSize;
			} else {
				availableBytesInternal = statFs.getAvailableBytes();
				totalBytesInternal = statFs.getTotalBytes();
			}

			Log.d(getClass().toString(), "Disk usage calculation result (bytes):"
					+ "Internal (" + availableBytesInternal + "," + totalBytesInternal
					+ ") External (" + availableBytesSD + "," + totalBytesSD + ")");
		}

		private String bytesToMb(long bytes) {
			return this.humanReadableByteCount(bytes, false);
		}

		public String humanReadableByteCount(long bytes, boolean si) {
			int unit = si ? 1000 : 1024;
			if (bytes < unit) return bytes + " B";
			int exp = (int) (Math.log(bytes) / Math.log(unit));
			String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
			return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		DDebug.log(getClass().getSimpleName(), "onCreateOptionsMenu()");

		// Append the custom menu to the current menu items
		menu.clear();
		inflater.inflate(R.menu.main, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// Return to parent (main) fragment
				// (But this is the main fragment so do nothing)
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
