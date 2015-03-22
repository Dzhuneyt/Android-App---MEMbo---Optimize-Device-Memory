package com.hasmobi.rambo.blocks;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.custom_views.PieView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RunningAppsBlockFragment extends Fragment {

	RamChangeListener listener = new RamChangeListener();

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_running_app_block, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		LinearLayout holder = (LinearLayout) view.findViewById(R.id.topConsumersContainer);

		Context c = getActivity().getBaseContext();
		ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager pm = c.getPackageManager();

		final List<ActivityManager.RunningAppProcessInfo> runningProcesses = am
				.getRunningAppProcesses();

		List<RunningAppProcessesList> list = new ArrayList<RunningAppProcessesList>();

		for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningProcesses) {
			final Debug.MemoryInfo memoryInfoArray = am
					.getProcessMemoryInfo(new int[]{runningAppProcessInfo.pid})[0];
			int memoryUsageByApp = memoryInfoArray.getTotalPss();
			list.add(new RunningAppProcessesList(runningAppProcessInfo, memoryUsageByApp));
		}

		Collections.sort(list);
		Collections.reverse(list);

		for (RunningAppProcessesList item : list) {
			String pkginfo = item.runningAppProcessInfo.pkgList[0];
			try {
				ApplicationInfo applicationInfo = pm.getApplicationInfo(pkginfo, 0);

				if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
					continue;
				}
			} catch (PackageManager.NameNotFoundException e) {
				continue;
			}
			Drawable icon = null;
			try {
				icon = pm.getApplicationIcon(pkginfo);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
			if (icon != null) {
				ImageView iv = new ImageView(c);
				iv.setImageDrawable(icon);
				int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 50, getResources().getDisplayMetrics());
				iv.setLayoutParams(new LinearLayout.LayoutParams(size, size, 1));
				iv.setPadding(10, 10, 10, 10);
				holder.addView(iv);
			}
		}

	}

	class RunningAppProcessesList implements Comparable {

		public ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
		public int memoryUsageByApp;

		public RunningAppProcessesList(ActivityManager.RunningAppProcessInfo runningAppProcessInfo, int memoryUsageByApp) {
			this.runningAppProcessInfo = runningAppProcessInfo;
			this.memoryUsageByApp = memoryUsageByApp;
		}

		@Override
		public int compareTo(@NonNull Object another) {
			RunningAppProcessesList compareTo = (RunningAppProcessesList) another;

			if (this.memoryUsageByApp > compareTo.memoryUsageByApp) {
				return 1;
			} else if (this.memoryUsageByApp < compareTo.memoryUsageByApp) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		listener.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		listener.stop();
	}

	class RamChangeListener {

		int updateIntervalSeconds = 3;

		final Handler h = new Handler();
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

					PieView pie = (PieView) getView().findViewById(R.id.pie);
					pie.setRam(totalRam, availableRam);

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
}
