package com.hasmobi.rambo;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActiveProcessAdapter extends ArrayAdapter<String> {

	private final Activity context;
	private final List objects;

	public ActiveProcessAdapter(Activity context, List objects) {
		super(context, R.layout.single_process_layout_inflater, objects);
		this.context = context;
		this.objects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// return super.getView(position, convertView, parent);
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

			// Cache the view objects in the tag,
			// so they can be re-accessed later
			rowView.setTag(sqView);
		} else {
			sqView = (SingleProcessView) rowView.getTag();
		}

		// Transfer the stock data from the data object
		// to the view objects
		SingleProcess currentApp = (SingleProcess) objects.get(position);
		sqView.name.setText(currentApp.name);
		if (currentApp.whitelisted) {
			sqView.name.setTextColor(context.getResources().getColor(
					R.color.pieGreen));
			// sqView.processWrapper.setBackgroundResource(R.color.pieGreen);
		} else {
			sqView.name.setTextColor(context.getResources().getColor(
					R.color.pieRed));
			// sqView.processWrapper.setBackgroundResource(R.color.pieRed);
		}
		sqView.icon.setBackgroundDrawable(currentApp.icon);

		return rowView;
	}

	protected static class SingleProcessView {
		protected TextView name;
		protected LinearLayout processWrapper;
		protected ImageView icon;
	}

}