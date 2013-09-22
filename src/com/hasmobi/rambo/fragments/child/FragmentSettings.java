package com.hasmobi.rambo.fragments.child;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.hasmobi.rambo.R;
import com.hasmobi.rambo.adapters.SettingsAdapter;
import com.hasmobi.rambo.adapters.placeholders.SettingsSingle;
import com.hasmobi.rambo.supers.DFragment;

public class FragmentSettings extends DFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.xml.fragment_settings, null);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		ListView lvSettings = (ListView) getView()
				.findViewById(R.id.lvSettings);

		List<SettingsSingle> list = new ArrayList<SettingsSingle>();

		SettingsSingle setting1 = new SettingsSingle("enable_autoboost",
				"Constant Autoboost");

		list.add(setting1);
		list.add(setting1);

		SettingsAdapter adapter = new SettingsAdapter(getActivity(), list);

		lvSettings.setAdapter(adapter);

		showView(lvSettings);
	}

}
