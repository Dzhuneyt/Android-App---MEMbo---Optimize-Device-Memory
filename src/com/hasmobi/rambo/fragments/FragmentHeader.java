package com.hasmobi.rambo.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hasmobi.lib.DDebug;
import com.hasmobi.rambo.R;
import com.hasmobi.rambo.fragments.child.FragmentMainActions;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.utils.Debugger;

public class FragmentHeader extends DFragment implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.xml.fragment_header, null);

		// Setup app name click handling
		TextView tvAppName = (TextView) v.findViewById(R.id.tvAppNameHeader);
		if (tvAppName != null) {
			tvAppName.setOnClickListener(this);

			// Setup header font
			final Typeface face = Typeface.createFromAsset(getActivity()
					.getAssets(), "sttransmission_800_extrabold.otf");
			tvAppName.setTypeface(face);
		} else {
			DDebug.log(getClass().toString(), "Can not get view R.id.tvAppNameHeader");
		}

		return v;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvAppNameHeader:
			final FragmentTransaction ft = getFragmentManager()
					.beginTransaction();
			ft.replace(R.id.fMain, new FragmentMainActions(), "fMain").commit();
			break;
		}

	}

}
