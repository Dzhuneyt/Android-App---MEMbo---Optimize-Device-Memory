package com.hasmobi.rambo.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
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

	public static String PREF_NAME = "last_tos_accept_version_code";

	Context context;

	private static String TOS_DIALOG_FRAGMENT_NAME = "fragment_tos_dialog";

	public TermsOfUse(Context c) {
		this.context = c;
	}

	/**
	 * Check if the TOS for the current version code of the app have been
	 * accepted already
	 *
	 * @return
	 */
	public boolean accepted() {
		SharedPreferences p = Prefs.instance(context);
		int lastAcceptedVersion = p.getInt(PREF_NAME, 0);
		if (lastAcceptedVersion > 0) {
			int currentVerCode = 0;
			try {
				currentVerCode = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0).versionCode;
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
	 * Shows the TOS dialog (using an AsyncTask while loading the TOS content so
	 * the UI thread is not blocked)
	 */
	public static void showDialog(FragmentManager fm) {
		final DialogFragment d = new TosDialogFragment();

		if (fm != null) {
			FragmentTransaction ft = fm.beginTransaction();
			Fragment prev = fm.findFragmentByTag(TOS_DIALOG_FRAGMENT_NAME);

			if (prev != null) {
				Log.d("rambo", "2");
				ft.remove(prev);
			}
			ft.addToBackStack(null);

			// Create and show the TOS dialog.
			d.show(ft, TOS_DIALOG_FRAGMENT_NAME);
		}
	}

	static public class TosDialogFragment extends DialogFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			setStyle(0, DialogFragment.STYLE_NO_TITLE);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		                         Bundle savedInstanceState) {
			final View v = inflater.inflate(R.layout.tos_content, null, false);

			getDialog().setTitle("Terms of Use");

			final TextView tvTosHolder = (TextView) v
					.findViewById(R.id.tvTosHolder);

			String rawTosContent = getTosContent();
			if (rawTosContent.length() > 0) {
				final Spanned tosContent = Html.fromHtml(rawTosContent);
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						tvTosHolder.setText(tosContent);
					}
				});
			}

			Button bClose = (Button) v.findViewById(R.id.bClose);
			bClose.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});

			return v;
		}

		/**
		 * Read the raw txt file with the TOS as a String
		 *
		 * @return String
		 */
		private String getTosContent() {
			// read tos.txt raw file
			StringBuilder sb = new StringBuilder();
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
			} catch (IOException | NotFoundException e) {
				e.printStackTrace();
			}

			return sb.toString();

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
