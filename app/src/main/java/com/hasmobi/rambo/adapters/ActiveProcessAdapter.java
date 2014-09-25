package com.hasmobi.rambo.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.lib.DDebug;
import com.hasmobi.rambo.lib.DResources;
import com.hasmobi.rambo.utils.FontHelper;
import com.hasmobi.rambo.utils.RamManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActiveProcessAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private List<SingleProcess> objects = null;

    SharedPreferences excluded_list;

    public ActiveProcessAdapter(Activity context, List objList) {
        super(context, R.layout.single_process_layout_inflater_new, objList);
        this.context = context;

        if (objList != null) {
            this.objects = objList;
        } else {
            this.objects = new ArrayList<SingleProcess>();
        }

        excluded_list = context.getSharedPreferences("excluded_list", Context.MODE_PRIVATE);

        sort();
    }

    public int getCount() {
        return this.objects != null ? this.objects.size() : 0;
    }

    public void add(SingleProcess object) {
        objects.add(object);
        this.sort();
    }

    public void resetValues(List objects) {
        this.objects = objects;
        this.sort();
    }

    public void sort() {
        if (this.objects == null || this.objects.size() <= 0)
            return;

        Comparator<SingleProcess> myComparator = new Comparator<SingleProcess>() {
            public int compare(SingleProcess first, SingleProcess second) {
                Float i = first.memoryUsage;
                Float x = second.memoryUsage;
                return x.compareTo(i);
            }
        };
        Collections.sort(this.objects, myComparator);
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        SingleProcessView sqView = null;

        if (rowView == null) {
            // Get a new instance of the row layout view
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.single_process_layout_inflater_new,
                    null);

            FontHelper.overrideFonts(rowView, Typeface.createFromAsset(context.getAssets(), FontHelper.FONT_COMFORTAA));

            // Hold the view objects in an object,
            // so they don't need to be re-fetched
            sqView = new SingleProcessView();
            sqView.name = (TextView) rowView.findViewById(R.id.tvName);
            sqView.processWrapper = (LinearLayout) rowView
                    .findViewById(R.id.llProcessWrapper);
            sqView.icon = (ImageView) rowView.findViewById(R.id.app_icon);
            sqView.memory = (TextView) rowView.findViewById(R.id.tvMemory);

            sqView.llKill = (LinearLayout) rowView.findViewById(R.id.llKillProcess);
            sqView.llWhitelist = (LinearLayout) rowView.findViewById(R.id.llWhitelist);

            sqView.killButton = (ImageView) sqView.llKill
                    .findViewById(R.id.iKill);
            sqView.whiteListButton = (ImageView) sqView.llWhitelist
                    .findViewById(R.id.iWhitelist);

            sqView.whitelistLabel = (TextView) sqView.llWhitelist.findViewById(R.id.tvWhitelist);
            sqView.killLabel = (TextView) sqView.llKill.findViewById(R.id.tvKill);

            // Cache the view objects in the tag,
            // so they can be re-accessed later
            rowView.setTag(sqView);
        } else {
            // Restore view object from cache
            sqView = (SingleProcessView) rowView.getTag();
        }

        // Get the process data
        final SingleProcess currentApp = objects.get(position);

        boolean inWhitelist = excluded_list.getBoolean(
                currentApp.ai.packageName, false);

        // Transfer the data to the view
        sqView.name.setText(currentApp.name); // App name

        String memoryUsage = formatFileSize(currentApp.memoryUsage * 1000 * 1000);
        memoryUsage = String.format(
                context.getResources().getString(R.string.users_n_mb_ram),
                memoryUsage);

        // Set the app icon
        sqView.icon.setImageDrawable(currentApp.icon);
        sqView.memory.setText(memoryUsage);

        OnClickListener killClickListener = new OnClickListener() {

            public void onClick(View v) {
                String pkg = currentApp.ai.packageName;
                Log.d(getClass().toString(), "Killing single process manually: " + pkg);

                if (pkg != null && pkg.length() > 0) {

                    excluded_list = context.getSharedPreferences(
                            "excluded_list", Context.MODE_PRIVATE);

                    if (excluded_list.getBoolean(pkg, false)) {
                        Log.d(getClass().toString(), "Package found in excluded list. Skipping kill");
                        // Process whitelisted, don't kill
                        DDebug.toast(v.getContext().getApplicationContext(), DResources.getString(v.getContext().getApplicationContext(), R.string.cant_kill_whitelisted_app));
                        return;
                    }

                    // Kill the app's process
                    RamManager rm = new RamManager(context);
                    rm.killPackage(pkg);

                    // Remove the app from the processes list immediately
                    try {
                        objects.remove(position);
                    } catch (Exception e) {

                    }

                    // Notify the ListView that the data list was changed
                    notifyDataSetChanged();
                }
            }

        };
        sqView.llKill.setOnClickListener(killClickListener);
        sqView.killButton.setOnClickListener(killClickListener);

        // Initial state of the "whitelist" button
        if (inWhitelist) {
            sqView.whiteListButton.setImageResource(R.drawable.lock);
            sqView.whitelistLabel.setText(DResources.getString(context, R.string.remove_from_whitelist));
        } else {
            sqView.whiteListButton.setImageResource(R.drawable.unlock);
            sqView.whitelistLabel.setText(DResources.getString(context, R.string.add_to_whitelist));
        }

        final SingleProcessView sqViewCopy = sqView;

        OnClickListener whitelistClickListener = new OnClickListener() {

            public void onClick(View view) {
                // Whitelist/Blacklist the clicked package and update its button
                // label
                String pkg = currentApp.ai.packageName;

                boolean inWhitelist = excluded_list.getBoolean(
                        pkg, false);

                SharedPreferences.Editor edit = excluded_list.edit();

                DDebug d = new DDebug(context);

                String toastMessage = null;

                try {
                    if (inWhitelist) {
                        edit.remove(currentApp.ai.packageName);
                        sqViewCopy.whiteListButton
                                .setImageResource(R.drawable.unlock);

                        toastMessage = DResources.getString(context,
                                R.string.app_whitelist_removed);
                    } else {
                        edit.putBoolean(currentApp.ai.packageName, true);
                        sqViewCopy.whiteListButton.setImageResource(R.drawable.lock);

                        toastMessage = DResources.getString(context,
                                R.string.app_whitelisted);
                    }
                } catch (Exception e) {
                    DDebug.log(getClass().toString(), "Can not whitelist/blacklist package", e);
                    d.toast("This app can not be "
                                    + (inWhitelist ? "removed from blacklist"
                                    : "added to blacklist")
                                    + ". Please contact us at feedback@hasmobi.com if this error persists.",
                            Toast.LENGTH_LONG);
                }

                if (edit.commit()) {
                    d.toast(toastMessage);

                    final Vibrator v = (Vibrator) context
                            .getSystemService(Context.VIBRATOR_SERVICE);
                    if (v != null)
                        v.vibrate(100);
                } else {
                    d = new DDebug(context);
                    d.toast("Unable to whitelist/remove from whitelist");
                }

                // Refresh the list
                excluded_list = context
                        .getSharedPreferences("excluded_list", Context.MODE_PRIVATE);

                notifyDataSetChanged();
            }

        };
        sqView.llWhitelist.setOnClickListener(whitelistClickListener);
        sqView.whiteListButton.setOnClickListener(whitelistClickListener);

        return rowView;
    }

    public static String formatFileSize(float size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size
                / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }

    protected static class SingleProcessView {
        protected TextView name;
        protected LinearLayout processWrapper;
        protected ImageView icon;
        protected TextView memory;
        protected ImageView killButton, whiteListButton;
        protected TextView killLabel, whitelistLabel;
        protected LinearLayout llKill, llWhitelist;
    }

}
