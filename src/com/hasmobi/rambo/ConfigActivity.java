package com.hasmobi.rambo;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ConfigActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);

		setContentView(R.layout.activity_config);

		addPreferencesFromResource(R.xml.fragmented_preferences);
		if (getActionBar() != null) {
			getActionBar().hide();
			// getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		setupFontsAndListeners();
	}

	private void setupFontsAndListeners() {
		final TextView tv = (TextView) findViewById(R.id.appTitle);
		final Typeface face = Typeface.createFromAsset(getAssets(),
				"sttransmission_800_extrabold.otf");
		tv.setTypeface(face);

		tv.setOnClickListener(new OnClickListener() {

			// Go to the parent activity
			public void onClick(View v) {
				goHome();
			}

		});
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
			goHome();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void goHome() {
		Intent i = new Intent(getBaseContext(), StartActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
		finish();
	}

}
