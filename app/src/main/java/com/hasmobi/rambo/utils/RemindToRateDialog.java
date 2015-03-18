package com.hasmobi.rambo.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.widget.LikeView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hasmobi.rambo.R;
import com.hasmobi.rambo.supers.DFragmentActivity;

/**
 * A simple dialog that is supposed to be shown onBackPressed
 * of the main activity of your app.
 * <p/>
 * The logic is the following:
 * <p/>
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

		LinearLayout llStars = (LinearLayout) v.findViewById(R.id.llStarsHolder);
		ImageView ivGooglePlay = (ImageView) v.findViewById(R.id.ivGooglePlay);
		llStars.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openGooglePlay();
			}
		});
		ivGooglePlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openGooglePlay();
			}
		});

		RelativeLayout likeViewHolder = (RelativeLayout) v.findViewById(R.id.likeViewHolder);
		LikeView likeView = new LikeView(getActivity());
		likeView.setHorizontalAlignment(LikeView.HorizontalAlignment.LEFT);

		likeView.setObjectId("http://www.facebook.com/memboapp");
		likeViewHolder.addView(likeView);

		return v;
	}

	private void openGooglePlay() {
		final String appPackageName = getActivity().getPackageName(); // getPackageName() from Context or Activity object
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
		} catch (android.content.ActivityNotFoundException anfe) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
		}

		long currentMillis = System.currentTimeMillis();

		long dayInMillis = 86400000;

		SharedPreferences sp = Prefs.getSettings(getActivity().getBaseContext());
		sp.edit().putLong("no_rate_remind_until", currentMillis + (dayInMillis * 30)).apply();

		rateButtonClicked = true;

		dismiss();

		final Tracker t = ((DFragmentActivity) getActivity()).t;
		if (t != null) {
			// Build and send an Event.
			t.send(new HitBuilders.EventBuilder()
					.setCategory("Remind To Rate Dialog")
					.setAction("Opened Google Play")
					.build());
		}
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
	 * @return true if the dialog is needed and is shown, false
	 * if it's snoozed and skipped from showing right now
	 */
	public boolean showIfNeeded(Context c, FragmentManager supportFragmentManager) {

		SharedPreferences sp = Prefs.getSettings(c);

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
