package com.hasmobi.rambo.fragments.child;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.blocks.RunningAppsBlockFragment;
import com.hasmobi.rambo.lib.DDebug;
import com.hasmobi.rambo.lib.DResources;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.utils.FontHelper;
import com.hasmobi.rambo.utils.RamManager;
import com.hasmobi.rambo.utils.TypefaceSpan;

public class FragmentMain extends DFragment {

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
		View layout = inflater.inflate(R.layout.fragment_main, container, false);

		final Typeface custom_font = Typeface.createFromAsset(getActivity().getBaseContext().getAssets(), "comfortaa-regular.ttf");

		getActivity().getSupportFragmentManager().beginTransaction()
				.add(R.id.holderRAMFragment, new RunningAppsBlockFragment())
				.commit();

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

		if (ramChangeListener != null) {
			ramChangeListener.stop();
		}
	}

	@Override
	public void onResume() {
		super.onResume();


		// Start updating free/taken RAM stats block
		if (ramChangeListener == null)
			ramChangeListener = new RamChangeListener();
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
