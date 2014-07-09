package com.hasmobi.rambo.adapters;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hasmobi.lib.DDebug;
import com.hasmobi.lib.DResources;
import com.hasmobi.rambo.R;
import com.hasmobi.rambo.utils.RamManager;

public class ActiveProcessAdapter extends ArrayAdapter<String> {

	private final Activity context;
	private List<SingleProcess> objects = null;

	SharedPreferences excluded_list;

	public ActiveProcessAdapter(Activity context, List objList) {
		super(context, R.layout.single_process_layout_inflater, objList);
		this.context = context;

		if (objList != null) {
			this.objects = objList;
		} else {
			this.objects = new ArrayList<SingleProcess>();
		}

		excluded_list = context.getSharedPreferences("excluded_list", 0);

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
			rowView = inflater.inflate(R.layout.single_process_layout_inflater,
					null);

			// Hold the view objects in an object,
			// so they don't need to be re-fetched
			sqView = new SingleProcessView();
			sqView.name = (TextView) rowView.findViewById(R.id.tvName);
			sqView.processWrapper = (LinearLayout) rowView
					.findViewById(R.id.llProcessWrapper);
			sqView.icon = (ImageView) rowView.findViewById(R.id.app_icon);
			sqView.memory = (TextView) rowView.findViewById(R.id.tvMemory);

			sqView.killButton = (ImageButton) rowView
					.findViewById(R.id.bKillProcess);
			sqView.whiteListButton = (ImageButton) rowView
					.findViewById(R.id.bWhitelist);

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
		sqView.killButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				String pkg = currentApp.ai.processName;
				if (pkg != null && pkg.length() > 0) {

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

		});

		// Initial state of the "whitelist" button
		if (inWhitelist) {
			sqView.whiteListButton.setImageResource(R.drawable.lock);
		} else {
			sqView.whiteListButton.setImageResource(R.drawable.unlock);
		}

		sqView.whiteListButton.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				// Whitelist/Blacklist the clicked package and update its button
				// label
				boolean inWhitelist = excluded_list.getBoolean(
						currentApp.ai.packageName, false);

				SharedPreferences.Editor edit = excluded_list.edit();

				DDebug d = new DDebug(context);

				String toastMessage = null;

				try {
					if (inWhitelist) {
						edit.remove(currentApp.ai.packageName);
						((ImageButton) view)
								.setImageResource(R.drawable.unlock);

						toastMessage = DResources.getString(context,
								R.string.app_whitelist_removed);
					} else {
						edit.putBoolean(currentApp.ai.packageName, true);
						((ImageButton) view).setImageResource(R.drawable.lock);

						toastMessage = DResources.getString(context,
								R.string.app_whitelisted);
					}
				} catch (Exception e) {
					DDebug.log(null, "Can not whitelist/blacklist package", e);
					d.toast("This app can not be "
							+ (inWhitelist ? "removed from blacklist"
									: "added to blacklist")
							+ ". Please contact us at feedback@hasmobi.com if this error persists.",
							Toast.LENGTH_LONG);
				}

				if (edit.commit()) {
					d.toast(toastMessage);

					try {
						final Vibrator v = (Vibrator) context
								.getSystemService(Context.VIBRATOR_SERVICE);
						if (v != null)
							v.vibrate(100);
					} catch (Exception e) {
					}
				} else {
					d = new DDebug(context);
					d.toast("Unable to whitelist/remove from whitelist");
				}

				excluded_list = context
						.getSharedPreferences("excluded_list", 0);

				notifyDataSetChanged();
			}

		});

		return rowView;
	}

	public static String formatFileSize(float size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
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
		protected ImageButton killButton;
		protected ImageButton whiteListButton;
	}

}
