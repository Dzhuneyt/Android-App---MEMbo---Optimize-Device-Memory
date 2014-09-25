package com.hasmobi.rambo.utils;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hasmobi.rambo.R;

import java.util.ArrayList;

public class BatteryTipAdapter extends ArrayAdapter<SingleBatteryTip> {

    static public final String SP_NAME_BATTERY_PREFS = "battery_tip_preferences";


    private Activity context;
    private ArrayList<SingleBatteryTip> data;

    public BatteryTipAdapter(Activity context, int resource, ArrayList<SingleBatteryTip> objects) {
        super(context, resource, objects);
        this.context = context;
        this.data = objects;
    }


    static class ViewHolder {
        TextView title, description;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.single_battery_tip, parent, false);

            holder = new ViewHolder();

            holder.title = (TextView) convertView.findViewById(R.id.tvHeader);
            holder.description = (TextView) convertView.findViewById(R.id.tvDescription);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SingleBatteryTip tip = data.get(position);

        FontHelper.overrideFonts(convertView, Typeface.createFromAsset(context.getAssets(), FontHelper.FONT_COMFORTAA));

        holder.title.setText(tip.title);
        holder.description.setText(tip.description);

        // Loads the controls (e.g. scrubber, checkbox, radio) depending on the
        // type of item/tip currently being viewed
        View control = tip.inflate(context);

        LinearLayout llHolder = (LinearLayout) convertView.findViewById(R.id.llHolder);
        if (llHolder != null && control != null) {
            llHolder.removeAllViews();
            llHolder.addView(control);
        }

        return convertView;
    }

    private void loadControl(final SingleBatteryTip tip, View parent) {
        if (tip == null) return;

        LinearLayout llHolder = (LinearLayout) parent.findViewById(R.id.llHolder);

        if (llHolder == null) return;

        if (tip.control != null) {
            llHolder.removeAllViews();
            llHolder.addView(tip.control);
        }
        /*

        switch (tip.tip_type) {
            case SingleBatteryTip.TYPE_SCRUBBER:
                llHolder.removeAllViews();

                SeekBar sb = new SeekBar(context.getBaseContext());
                sb.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                sb.setMax(3 * 60); // 3 minutes is max

                sb.setThumb(context.getResources().getDrawable(R.drawable.apptheme_scrubber_control_selector_holo_light));
                sb.setProgressDrawable(context.getResources().getDrawable(R.drawable.apptheme_scrubber_progress_horizontal_holo_light));
                sb.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.apptheme_scrubber_progress_horizontal_holo_light));

                SharedPreferences sp = getContext().getSharedPreferences(SP_NAME_BATTERY_PREFS, Context.MODE_PRIVATE);
                int progressFromPrefs = sp.getInt(tip.sp_name, 0);
                sb.setProgress(progressFromPrefs);

                sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        SharedPreferences sp = getContext().getSharedPreferences(SP_NAME_BATTERY_PREFS, Context.MODE_PRIVATE);
                        sp.edit().putInt(tip.sp_name, progress).apply();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                llHolder.addView(sb);
                break;
        }*/
    }
}
