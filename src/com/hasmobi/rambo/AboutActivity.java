package com.hasmobi.rambo;

import com.hasmobi.rambo.utils.FeedbackManager;
import com.hasmobi.rambo.utils.Values;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AboutActivity extends Activity {
	
	Context c;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		if (getActionBar() != null) {
			getActionBar().hide();
		}

		fonts();

		getAppVersion();
		
		c = getBaseContext();
	}

	private void log(String s) {
		Log.d(Values.DEBUG_TAG, s);
	}

	private void getAppVersion() {
		String versionName = "";
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			versionName = "v" + pInfo.versionName;
		} catch (NameNotFoundException e) {
			log(e.getMessage());
		}

		final TextView aboutHeader = (TextView) findViewById(R.id.aboutHeader);
		String aboutText = String.format(
				getResources().getString(R.string.aboutText), versionName);
		aboutHeader.setText(aboutText);
	}

	private void fonts() {
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

	private void goHome() {
		Intent i = new Intent(getBaseContext(), StartActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		FeedbackManager fm = new FeedbackManager(c);

		switch (item.getItemId()) {
		case R.id.menuSettings:
			Intent i = new Intent(c, ConfigActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(i);
			break;
		case R.id.menuUpdateApp:
			fm.goToGooglePlay();
			break;
		case R.id.menuFeedback:
			fm.feedbackDialog();
			break;
		case R.id.menuQuit:
			finish();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

}
