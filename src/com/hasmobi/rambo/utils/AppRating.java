package com.hasmobi.rambo.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.hasmobi.lib.DApp;
import com.hasmobi.lib.DException;
import com.hasmobi.rambo.R;

public class AppRating {

	private Context c;

	public void remindToRate(Context c) {
		this.c = c;

		Prefs p = new Prefs(c);

		// Get the app start count from preferences
		int appStartCount = p.getAppStartCount();

		// If the user has already rated the app, don't annoy him with the
		// reminder dialog over and over. Values.REMIND_RATE_EVERY_N_START is
		// how often (in app start counts) will he be reminded to rate the app.
		if (!p.isAppRated()) {
			if (appStartCount == 1) {
				// App first start, remind to rate
				showRemindRateDialog();
			} else if (appStartCount % Values.REMIND_RATE_EVERY_N_START == 0) {
				// Scheduled reminder to rate
			}
		} else {
			// App already rated. Don't show reminder dialog
		}

		appStartCountIncrease(appStartCount);

	}

	/**
	 * Log the new app start count for the next start
	 * 
	 * @param int currentAppStartCount
	 */
	private void appStartCountIncrease(int currentAppStartCount) {
		Prefs p = new Prefs(c);
		p.save("app_start_count", currentAppStartCount + 1);
	}

	/**
	 * Show the remind to rate dialog. Any checks whether or not it should be
	 * showed must be done before calling this method
	 */
	private void showRemindRateDialog() {
		final Dialog remindDialog = new Dialog(c);

		remindDialog.setTitle(R.string.remind_rate_dialog_title);
		remindDialog.setContentView(R.layout.dialog_remind_to_rate);
		TextView tvStartCount = (TextView) remindDialog
				.findViewById(R.id.tvRemindStartCount);
		tvStartCount.setText(ResManager.getString(c,
				R.string.remind_rate_dialog_content));
		Button bOpenMarket = (Button) remindDialog
				.findViewById(R.id.bOpenMarket);
		TextView tvHidePermanently = (TextView) remindDialog
				.findViewById(R.id.tvRemindHidePermanently);

		class ButtonListener implements OnClickListener {
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.tvRemindHidePermanently:

					// Never remind to rate anymore
					Prefs p = new Prefs(c);
					p.save("already_rated_app", true);

					break;
				case R.id.bOpenMarket:

					try {
						DApp.openGooglePlayStore(c);
					} catch (DException e) {
						e.printStackTrace();
					}

					break;
				}

				remindDialog.dismiss();
			}
		}

		// Setup the click listeners for the buttons inside the remind dialog
		bOpenMarket.setOnClickListener(new ButtonListener());
		tvHidePermanently.setOnClickListener(new ButtonListener());

		remindDialog.show();

	}

	private void goToGooglePlay() {
		Uri uri = Uri.parse("market://details?id=" + c.getPackageName());
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		try {
			// If Google Play app is installed
			c.startActivity(goToMarket);
		} catch (Exception e) {
			try {
				// Alternatively, open it in the browser
				c.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("https://play.google.com/store/apps/details?id="
								+ c.getPackageName())));
			} catch (Exception ex) {
				Debugger d = new Debugger(c);
				d.canNotGoToGooglePlay();
				// Can't even open regular URLs in browser
				// log("Can't start browser");
			}
		}
	}

}
