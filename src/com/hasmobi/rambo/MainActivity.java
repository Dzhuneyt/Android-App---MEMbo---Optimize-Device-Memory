package com.hasmobi.rambo;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.hasmobi.rambo.fragments.child.FragmentSettings;
import com.hasmobi.rambo.supers.DFragmentActivity;
import com.hasmobi.rambo.utils.AutoBoostBroadcast;
import com.hasmobi.rambo.utils.ChangeLog;
import com.hasmobi.rambo.utils.Debugger;
import com.hasmobi.rambo.utils.FeedbackManager;
import com.hasmobi.rambo.utils.TermsOfUse;

public class MainActivity extends DFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (getActionBar() != null) {
			getActionBar().hide();
		}

		sendNotification();

		showChangelog();

		showTOS();
	}

	private void sendNotification() {
		Intent i = new Intent(c, AutoBoostBroadcast.class);
		PendingIntent pi = PendingIntent.getBroadcast(c, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		try {
			pi.send();
		} catch (CanceledException e) {
			Debugger.log("Can not start autobooster due to an exception.");
			Debugger.log(e.getMessage());
		}
	}

	private void showChangelog() {
		try {
			ChangeLog cl = new ChangeLog(this);
			if (cl.firstRun())
				cl.getLogDialog().show();
		} catch (Exception e) {
			Debugger.log("Can not display changelog dialog for some reason");
		}
	}

	private void showTOS() {
		TermsOfUse tos = new TermsOfUse(this);

		if (!tos.accepted())
			tos.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FeedbackManager fm = new FeedbackManager(c);

		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_feedback:
			fm.feedbackDialog();
			return true;
		case R.id.action_update:
			fm.goToGooglePlay();
			return true;
		case R.id.action_settings:
			Fragment newFragment = new FragmentSettings();
			try {
				final FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.fMain, newFragment, "ReplacementFragment");
				ft.addToBackStack(null);
				ft.commit();
			} catch (Exception e) {
				Debugger.log(e.getMessage());
				Debugger d = new Debugger(c);
				d.toast("Can not open new Fragment. Please, contact us at feedback@hasmobi.com");
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
