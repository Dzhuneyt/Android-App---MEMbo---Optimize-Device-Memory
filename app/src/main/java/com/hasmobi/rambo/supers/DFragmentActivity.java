package com.hasmobi.rambo.supers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.hasmobi.rambo.R;
import com.hasmobi.rambo.lib.DDebug;
import com.hasmobi.rambo.lib.DResources;
import com.hasmobi.rambo.utils.TermsOfUse;
import com.hasmobi.rambo.utils.TypefaceSpan;
import com.hasmobi.rambo.utils.Values;

public class DFragmentActivity extends FragmentActivity {

	public Context c;

	public Tracker t;
	boolean analyticsEnabled = false;

    private void initAnalytics(){
        if(t==null){
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);

            if (Values.DEBUG_MODE)
                analytics.setDryRun(true);

            this.t = analytics.newTracker(R.xml.global_tracker);
        }
    }

	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		c = getBaseContext();

		DDebug.log(getClass().toString(), "onCreate()");

        initAnalytics();
	}


	@Override
	public void onResume() {
		super.onResume();

        initAnalytics();
	}

	public boolean hideView(int res) {
		View v = findViewById(res);
		if (v != null) {
			v.setVisibility(View.GONE);
			return true;
		}

		return false;
	}

	public boolean showView(int res) {
		View v = findViewById(res);
		if (v != null) {
			v.setVisibility(View.VISIBLE);
			return true;
		}

		return false;
	}

    protected void setActionBarTitle(String title, String fontFileName){
        if (getActionBar() != null) {

            // Apply a custom TypeFace to the ActionBar title
            try {
                SpannableString s = new SpannableString(title);
                s.setSpan(new TypefaceSpan(this, fontFileName), 0,
                        s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                getActionBar().setTitle(s);
            } catch (Exception e) {
                log("Can not apply typeface to ActionBar title");
                log(e.getMessage());
            }
        }
    }

	public void log(String message) {
		if (message.length() > 0)
			Log.d(Values.DEBUG_TAG + " " + this.getClass().toString(), message);
	}

}
