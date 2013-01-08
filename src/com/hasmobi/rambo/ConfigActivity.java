package com.hasmobi.rambo;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class ConfigActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		addPreferencesFromResource(R.xml.fragmented_preferences);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onPause() {
		// Close the settings activity just in case
		finish();
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// NavUtils.navigateUpFromSameTask(this);
			Intent i = new Intent(getBaseContext(),
					StartActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(i);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
