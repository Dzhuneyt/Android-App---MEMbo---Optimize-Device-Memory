package com.hasmobi.rambo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hasmobi.rambo.R;

public class SingleBatteryTipRegular extends SingleBatteryTip implements SeekBar.OnSeekBarChangeListener {

    public SingleBatteryTipRegular(String title, String description, String sp_name) {
        super(title, description, sp_name);
    }

    @Override
    public View inflate(Context c) {
        return control;
    }

    @Override
    public void inflate(Context c, View parent) {
    }

    public void setProgressText(String text) {
        if (this.control != null) {
            TextView tv = (TextView) this.control.findViewById(R.id.tvProgress);
            if (tv != null) {
                if (text == null) {
                    tv.setText("");
                    tv.setVisibility(View.GONE);
                } else {
                    tv.setText(text);
                    tv.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    static public String getProgressLabel(int progressSeconds) {
        if (progressSeconds == 0) {
            return "Never";
        }

        if (progressSeconds < 60) {
            return progressSeconds + " seconds";
        } else {
            return Math.round(progressSeconds / 60) + " minutes";
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean byUser) {
        final SharedPreferences sp = seekBar.getContext().getSharedPreferences(BatteryTipAdapter.SP_NAME_BATTERY_PREFS, Context.MODE_PRIVATE);
        // Save the current progress in the SeekBar to SharedPreferences
        sp.edit().putInt(sp_name, progress).apply();

        setProgressText(SingleBatteryTipRegular.getProgressLabel(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * Methods specific to different types of tips follow:
     */
    public void setWarningMessage(String msg) {
        if (this.control != null) {
            TextView tvWarning = (TextView) this.control.findViewById(R.id.tvWarningMessage);
            if (tvWarning != null) {
                tvWarning.setText(msg);
            } else {
                Log.e(getClass().toString(), "Can not set warning message because warning TextView not found. Maybe this is not the correct tip type?");
            }
        }

    }

}
