package com.hasmobi.rambo.supers;

import com.hasmobi.rambo.utils.Debugger;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class DFragmentActivity extends FragmentActivity {

	public Context c;

	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		c = getBaseContext();

		Debugger.log(getClass().getSimpleName() + " onCreate()");
	}

}
