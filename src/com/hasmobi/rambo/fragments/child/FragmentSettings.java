package com.hasmobi.rambo.fragments.child;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.adapters.SettingsAdapter;
import com.hasmobi.rambo.adapters.placeholders.SettingsSingle;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.supers.SettingChangeObserver;
import com.hasmobi.rambo.utils.AutoBoostBroadcast;
import com.hasmobi.rambo.utils.Debugger;
import com.hasmobi.rambo.utils.NotificationIcon;
import com.hasmobi.rambo.utils.Prefs;
import com.hasmobi.rambo.utils.ResManager;

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
				ResManager.getString(c, R.string.sett_name_autoboost),
				new AutoBoostSettingChanged());
		SettingsSingle setting2 = new SettingsSingle("notification_icon",
				ResManager.getString(c, R.string.sett_name_notif_icon),
				new NotificationIconSettingChanged());

		list.add(setting1);
		list.add(setting2);

		SettingsAdapter adapter = new SettingsAdapter(getActivity(), list);

		lvSettings.setAdapter(adapter);

		showView(lvSettings);
	}

	/**
	 * Get notified when the Autoboost preferences are changed (and
	 * enable/disable Autoboost accordingly)
	 */
	class AutoBoostSettingChanged implements SettingChangeObserver {

		public void changed() {
			Debugger d = new Debugger(c);
			Prefs p = new Prefs(c);
			Intent i = new Intent(c, AutoBoostBroadcast.class);
			if (p.isAutoboostEnabled()) {
				i.setAction(AutoBoostBroadcast.ACTION_AUTOBOOST_ENABLE);
				d.toast("Enabling autoboost");
			} else {
				i.setAction(AutoBoostBroadcast.ACTION_AUTOBOOST_DISABLE);
				d.toast("Autoboost disabled");
			}
			PendingIntent pi = PendingIntent.getBroadcast(c, 0, i,
					PendingIntent.FLAG_CANCEL_CURRENT);
			try {
				pi.send();
			} catch (CanceledException e) {
				Debugger.log("Can not start autobooster due to an exception.");
				Debugger.log(e.getMessage());
			}
		}

	}

	/**
	 * Get notified when the Notification icon preferences are changed (and
	 * enable/disable the Notification icon accordingly)
	 */
	class NotificationIconSettingChanged implements SettingChangeObserver {

		public void changed() {
			Intent i = new Intent(getActivity(), NotificationIcon.class);
			PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, i,
					PendingIntent.FLAG_CANCEL_CURRENT);
			try {
				pi.send();
			} catch (CanceledException e) {
				Debugger.log(e.getMessage());
			}
		}

	}

}
