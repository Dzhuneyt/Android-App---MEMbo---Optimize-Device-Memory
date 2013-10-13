package com.hasmobi.rambo.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.utils.Debugger;
import com.hasmobi.rambo.utils.Prefs;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.ResManager;

public class FragmentToolbar extends DFragment implements OnClickListener {

	ToolbarDataUpdater ToolbarDataUpdater = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.xml.fragment_toolbar, null);

		final Button bOptimize = (Button) v.findViewById(R.id.bOptimize);

		// Setup fonts
		Typeface bold = Typeface.createFromAsset(getActivity().getAssets(),
				"sttransmission_800_extrabold.otf");
		Typeface face = Typeface.createFromAsset(getActivity().getAssets(),
				"notosansregular.ttf");
		
		if (bOptimize != null) {
			bOptimize.setTypeface(bold);
			bOptimize.setOnClickListener(this);
		}

		final TextView tvLeft = (TextView) v.findViewById(R.id.tvToolbarLeft);
		final TextView tvRight = (TextView) v.findViewById(R.id.tvToolbarRight);

		if (tvLeft != null)
			tvLeft.setTypeface(bold);

		
		if (tvRight != null)
			tvRight.setTypeface(face);

		return v;
	}

	@Override
	public void onPause() {
		super.onPause();

		if (ToolbarDataUpdater != null)
			ToolbarDataUpdater.stop();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (ToolbarDataUpdater == null) {
			ToolbarDataUpdater = new ToolbarDataUpdater();
			ToolbarDataUpdater.start();
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bOptimize:
			RamManager rm = new RamManager(c);
			rm.killBgProcesses();

			try {
				ToolbarDataUpdater.stop();
				ToolbarDataUpdater.start();
			} catch (Exception e) {
			}
			break;
		}

	}

	private class ToolbarDataUpdater {
		Handler h = null;
		Runnable r = null;

		int totalRam = 0, freeRam = 0;
		long lastUpdate = 0; // timestamp

		/**
		 * Setup the Handler and Runnable in constructor. They are not ran until
		 * you call start()
		 */
		public ToolbarDataUpdater() {
			final RamManager rm = new RamManager(getActivity().getBaseContext());

			h = new Handler();
			r = new Runnable() {

				public void run() {
					if (fragmentVisible) {

						totalRam = rm.getTotalRam();
						freeRam = rm.getFreeRam();

						Prefs p = new Prefs(c);
						lastUpdate = p.getLastOptimizeTimestamp();

						updateLabel();

						h.postDelayed(this, 3000);
					}
				}

			};

		}

		public void start() {
			if (h == null) {
				h = new Handler();
			}
			if (r != null) {
				h.removeCallbacks(r);
			}
			h.post(r);
		}

		public void stop() {
			if (h != null && r != null)
				h.removeCallbacks(r);
		}

		private void updateLabel() {
			final TextView tvToolbarLeft = (TextView) getView().findViewById(
					R.id.tvToolbarLeft);
			final TextView tvToolbarRight = (TextView) getView().findViewById(
					R.id.tvToolbarRight);

			if (tvToolbarLeft != null) {
				int freeRamPercent = (freeRam * 100) / totalRam;
				try {
					String label = ResManager.getString(c,
							R.string.free_ram_percent);
					label = String.format(label, freeRamPercent);
					tvToolbarLeft.setText(label);
				} catch (Exception e) {
					Debugger.log("Can not update main actions label for free ram. Error: "
							+ e.getMessage());
				}
			}

			if (tvToolbarRight != null) {
				try {
					String label = ResManager.getString(c,
							R.string.last_optimized_toolbar);
					if (lastUpdate == 0) {
						label = String.format(label,
								ResManager.getString(c, R.string.never));
					} else {
						String lastOptimizeLabel = (String) DateUtils
								.getRelativeDateTimeString(c, lastUpdate,
										DateUtils.SECOND_IN_MILLIS, 0, 0);
						label = String.format(label, lastOptimizeLabel);
					}
					tvToolbarRight.setText(label);
				} catch (Exception e) {
					Debugger.log("Can not update main actions label for free ram. Error: "
							+ e.getMessage());
				}
			}

		}
	}
}
