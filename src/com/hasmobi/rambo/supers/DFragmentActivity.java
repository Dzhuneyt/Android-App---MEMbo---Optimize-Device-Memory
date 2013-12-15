package com.hasmobi.rambo.supers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.hasmobi.rambo.utils.Debugger;
import com.hasmobi.rambo.utils.Values;

public class DFragmentActivity extends FragmentActivity {

	public Context c;

	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		c = getBaseContext();

		Debugger.log(getClass().getSimpleName() + " onCreate()");
	}

	public boolean hideView(int res) {
		View v = (View) findViewById(res);
		if (v != null) {
			v.setVisibility(View.GONE);
			return true;
		}

		return false;
	}

	public void log(String message) {
		if (message.length() > 0)
			Log.d(Values.DEBUG_TAG + " " + this.getClass().toString(), message);
	}

}
