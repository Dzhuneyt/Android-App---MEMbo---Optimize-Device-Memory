package com.hasmobi.rambo.supers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

public class DFragment extends Fragment {
	public boolean fragmentVisible = false;

	public Context c;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		c = getActivity().getBaseContext();

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onPause() {
		fragmentVisible = false;
		super.onPause();
	}

	@Override
	public void onResume() {
		fragmentVisible = true;
		super.onResume();
	}

	public void hideView(View v) {
		if (v != null)
			v.setVisibility(View.GONE);
	}

	public void showView(View v) {
		if (v != null)
			v.setVisibility(View.VISIBLE);
	}

}
