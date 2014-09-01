package com.hasmobi.rambo.fragments.child;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.supers.DFragment;

public class FragmentMainActionsNew extends DFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View layout = inflater.inflate(R.layout.fragment_main_new, null);

		LinearLayout llFirst = (LinearLayout) layout.findViewById(R.id.llFirst);
		LinearLayout llSecond = (LinearLayout) layout.findViewById(R.id.llSecond);
		LinearLayout llThird = (LinearLayout) layout.findViewById(R.id.llThird);

		return layout;
	}

	@Override
	public void handleBroadcast(Context c, Intent i) {
		super.handleBroadcast(c, i);
	}

}
