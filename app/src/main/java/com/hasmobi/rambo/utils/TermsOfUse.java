package com.hasmobi.rambo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.lib.DApp;
import com.hasmobi.rambo.supers.DFragmentActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TermsOfUse {

	public static final String PREF_NAME_ANALYTICS = "tos_analytics_accepted";

	static String PREF_NAME = "last_tos_accept_version_code";

	DFragmentActivity c;

	Context context;

	private static String TOS_DIALOG_FRAGMENT_NAME = "fragment_tos_dialog";

	public TermsOfUse(DFragmentActivity c) {
		this.c = c;
		this.context = c.getBaseContext();
	}

	/**
	 * Check if the TOS for the current version code of the app have been
	 * accepted already
	 *
	 * @return
	 */
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
				return currentVerCode <= lastAcceptedVersion;
			} else {
				// We can't determinate the current app version. This will most
				// likely persist, so don't force the user to accept the TOS on
				// every app start
				return true;
			}
		}
		return false;
	}

	/**
	 * Show the TOS dialog if not shown and accepted already
	 */
	public void showIfNeeded() {
		if (!this.accepted())
			this.show();
	}

	/**
	 * Shows the TOS dialog (using an AsyncTask while loading the TOS content so
	 * the UI thread is not blocked)
	 */
	public void show() {
		DialogFragment d = new TosDialogFragment();

		FragmentManager fm = c.getSupportFragmentManager();

		if (fm != null) {
			FragmentTransaction ft = fm.beginTransaction();
			Fragment prev = fm.findFragmentByTag(TOS_DIALOG_FRAGMENT_NAME);

			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);

			// Create and show the TOS dialog.
			d.show(ft, TOS_DIALOG_FRAGMENT_NAME);
		}
	}

	static public class TosDialogFragment extends DialogFragment implements
			OnClickListener {
		// Becomes true when the TOS dialog is accepted with a
		// checked "I accept to provide anonymous usage statistics"
		boolean analyticsAccepted = false;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		                         Bundle savedInstanceState) {
			final View v = inflater.inflate(R.layout.tos_content, null, false);

			getDialog().setCanceledOnTouchOutside(false);
			this.setCancelable(false);

			getDialog().setTitle("Terms of Use");

			new Thread(new Runnable() {
				public void run() {
					String rawTosContent = getTosContent();
					if (rawTosContent.length() > 0) {
						final Spanned tosContent = Html.fromHtml(rawTosContent);
						getActivity().runOnUiThread(new Runnable() {
							public void run() {
								final TextView tvTosHolder = (TextView) v
										.findViewById(R.id.tvTosHolder);
								tvTosHolder.setText(tosContent);
							}
						});
					}
				}
			}).start();

			Button bAccept = (Button) v.findViewById(R.id.bAcceptTos);
			if (bAccept != null)
				bAccept.setOnClickListener(this);

			return v;
		}

		/**
		 * Read the raw txt file with the TOS as a String
		 *
		 * @return String
		 */
		private String getTosContent() {
			// read tos.txt raw file
			StringBuffer sb = new StringBuffer();
			try {
				InputStream ins = getActivity().getResources().openRawResource(
						R.raw.tos);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						ins));

				String line = null;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					sb.append(line + "\n");
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				e.printStackTrace();
			}

			return sb.toString();

		}

		public void onClick(View clicked) {
			switch (clicked.getId()) {
				case R.id.bAcceptTos: // "TOS accepted" button clicked

					if (getView() == null)
						return;

					// See if the user agrees to share his
					// anonymous usage statistic with us (Google
					// Analytics) and save it in
					// SharedPreferences
					final CheckBox cbAnalytics = (CheckBox) getView().findViewById(
							R.id.cbAnalyticsAgree);
					analyticsAccepted = cbAnalytics.isChecked();

					getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
							.putBoolean(PREF_NAME_ANALYTICS, analyticsAccepted)
							.apply();

					final int currentAppVersionCode = DApp
							.getCurrentAppVersionCode(getActivity());
					markAccepted(currentAppVersionCode);

					// Close the TOS dialog
					dismiss();

					if (analyticsAccepted) {
						// Recreate the activity so Google Analytics to
						// reinitialize
						getActivity().recreate();
					}

					break;
			}

		}

		/**
		 * Mark the TOS associated with the provided version code as accepted
		 * (writes a flag in SharedPreferences)
		 *
		 * @param acceptAppVersion
		 */
		public void markAccepted(int acceptAppVersion) {
			Prefs p = new Prefs(getActivity());
			p.save(PREF_NAME, acceptAppVersion);
		}
	}
}
