package com.hasmobi.rambo.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.utils.RamManager;

public class FragmentToolbar extends DFragment implements OnClickListener {

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
		final Typeface face = Typeface.createFromAsset(getActivity()
				.getAssets(), "sttransmission_800_extrabold.otf");
		bOptimize.setTypeface(face);

		bOptimize.setOnClickListener(this);

		return v;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bOptimize:
			RamManager rm = new RamManager(getActivity());
			rm.killBgProcesses();
			break;
		}

	}
}
