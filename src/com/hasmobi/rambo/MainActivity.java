package com.hasmobi.rambo;

import com.hasmobi.rambo.supers.DFragmentActivity;
import com.hasmobi.rambo.utils.AutoBoostBroadcast;
import com.hasmobi.rambo.utils.Debugger;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends DFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (getActionBar() != null) {
			getActionBar().hide();
		}

		sendNotification();
	}

	private void sendNotification() {
		Intent i = new Intent(c, AutoBoostBroadcast.class);
		PendingIntent pi = PendingIntent.getBroadcast(c, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		try {
			pi.send();
		} catch (CanceledException e) {
			Debugger.log("Can not start autobooster due to an exception.");
			Debugger.log(e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
