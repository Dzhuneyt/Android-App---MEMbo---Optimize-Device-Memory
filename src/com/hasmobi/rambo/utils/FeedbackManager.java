package com.hasmobi.rambo.utils;

import com.hasmobi.rambo.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class FeedbackManager {

	Context c;

	public FeedbackManager(Context c) {
		this.c = c;
	}

	public void feedbackDialog() {
		String versionName = "n/a";
		int versionCode = 0;
		try {
			final PackageInfo v = c.getPackageManager().getPackageInfo(
					c.getPackageName(), 0);
			versionName = v.versionName;
			versionCode = v.versionCode;
		} catch (NameNotFoundException e) {
			// log("Can't find app version name and code for feedback.");
		}
		final String receivers[] = { Values.FEEDBACK_EMAIL };
		final String emailSubject = "Feedback for RAMbo";
		final String emailBody = "App version " + versionName + " ("
				+ versionCode + ")\n";
		final Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SEND);
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receivers);
		emailIntent.setType("plain/text");
		emailIntent
				.putExtra(android.content.Intent.EXTRA_SUBJECT, emailSubject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);
		try {
			c.startActivity(Intent.createChooser(emailIntent, c.getResources()
					.getString(R.string.feedback)));
		} catch (Exception e) {
			String message = c.getResources().getString(R.string.no_email_app);
			Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
		}
	}

	public void goToGooglePlay() {
		log("Going to Google Play");
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
				// Can't even open regular URLs in browser
				log("Can't start browser");
			}
		}
	}

	private void log(String s) {
		Log.d(Values.DEBUG_TAG, s);
	}
}
