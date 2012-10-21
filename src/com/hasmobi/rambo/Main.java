package com.hasmobi.rambo;

import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {

	private static final String TAG = "com.hasmobi.rambo MAIN";

	long totalRam = 0, freeRam = 0, percent = 100;

	LinearLayout pieContainer;

	private PieView pie;

	AsyncTask<String, String, Void> freeRamUpdater = null;
	Context context;

	ActivityManager am;

	String[] excluded = { "system_process", "com.hasmobi.rambo",
			"com.android.phone", "com.android.systemui",
			"android.process.acore", "com.android.launcher" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove the window title
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

		context = getBaseContext();

		am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		// Setup the Pie Chart
		pieContainer = (LinearLayout) findViewById(R.id.pie_container_id);
		pie = new PieView(this);
		pieContainer.addView(pie);

		setRamReaders();

		initCore();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Don't forget to cancel the Free RAM monitor
		freeRamUpdater.cancel(false);
	}

	private class freeRamUpdater extends AsyncTask<String, String, Void> {

		TextView tvFree, tvTaken;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			tvFree = (TextView) findViewById(R.id.tvFree);
			tvTaken = (TextView) findViewById(R.id.tvTaken);
		}

		@Override
		protected Void doInBackground(String... params) {

			while (!isCancelled()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}

				MemoryInfo mi = new MemoryInfo();
				am.getMemoryInfo(mi);
				long availableMegs = (mi.availMem / 1048576L);
				freeRam = availableMegs;
				percent = ((freeRam * 100) / totalRam);

				// Push progress to UI
				publishProgress(String.valueOf(availableMegs),
						String.valueOf(percent));

			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			redrawChart();
			tvFree.setText("Free: " + values[0] + " MB (" + values[1] + "%)");
			tvTaken.setText("Taken: " + (totalRam - freeRam) + " MB");
		}

	}

	private void killBgProcesses() {

		boolean excludeThis = false;
		int killCount = 0;
		for (RunningAppProcessInfo pid : am.getRunningAppProcesses()) {

			// Iterate through the excluded list and set excludeThis to true if
			// the currently iterated process is in that list.
			for (int i = 0; i < excluded.length; i++) {
				if (pid.processName.equalsIgnoreCase(excluded[i])) {
					excludeThis = true;
					Log.d(TAG, "Excluding " + excluded[i] + " from kill list");
				}
			}

			if (excludeThis == false) {
				// The running process is not in the exclude list, kill it
				am.killBackgroundProcesses(pid.processName);
				killCount++;

			} else {
				// Reset the exclusion to default for the next active process
				excludeThis = false;
			}
		}

		String toDisplay = getResources().getString(R.string.ram_cleared)
				+ "\n" + getResources().getString(R.string.apps_killed);
		toDisplay = String.format(toDisplay, killCount);
		Toast.makeText(
				getApplicationContext(),
				String.format(
						getResources().getString(R.string.ram_cleared)
								+ "\n"
								+ getResources()
										.getString(R.string.apps_killed),
						killCount), Toast.LENGTH_LONG).show();

	}

	public void readTotalRam() {
		int tm = 0;

		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");
			String load = reader.readLine();
			String[] totrm = load.split(" kB");
			String[] trm = totrm[0].split(" ");
			tm = Integer.parseInt(trm[trm.length - 1]);
			tm = Math.round(tm / 1024);
			totalRam = Long.valueOf(tm);
			TextView tvTotal = (TextView) findViewById(R.id.tvTotal);
			tvTotal.setText("Total RAM: " + String.valueOf(totalRam) + " MB");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Redraw the Pie Chart for free/total RAM
	public void redrawChart() {
		pie.setRam(totalRam, freeRam);
		// pie.setRam(100, 10);
		pieContainer.getChildAt(0).invalidate();
	}

	private void initCore() {
		Button bGarbage = (Button) findViewById(R.id.bOptimize);
		bGarbage.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				killBgProcesses();

				((Button) v).setText(getResources().getString(
						R.string.optimizing));
				// Simulate a background color change for 300ms
				v.setBackgroundColor(getResources().getColor(
						R.color.optimizeButtonBGclicked));
				v.postDelayed(new Runnable() {
					public void run() {
						v.setBackgroundColor(getResources().getColor(
								R.color.optimizeButtonBG));
						((Button) v).setText(getResources().getString(
								R.string.quick_optimize));
					}
				}, 300);
			}

		});

		try {
			TextView tv = (TextView) findViewById(R.id.appTitle);
			Typeface face = Typeface.createFromAsset(getAssets(),
					"sttransmission_800_extrabold.otf");
			tv.setTypeface(face);
			bGarbage.setTypeface(face);
		} catch (Exception e) {
			Log.d(TAG, "Unable to apply custom fonts");
		}
	}

	// Set the total and free RAM readers and updaters
	private void setRamReaders() {
		// Read the total RAM (only needed once)
		readTotalRam();
		// Start a continuous task that will update the free RAM amount
		freeRamUpdater = new freeRamUpdater().execute();
	}

}
