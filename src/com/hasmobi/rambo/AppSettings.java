package com.hasmobi.rambo;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class AppSettings extends PreferenceActivity {

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		addPreferencesFromResource(R.xml.fragmented_preferences);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
