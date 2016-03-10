package com.hasmobi.rambo.blocks;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.custom_views.PieView;

public class RunningAppsBlockFragment extends Fragment {

	RamChangeListener ramChangeListener = new RamChangeListener();

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_running_app_block, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		ramChangeListener.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		ramChangeListener.stop();
	}

	class RamChangeListener {

		private int updateIntervalSeconds = 3;

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
