package com.hasmobi.rambo.fragments;

import android.content.Context;
import android.content.Intent;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.xml.fragment_toolbar, null);

		final Button bOptimize = (Button) v.findViewById(R.id.bOptimize);

		// Setup fonts
		Typeface bold = Typeface.createFromAsset(getActivity().getAssets(),
				"comfortaa-regular.ttf");
		Typeface face = Typeface.createFromAsset(getActivity().getAssets(),
				"notosansregular.ttf");

		if (bOptimize != null) {
			bOptimize.setTypeface(bold);
			bOptimize.setOnClickListener(this);
		}

		final TextView tvRight = (TextView) v.findViewById(R.id.tvToolbarRight);

		if (tvRight != null)
			tvRight.setTypeface(face);

		if (ToolbarDataUpdater == null) {
			ToolbarDataUpdater = new ToolbarDataUpdater();
			ToolbarDataUpdater.start();
		} else {
			ToolbarDataUpdater.stop();
			ToolbarDataUpdater.start();
		}

		return v;
	}

	@Override
	public void onPause() {
		super.onPause();

		if (ToolbarDataUpdater != null) {
			ToolbarDataUpdater.stop();
		} else {
			ToolbarDataUpdater.stop();
			ToolbarDataUpdater.start();

		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (ToolbarDataUpdater == null) {
			ToolbarDataUpdater = new ToolbarDataUpdater();
			ToolbarDataUpdater.start();
		} else {
			ToolbarDataUpdater.stop();
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

	@Override
	public void handleBroadcast(Context c, Intent i) {
		if (fragmentVisible) {
			if (ToolbarDataUpdater == null) {
				ToolbarDataUpdater = new ToolbarDataUpdater();
				ToolbarDataUpdater.start();
			} else {
				ToolbarDataUpdater.stop();
				ToolbarDataUpdater.start();
			}
		}

		super.handleBroadcast(c, i);
	}

	private class ToolbarDataUpdater {
		Handler h = null;
		Runnable r = null;

		long lastUpdate = 0; // timestamp

		/**
		 * Setup the Handler and Runnable in constructor. They are not ran until
		 * you call start()
		 */
		public ToolbarDataUpdater() {
			Prefs p = new Prefs(c);
			lastUpdate = p.getLastOptimizeTimestamp();

			h = new Handler();
			r = new Runnable() {
				public void run() {
					h.postDelayed(this, 1000);

					final TextView tvToolbarRight = (TextView) getView()
							.findViewById(R.id.tvToolbarRight);

					if (tvToolbarRight != null) {
						try {
							String label = ResManager.getString(c,
									R.string.last_optimized_toolbar);
							if (lastUpdate == 0) {
								label = String
										.format(label, ResManager.getString(c,
												R.string.never));
							} else {
								String lastOptimizeLabel = (String) DateUtils
										.getRelativeDateTimeString(c,
												lastUpdate,
												DateUtils.SECOND_IN_MILLIS, 0,
												0);

								lastOptimizeLabel = (String) DateUtils
										.getRelativeTimeSpanString(lastUpdate,
												System.currentTimeMillis(), 0);
								label = String.format(label, lastOptimizeLabel);
							}
							tvToolbarRight.setText(label);
						} catch (Exception e) {
							log("Can not update toolbar");
							log(e.getMessage());
						}
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
			h.postDelayed(r, 100);
		}

		public void stop() {
			if (h != null && r != null)
				h.removeCallbacks(r);
		}

	}
}
