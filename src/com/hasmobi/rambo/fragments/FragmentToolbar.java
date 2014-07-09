package com.hasmobi.rambo.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hasmobi.lib.DResources;
import com.hasmobi.rambo.R;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.supers.DFragmentActivity;
import com.hasmobi.rambo.utils.Prefs;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.Values;

public class FragmentToolbar extends DFragment implements OnClickListener {

	Handler h = new Handler();
	Runnable r = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		r = new Runnable() {

			public void run() {

				Prefs p = new Prefs(c);
				long lastUpdate = p.getLastOptimizeTimestamp();

				final TextView tvToolbarRight = (TextView) getView()
						.findViewById(R.id.tvToolbarRight);

				if (tvToolbarRight != null) {
					try {
						String label = DResources.getString(c,
								R.string.last_optimized_toolbar);
						if (lastUpdate == 0) {
							label = String.format(label,
									DResources.getString(c, R.string.never));
						} else {
							String lastOptimizeLabel = (String) DateUtils
									.getRelativeDateTimeString(c, lastUpdate,
											DateUtils.SECOND_IN_MILLIS, 0, 0);

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

				h.postDelayed(this, 1000);
			}

		};

		if (h != null && r != null)
			h.post(r);
	}

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

		return v;
	}

	@Override
	public void onPause() {
		super.onPause();

		if (h != null && r != null)
			h.removeCallbacks(r);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (h != null && r != null)
			h.post(r);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bOptimize:
			RamManager rm = new RamManager(c);
			int killedApps = rm.killBgProcesses();

			try {
				Tracker t = ((DFragmentActivity) this.getActivity()).t;
				t.send(new HitBuilders.EventBuilder()
						.setCategory(Values.ANALYTICS_CATEGORY_RAM)
						.setAction(Values.ANALYTICS_ACTION_OPTIMIZE)
						.setValue(killedApps)
						.set(Values.ANALYTICS_LABEL_OPTIMIZED_APPS,
								String.valueOf(killedApps))
						.set(Values.ANALYTICS_LABEL_CONTEXT,
								Values.ANALYTICS_CONTEXTS_FOOTER_BUTTON)
						.build());
			} catch (Exception e) {
				Log.d(getClass().toString(), null, e);
			}
			break;
		}

	}

	@Override
	public void handleBroadcast(Context c, Intent i) {

		super.handleBroadcast(c, i);
	}

}
