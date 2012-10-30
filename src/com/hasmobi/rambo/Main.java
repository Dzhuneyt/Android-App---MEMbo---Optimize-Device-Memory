package com.hasmobi.rambo;

import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Main extends Activity {

	long totalRam = 0, freeRam = 0, percent = 100;

	LinearLayout pieContainer;

	private PieView pie;

	AsyncTask<String, String, Void> memoryMonitor = null;
	Context context;

	ActivityManager am;

	String[] excluded = { "system_process", "com.hasmobi.rambo",
			"com.android.phone", "com.android.systemui",
			"android.process.acore", "com.android.launcher" };
	
	RamManager ramManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fullscreen();

		setContentView(R.layout.activity_main);

		context = getBaseContext();
		ramManager = new RamManager(context);

		am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		setPiechart();

		memoryMonitor = new freeRamUpdater().execute();

		setStyles();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(memoryMonitor==null){
			memoryMonitor = new freeRamUpdater().execute();
		}
	}

	@Override
	protected void onDestroy() {
		// Don't forget to cancel the Free RAM monitor
		if(memoryMonitor!=null)
			memoryMonitor.cancel(false);
		memoryMonitor = null;
		super.onDestroy();
	}

	public class freeRamUpdater extends AsyncTask<String, String, Void> {

		TextView tvFree, tvTaken;
		MemoryInfo mi;
		long availableMegs;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			tvFree = (TextView) findViewById(R.id.tvFree);
			tvTaken = (TextView) findViewById(R.id.tvTaken);
			mi = new MemoryInfo();

			// Reads the total available RAM on the device.
			// Only needed once. It's run once on the UI thread.
			readTotalRam();
		}

		@Override
		protected Void doInBackground(String... params) {

			while (!isCancelled()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}

				am.getMemoryInfo(mi);
				availableMegs = (mi.availMem / 1048576L);
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

			pie.setRam(totalRam, freeRam); // Also redraws the pie chart

			if (values[0] != null && values[1] != null) {
				tvFree.setText("Free: " + values[0] + " MB (" + values[1]
						+ "%)");
				tvTaken.setText("Taken: " + (totalRam - freeRam) + " MB");
			}
		}

		private void readTotalRam() {
			int tm = 0; // Total memory
			final TextView tvTotal = (TextView) findViewById(R.id.tvTotal);
			try {
				RandomAccessFile r = new RandomAccessFile("/proc/meminfo", "r");
				String load = r.readLine();
				String[] totrm = load.split(" kB");
				String[] trm = totrm[0].split(" ");
				tm = Integer.parseInt(trm[trm.length - 1]);
				tm = Math.round(tm / 1024);
				totalRam = Long.valueOf(tm);
				tvTotal.setText("Total RAM: " + String.valueOf(totalRam)
						+ " MB");
			} catch (IOException e) {
				tvTotal.setText("Total RAM: Unable to find");
				e.printStackTrace();
			}
		}

	}


	// Optimize button clicked
	public void optimizeHandler(final View v) {
		
		ramManager.killBgProcesses();

		((Button) v).setText(getResources().getString(R.string.optimizing));
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

	private void setStyles() {
		try {
			final TextView tv = (TextView) findViewById(R.id.appTitle);
			final Button bOptimize = (Button) findViewById(R.id.bOptimize);
			final Typeface face = Typeface.createFromAsset(getAssets(),
					"sttransmission_800_extrabold.otf");
			tv.setTypeface(face);
			bOptimize.setTypeface(face);
		} catch (Exception e) {
			log("Unable to apply custom fonts");
		}
	}

	private void setPiechart() {
		// Setup the Pie Chart
		pieContainer = (LinearLayout) findViewById(R.id.pie_container_id);
		pie = new PieView(this);
		pieContainer.addView(pie);
	}

	private void fullscreen() {
		// Remove the window title
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void log(String s) {
		Log.d("com.hasmobi.rambo MAIN", s);
	}

}
