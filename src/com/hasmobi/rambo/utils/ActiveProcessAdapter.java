package com.hasmobi.rambo.utils;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hasmobi.rambo.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActiveProcessAdapter extends ArrayAdapter<String> {

	private final Activity context;
	private final List<SingleProcess> objects;

	public ActiveProcessAdapter(Activity context, List objects) {
		super(context, R.layout.single_process_layout_inflater, objects);
		this.context = context;
		this.objects = objects;

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

			sqView.killButton = (Button) rowView
					.findViewById(R.id.bKillProcess);

			// Cache the view objects in the tag,
			// so they can be re-accessed later
			rowView.setTag(sqView);
		} else {
			// Restore view object from cache
			sqView = (SingleProcessView) rowView.getTag();
		}

		// Get the process data
		final SingleProcess currentApp = objects.get(position);

		// Transfer the data to the view
		sqView.name.setText(currentApp.name); // App name

		// Set the app icon
		sqView.icon.setImageDrawable(currentApp.icon);
		String memoryUsage = formatFileSize(currentApp.memoryUsage * 1000 * 1000);
		sqView.memory.setText(String.valueOf(memoryUsage));
		sqView.killButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				String pkg = currentApp.appInfo.processName;
				if (pkg != null && pkg.length() > 0) {

					// Kill the app's process
					RamManager rm = new RamManager(context);
					rm.killPackage(pkg);

					// Remove the app from the processes list immediately
					objects.remove(position);

					// Notify the ListView that the data list was changed
					notifyDataSetChanged();
				}
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
		protected Button killButton;
	}

}
