package com.hasmobi.rambo.fragments.child;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.utils.BatteryTipAdapter;
import com.hasmobi.rambo.utils.SingleBatteryTip;
import com.hasmobi.rambo.utils.SingleBatteryTipRegular;
import com.hasmobi.rambo.utils.services.BackgroundSyncSleepBackgroundBooster;
import com.hasmobi.rambo.utils.services.WiFiSleepBackgroundBooster;

import java.util.ArrayList;

public class FragmentBattery extends DFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setActionBarTitle(R.string.battery_usage);

        View v = inflater.inflate(R.layout.fragment_battery, null);

        final ListView lvBatteryTips = (ListView) v.findViewById(R.id.lvBatteryTips);
        if (lvBatteryTips != null) {
            ArrayList<SingleBatteryTip> all_tips = new ArrayList<SingleBatteryTip>();

            final SharedPreferences sp = c.getSharedPreferences(BatteryTipAdapter.SP_NAME_BATTERY_PREFS, Context.MODE_PRIVATE);

            // Reusable object
            SeekBar sb;
            TextView tvProgress;

            final SingleBatteryTipRegular tipWifiSleep = new SingleBatteryTipRegular(
                    "WiFi Sleep",
                    "Automatically disable WiFi after the screen is turned off. It will be re-enabled as soon as you turn on the screen.",
                    WiFiSleepBackgroundBooster.SP_NAME
            );

            tipWifiSleep.control = getActivity().getLayoutInflater().inflate(R.layout.tip_scrubber, null, false);

            sb = (SeekBar) tipWifiSleep.control.findViewById(R.id.sbScrubber);

            sb.setMax(3 * 60); // 30 minutes

            // Set initial scrubber position
            sb.setProgress(sp.getInt(tipWifiSleep.sp_name, 0));

            // Set initial progress text
            tipWifiSleep.setProgressText(SingleBatteryTipRegular.getProgressLabel(sb.getProgress()));

            // Start listening for SeekBar progress changes
            sb.setOnSeekBarChangeListener(tipWifiSleep);

            all_tips.add(tipWifiSleep);

            final SingleBatteryTipRegular tipSyncSleep = new SingleBatteryTipRegular("Background Sync Sleep", "Automatically disable background syncing after the screen is turned off. It will be re-enabled as soon as you turn on the screen.", BackgroundSyncSleepBackgroundBooster.SP_NAME);

            tipSyncSleep.control = getActivity().getLayoutInflater().inflate(R.layout.tip_scrubber, null, false);
            sb = (SeekBar) tipSyncSleep.control.findViewById(R.id.sbScrubber);
            sb.setMax(5 * 60); // 5 minutes max

            // Set initial scrubber position
            sb.setProgress(sp.getInt(tipSyncSleep.sp_name, 0));

            // Set initial progress text
            tipSyncSleep.setProgressText(SingleBatteryTipRegular.getProgressLabel(sb.getProgress()));

            // Start listening for SeekBar progress changes
            sb.setOnSeekBarChangeListener(tipSyncSleep);

            // duplicate to illustrate UI
            all_tips.add(tipSyncSleep);

            final SingleBatteryTipRegular tipScreenOnTime = new SingleBatteryTipRegular(
                    "Screen backlight time",
                    "The screen backlight is the biggest battery consumer on most devices. It is recommended that you keep its timeout as low as possible. We recommend 15 seconds.",
                    null // No sp_name needed
            );
            //tipScreenOnTime.buttonText = DResources.getString(c, R.string.reduce_to_15_seconds);

            tipScreenOnTime.control = getActivity().getLayoutInflater().inflate(R.layout.tip_warning, null, false);

            // Hide the "Progress" text above the control
            tipScreenOnTime.setProgressText(null);

            int currentScreenTimeout = Settings.System.getInt(c.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, -1); // milliseconds
            if (currentScreenTimeout > 0) {
                currentScreenTimeout = currentScreenTimeout / 1000; // convert to seconds
            }
            tipScreenOnTime.setWarningMessage(String.format("Current screen on time is %s seconds", String.valueOf(currentScreenTimeout)));

            Button bReduceScreenOnTime = (Button) tipScreenOnTime.control.findViewById(R.id.bAction);
            if(bReduceScreenOnTime!=null) {
                bReduceScreenOnTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Settings.System.putInt(c.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                        view.setVisibility(View.GONE);

                        int currentScreenTimeout = Settings.System.getInt(view.getContext().getContentResolver(),
                                Settings.System.SCREEN_OFF_TIMEOUT, -1); // milliseconds
                        if (currentScreenTimeout > 0) {
                            currentScreenTimeout = currentScreenTimeout / 1000; // convert to seconds
                        }
                        tipScreenOnTime.setWarningMessage(String.format("Current screen on time is %s seconds", String.valueOf(currentScreenTimeout)));
                    }
                });

                bReduceScreenOnTime.setText(c.getResources().getString(R.string.reduce_to_15_seconds));

                if(currentScreenTimeout <= 15){
                    bReduceScreenOnTime.setVisibility(View.GONE);
                }else{
                    bReduceScreenOnTime.setVisibility(View.VISIBLE);
                }
            }

            /*
            tipScreenOnTime.asyncTask = new SingleBatteryTipMessageWithButton.LabelUpdater() {
                @Override
                protected Void doInBackground(Void... voids) {
                    int currentScreenTimeout = Settings.System.getInt(c.getContentResolver(),
                            Settings.System.SCREEN_OFF_TIMEOUT, -1); // milliseconds
                    if (currentScreenTimeout > 0) {
                        currentScreenTimeout = currentScreenTimeout / 1000; // convert to seconds
                    }
                    DDebug.log(getClass().toString(), "Current timeout " + currentScreenTimeout);
                    this.resultReplacement = String.format("Current screen on time is %s seconds", String.valueOf(currentScreenTimeout));
                    return super.doInBackground(voids);
                }
            };
            tipScreenOnTime.updateLabel = new SingleBatteryTipMessageWithButton.LabelUpdaterRunnable("Current screen on time is %s seconds"){
                @Override
                public void run() {
                    int currentScreenTimeout = Settings.System.getInt(c.getContentResolver(),
                            Settings.System.SCREEN_OFF_TIMEOUT, -1); // milliseconds
                    if (currentScreenTimeout > 0) {
                        currentScreenTimeout = currentScreenTimeout / 1000; // convert to seconds
                    }
                    DDebug.log(getClass().toString(), "Current timeout " + currentScreenTimeout);
                    this.label = String.format("Current screen on time is %s seconds", String.valueOf(currentScreenTimeout));

                    if(currentScreenTimeout > 15000 && bFix!=null){
                        bFix.setVisibility(View.GONE);
                    }
                    super.run();
                }
            };

            // Get current screen timeout time and if it's less equal to
            // or less than 15 seconds, don't show the button at all
            int currentScreenTimeout = Settings.System.getInt(c.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, -1); // milliseconds
            if(currentScreenTimeout > 15000) {
                tipScreenOnTime.buttonClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Settings.System.putInt(c.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                        view.setVisibility(View.GONE);

                        lvBatteryTips.invalidateViews();
                        lvBatteryTips.refreshDrawableState();
                    }
                };
            }*/
            all_tips.add(tipScreenOnTime);

            BatteryTipAdapter adapter = new BatteryTipAdapter(getActivity(), android.R.layout.simple_list_item_1, all_tips);
            lvBatteryTips.setAdapter(adapter);
        }

        return v;
    }
}
