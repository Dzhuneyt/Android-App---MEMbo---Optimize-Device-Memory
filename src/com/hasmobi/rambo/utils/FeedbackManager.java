package com.hasmobi.rambo.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

import com.hasmobi.lib.DDebug;
import com.hasmobi.rambo.R;

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
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			Intent i = Intent.createChooser(emailIntent, c.getResources()
					.getString(R.string.feedback));
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			c.startActivity(i);
		} catch (Exception e) {
			DDebug.log(null, e.getMessage(), e);
			String message = c.getResources().getString(R.string.no_email_app);
			Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
		}
	}

}
