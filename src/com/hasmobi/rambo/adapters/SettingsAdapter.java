package com.hasmobi.rambo.adapters;

import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.adapters.placeholders.SettingsSingle;
import com.hasmobi.rambo.utils.Prefs;

public class SettingsAdapter extends ArrayAdapter<String> {

	private final Activity context;
	private final List<SettingsSingle> objects;

	public SettingsAdapter(Activity context, List objects) {
		super(context, R.layout.infl_single_setting, objects);
		this.context = context;
		this.objects = objects;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		SingleView v = null;

		if (rowView == null) {
			// Get a new instance of the row layout view
			rowView = context.getLayoutInflater().inflate(
					R.layout.infl_single_setting, null);

			// Hold the view objects in an object,
			// so they don't need to be re-fetched
			v = new SingleView();

			v.name = (TextView) rowView.findViewById(R.id.tvName);
			v.checkbox = (Button) rowView.findViewById(R.id.bSettingsCheckbox);

			// Cache the view objects in the tag,
			// so they can be re-accessed later
			rowView.setTag(v);
		} else {
			// Restore view object from cache
			v = (SingleView) rowView.getTag();
		}

		// Get the process data
		final SettingsSingle current = objects.get(position);

		final String settingName = current.getSettingName();
		final String settingKey = current.getSettingKey();

		Resources r = context.getResources();

		SharedPreferences sp = Prefs.instance(context);

		boolean checked = sp.getBoolean(settingKey, false);

		// Transfer the data to the view
		v.name.setText(settingName);

		v.checkbox.setText(r.getString(checked ? R.string.disable
				: R.string.enable));
		v.checkbox.setOnClickListener(new SettingsClickListener(position));

		return rowView;
	}

	class SettingsClickListener implements OnClickListener {

		int position;

		public SettingsClickListener(int position) {
			this.position = position;
		}

		public void onClick(View v) {
			final SettingsSingle current = objects.get(this.position);

			final String settingKey = current.getSettingKey();

			SharedPreferences sp = Prefs.instance(context);

			boolean checked = sp.getBoolean(settingKey, false);

			sp.edit().putBoolean(settingKey, (checked ? false : true)).commit();

			Resources r = context.getResources();
			((Button) v).setText(r.getString(checked ? R.string.disable
					: R.string.enable));

			if (current.observer != null) {
				current.observer.changed();
			}

			notifyDataSetChanged();
		}
	}

	protected static class SingleView {
		protected TextView name;
		protected Button checkbox;
	}

}
