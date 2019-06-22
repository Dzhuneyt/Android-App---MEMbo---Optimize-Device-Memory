package com.hasmobi.rambo.utils;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hasmobi.rambo.R;

/**
 * A simple dialog that is supposed to be shown onBackPressed
 * of the main activity of your app.
 *
 * The logic is the following:
 *
 * when showifNeeded() is called in an instance of this
 * class, it will show the dialog urging the user to
 * rate the app. If he decides to NOT rate by closing the
 * dialog or pressing back again we just exit the app
 * normally and repromt the user the next time he opens the
 * app. However, if he taps the button to rate and goes
 * to Google Play, don't show the current dialog again for 30
 * days to stop annoying him.
 */
public class RemindToRateDialog extends DialogFragment {

	public boolean rateButtonClicked = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dialog_remind_rate, container, false);

		Button b = (Button) v.findViewById(R.id.bOpenGooglePlay);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final String appPackageName = getActivity().getPackageName(); // getPackageName() from Context or Activity object
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
				} catch (android.content.ActivityNotFoundException anfe) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
				}

				long currentMillis = System.currentTimeMillis();

				long dayInMillis = 86400000;

				SharedPreferences sp = getActivity().getSharedPreferences("settings", 0);
				sp.edit().putLong("no_rate_remind_until", currentMillis + (dayInMillis * 30)).apply();

				rateButtonClicked = true;

				dismiss();
			}
		});

		return v;
	}

	/**
	 * Show the dialog only if is not snoozed for 30 days
	 * (happens when the user tapped the button to open
	 * Google Play and he most likely rated but we can't be
	 * sure, so we just snooze the dialog for 30 days and
	 * notify him again after that)
	 *
	 * @param c
	 * @param supportFragmentManager
	 *
	 * @return true if the dialog is needed and is shown, false
	 * if it's snoozed and skipped from showing right now
	 */
	public boolean showIfNeeded(Context c, FragmentManager supportFragmentManager){

		SharedPreferences sp = c.getSharedPreferences("settings", 0);

		long currentTime = System.currentTimeMillis();

		long noRemindMillisTimestamp = sp.getLong("no_rate_remind_until", -1);

		if (noRemindMillisTimestamp == -1 || currentTime > noRemindMillisTimestamp) {

			// Prevent duplicate of the same dialog
			Fragment exists = supportFragmentManager.findFragmentByTag("rate_us");
			if (exists != null)
				supportFragmentManager.beginTransaction().remove(exists).commit();

			this.show(supportFragmentManager, "rate_us");

			return true;
		}

		return false;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);

		getActivity().finish();
	}
}
