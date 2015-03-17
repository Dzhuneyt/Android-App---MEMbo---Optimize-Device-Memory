package com.hasmobi.rambo.fragments.child;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.supers.DFragment;

public class FragmentBatteryNew extends DFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setActionBarTitle(R.string.battery_usage);

        View v = inflater.inflate(R.layout.fragment_battery_new, null);


	    LinearLayout batteryTipsHolder = (LinearLayout) v.findViewById(R.id.batteryTipsHolder);

	    if(batteryTipsHolder!=null){
		    batteryTipsHolder.removeAllViews();

		    //batteryTipsHolder.addView(new BatteryTipWifiSleep(c));
	    }

        return v;
    }
}
