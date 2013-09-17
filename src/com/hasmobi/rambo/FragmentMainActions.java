package com.hasmobi.rambo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FragmentMainActions extends DFragment implements OnClickListener {

	Context c;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		c = getActivity().getBaseContext();

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View layout = inflater.inflate(R.xml.fragment_main_actions, null);

		// Open Pie icon
//		LinearLayout i1 = (LinearLayout) layout.findViewById(R.id.llOpenPie);
//		// Running Apps icon
//		LinearLayout i2 = (LinearLayout) layout
//				.findViewById(R.id.llOpenRunningApps);
//
//		i1.setOnClickListener(this);
//		i2.setOnClickListener(this);

		return layout;
	}

	public void onClick(View v) {
//		switch (v.getId()) {
//		case R.id.llOpenPie:
//			Intent i = new Intent(c, PieActivity.class);
//			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//			startActivity(i);
//			break;
//		}
	}

}
