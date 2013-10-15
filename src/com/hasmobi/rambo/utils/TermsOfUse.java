package com.hasmobi.rambo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.hasmobi.rambo.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.webkit.WebView;

public class TermsOfUse {

	static String PREF_NAME = "last_tos_accept_version_code";

	Activity c;

	Context context;

	public TermsOfUse(Activity c) {
		this.c = c;
		this.context = c.getBaseContext();
	}

	public boolean accepted() {
		SharedPreferences p = Prefs.instance(c);
		int lastAcceptedVersion = p.getInt(PREF_NAME, 0);
		if (lastAcceptedVersion > 0) {
			int currentVerCode = 0;
			try {
				currentVerCode = c.getPackageManager().getPackageInfo(
						c.getPackageName(), 0).versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

			if (currentVerCode > 0) {
				if (currentVerCode > lastAcceptedVersion) {
					return false;
				} else {
					// Last accepted TOS app version matches the current version
					return true;
				}
			} else {
				// We can't determinate the current app version. This will most
				// likely persist, so don't force the user to accept the TOS on
				// every app start
				return true;
			}
		}
		return false;
	}

	public void show() {
		WebView wv = new WebView(c);

		// wv.setBackgroundColor(Color.BLACK);
		wv.loadDataWithBaseURL(null, this.getTosContent(), "text/html",
				"UTF-8", null);

		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(c, android.R.style.Theme_Dialog));
		builder.setTitle(
				ResManager.getString(context, R.string.tos_dialog_title))
				.setView(wv).setCancelable(false)
				// OK button
				.setPositiveButton(
						ResManager.getString(context,
								R.string.tos_accept_button),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								int currentVerCode = 0;
								try {
									currentVerCode = c.getPackageManager()
											.getPackageInfo(c.getPackageName(),
													0).versionCode;
								} catch (NameNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								// Mark the TOS as accepted for this app
								// version, but later app versions will show the
								// TOS again
								if (currentVerCode > 0)
									markAccepted(currentVerCode);
							}
						});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void markAccepted(int acceptAppVersion) {
		Prefs p = new Prefs(context);
		p.save(PREF_NAME, acceptAppVersion);
	}

	private String getTosContent() {
		// read changelog.txt file
		StringBuffer sb = new StringBuffer();
		try {
			InputStream ins = c.getResources().openRawResource(R.raw.tos);
			BufferedReader br = new BufferedReader(new InputStreamReader(ins));

			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				sb.append(line + "\n");
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Debugger.log(sb.toString().length() + " length of the string");

		return sb.toString();

	}
}
