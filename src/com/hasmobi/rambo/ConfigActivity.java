package com.hasmobi.rambo;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.hasmobi.rambo.utils.AutoBoostBroadcast;
import com.hasmobi.rambo.utils.Debugger;
import com.hasmobi.rambo.utils.NotificationIcon;
import com.hasmobi.rambo.utils.Prefs;

public class ConfigActivity extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {

	Context c = null;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.activity_config);

		c = getBaseContext();

		addPreferencesFromResource(R.xml.fragmented_preferences);
		if (getActionBar() != null) {
			getActionBar().hide();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			goHome();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void goHome() {
		Intent i = new Intent(getBaseContext(), MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
		finish();
	}

	public void onSharedPreferenceChanged(SharedPreferences sp, String s) {
		if (s.equalsIgnoreCase("enable_autoboost")) {
			Prefs p = new Prefs(c);
			Intent i = new Intent(c, AutoBoostBroadcast.class);
			if (p.isAutoboostEnabled()) {
				i.setAction(AutoBoostBroadcast.ACTION_SCREENON_AUTOBOOST_ENABLED);
			} else {
				i.setAction(AutoBoostBroadcast.ACTION_SCREENON_AUTOBOOST_DISABLED);
			}
			PendingIntent pi = PendingIntent.getBroadcast(c, 0, i,
					PendingIntent.FLAG_CANCEL_CURRENT);
			try {
				pi.send();
			} catch (CanceledException e) {
				Debugger.log("Can not start autobooster due to an exception.");
				Debugger.log(e.getMessage());
			}
		} else if (s.equalsIgnoreCase("notification_icon")) {
			Intent i = new Intent(c, NotificationIcon.class);
			PendingIntent pi = PendingIntent.getBroadcast(c, 0, i,
					PendingIntent.FLAG_CANCEL_CURRENT);
			try {
				pi.send();
			} catch (CanceledException e) {
				Debugger.log(e.getMessage());
			}
		}
	}

}
