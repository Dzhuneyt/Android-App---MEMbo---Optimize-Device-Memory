package com.hasmobi.rambo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hasmobi.rambo.lib.DApp;
import com.hasmobi.rambo.utils.Prefs;
import com.hasmobi.rambo.utils.TermsOfUse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @deprecated
 */
public class TosActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tos_content);

		if (getActionBar() != null)
			getActionBar().hide();

		new Thread(new Runnable() {
			@Override
			public void run() {
				final StringBuffer sb = new StringBuffer();
				try {
					InputStream ins = getResources().openRawResource(
							R.raw.tos);
					BufferedReader br = new BufferedReader(new InputStreamReader(
							ins));

					String line = null;
					while ((line = br.readLine()) != null) {
						line = line.trim();
						sb.append(line + "\n");
					}
					br.close();
				} catch (IOException | Resources.NotFoundException e) {
					e.printStackTrace();
				}

				if (sb.toString().length() > 0) {
					final CharSequence tosContentStr = Html.fromHtml(sb.toString());
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							TextView tosHolder = (TextView) findViewById(R.id.tvTosHolder);
							tosHolder.setText(tosContentStr);
						}
					});
				}
			}
		}).run();

		Button bAccept = (Button) findViewById(R.id.bAcceptTos);
		bAccept.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int currentAppVersionCode = DApp
						.getCurrentAppVersionCode(getBaseContext());
				new Prefs(getBaseContext()).save(TermsOfUse.PREF_NAME, currentAppVersionCode);

				startActivity(new Intent(getBaseContext(), MainActivity.class));
				finish();
			}
		});
	}
}
