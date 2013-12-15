package com.hasmobi.rambo;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import com.hasmobi.rambo.fragments.FragmentToolbar;
import com.hasmobi.rambo.fragments.child.FragmentMainActions;
import com.hasmobi.rambo.supers.DFragmentActivity;
import com.hasmobi.rambo.utils.AutoBoostBroadcast;
import com.hasmobi.rambo.utils.ChangeLog;
import com.hasmobi.rambo.utils.Debugger;
import com.hasmobi.rambo.utils.FeedbackManager;
import com.hasmobi.rambo.utils.NotificationIcon;
import com.hasmobi.rambo.utils.Prefs;
import com.hasmobi.rambo.utils.ResManager;
import com.hasmobi.rambo.utils.TermsOfUse;
import com.hasmobi.rambo.utils.TypefaceSpan;
import com.hasmobi.rambo.utils.Values;

public class MainActivity extends DFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (getActionBar() != null) {

			// Apply a custom TypeFace to the ActionBar title
			try {
				SpannableString s = new SpannableString(ResManager.getString(c,
						R.string.app_name));
				s.setSpan(new TypefaceSpan(this, "notosansregular.ttf"), 0,
						s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				getActionBar().setTitle(s);
			} catch (Exception e) {
				log("Can not apply typeface to ActionBar title");
				log(e.getMessage());
			}
		}

		// Fix for orientation change on some devices where Fragments defined in
		// the XML layout do not inflate a view on orientation change, so we
		// need to use some other layout like LinearLayout and replace it with
		// the Fragment on Acitity create
		setupFragments();

		sendNotification();

		enableAutoboost();

		showChangelog();

		if (!Values.DEBUG_MODE)
			showTOS();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void setupFragments() {
		FragmentManager fm = getSupportFragmentManager();

		FrameLayout fl = (FrameLayout) findViewById(R.id.fHeader);
		fl.removeAllViews();
		hideView(R.id.fHeader);
		/*
		 * if (fm != null) { FragmentTransaction ft = fm.beginTransaction();
		 * ft.add(R.id.fHeader, new FragmentHeader()); ft.commit(); }
		 */

		fl = (FrameLayout) findViewById(R.id.fMain);
		fl.removeAllViews();
		if (fm != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.fMain, new FragmentMainActions());
			ft.commit();
		}

		fl = (FrameLayout) findViewById(R.id.fToolbar);
		fl.removeAllViews();
		if (fm != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.fToolbar, new FragmentToolbar());
			ft.commit();
		}

		// llMainActionRow1
	}

	private void sendNotification() {
		NotificationIcon.notify(c);
	}

	private void enableAutoboost() {
		final Prefs p = new Prefs(c);
		if (p.isAutoboostEnabled()) {
			final Intent i = new Intent(c, AutoBoostBroadcast.class);
			i.setAction(AutoBoostBroadcast.ACTION_SCREENON_AUTOBOOST_ENABLED);
			final PendingIntent pi = PendingIntent.getBroadcast(c, 0, i,
					PendingIntent.FLAG_CANCEL_CURRENT);
			try {
				pi.send();
			} catch (CanceledException e) {
				log("Can not start autobooster due to an exception.");
				log(e.getMessage());
			}
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
			startActivity(new Intent(this, ConfigActivity.class));
			// Fragment newFragment = new FragmentSettings();
			// try {
			// final FragmentTransaction ft = getSupportFragmentManager()
			// .beginTransaction();
			// ft.replace(R.id.fMain, newFragment, "ReplacementFragment");
			// ft.addToBackStack(null);
			// ft.commit();
			// } catch (Exception e) {
			// Debugger.log(e.getMessage());
			// Debugger d = new Debugger(c);
			// d.toast("Can not open new Fragment. Please, contact us at feedback@hasmobi.com");
			// }
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
