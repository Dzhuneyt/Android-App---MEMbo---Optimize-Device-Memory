package com.hasmobi.rambo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hasmobi.rambo.utils.TermsOfUse;


public class SplashActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		if (getActionBar() != null)
			getActionBar().hide();

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new SplashFragment())
					.commit();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_splash, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class SplashFragment extends Fragment {

		private boolean isTosAccepted() {
			return getActivity().getSharedPreferences("settings", MODE_PRIVATE).getBoolean("tos_accepted", false);
		}

		private void markTosAccepted() {
			getActivity().getSharedPreferences("settings", MODE_PRIVATE).edit().putBoolean("tos_accepted", true).apply();
		}

		public SplashFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		                         Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_splash, container, false);
			TextView tvAppName = (TextView) rootView.findViewById(R.id.tvAppName);

			Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "notosansregular.ttf");

			tvAppName.setTypeface(tf);

			return rootView;
		}

		@Override
		public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
			super.onViewCreated(rootView, savedInstanceState);

			TextView tvTOSLink = (TextView) rootView.findViewById(R.id.tvTOSLink);

			tvTOSLink.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TermsOfUse.showDialog(getActivity().getSupportFragmentManager());
				}
			});

			if (!isTosAccepted()) {
				LinearLayout llTosBlock = (LinearLayout) rootView.findViewById(R.id.llTosBlock);
				llTosBlock.setVisibility(View.VISIBLE);
				Button bContinue = (Button) llTosBlock.findViewById(R.id.bContinue);
				bContinue.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						markTosAccepted();
						getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
						getActivity().finish();
					}
				});
			} else {
				// Do a short delay and start the main activity
				final Runnable r = new Runnable() {
					@Override
					public void run() {
						getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
						getActivity().finish();
					}
				};
				final Handler h = new Handler();
				h.postDelayed(r, 1000);
			}
		}
	}
}
