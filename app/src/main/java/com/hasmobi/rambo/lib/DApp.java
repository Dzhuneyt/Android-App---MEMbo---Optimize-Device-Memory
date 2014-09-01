package com.hasmobi.rambo.lib;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

public class DApp {

	/**
	 * Returns the current version code of the app
	 * 
	 * @param c
	 * @return
	 */
	public static int getCurrentAppVersionCode(Context c) {
		int currentVerCode = 0;
		try {
			currentVerCode = c.getPackageManager().getPackageInfo(
					c.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return currentVerCode;
	}

	/**
	 * Based on the provided Context, open the Google Play store page for the
	 * given app. If it fails, open it in the default web browser in Google
	 * Play.
	 * 
	 * @param c
	 *            Context
	 * @throws DException
	 */
	public static void openGooglePlayStore(Context c) throws DException {
		try {
			Uri uri = Uri.parse("market://details?id=" + c.getPackageName());
			Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

			// If Google Play app is installed
			c.startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			try {
				// Alternatively, open it in the browser
				c.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("https://play.google.com/store/apps/details?id="
								+ c.getPackageName())));
			} catch (Exception ex) {
				// Can't even open regular URLs in browser
				throw new DException(ex);
			}
		} catch (NullPointerException e) {
			throw new DException(e);
		} catch (Exception e) {
			throw new DException(e);
		}
	}

}
