package com.hasmobi.rambo.fragments.child;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.lib.DResources;
import com.hasmobi.rambo.supers.DFragment;
import com.hasmobi.rambo.utils.FontHelper;
import com.hasmobi.rambo.utils.TypefaceSpan;
import com.hasmobi.rambo.utils.custom_views.StatsBlock;

public class FragmentDiskUsage extends DFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(getActivity().getActionBar()!=null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            SpannableString s = new SpannableString(DResources.getString(c,
                    R.string.disk_usage));
            s.setSpan(new TypefaceSpan(getActivity(), FontHelper.ACTIONBAR_TITLE), 0,
                    s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getActivity().getActionBar().setTitle(s);
        }

        return inflater.inflate(R.layout.fragment_disk_usage, null);
    }

    private long availableBytesInternal, totalBytesInternal, availableBytesSD, totalBytesSD;

    @Override
    public void onResume() {
        super.onResume();

        updateDiskUsage();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateDiskUsage(){
        new DiskUsageCalculator().execute();
    }

    private class DiskUsageCalculator extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (getView() != null) {
                TextView tvLoading = (TextView) getView().findViewById(R.id.tvRunningProcessesPlaceholder);
                if (tvLoading != null)
                    showView(tvLoading);

                LinearLayout llHolder = (LinearLayout) getView().findViewById(R.id.lvHolder);
                if (llHolder != null)
                    hideView(llHolder);
            }
        }

        protected Void doInBackground(Void... unused) {
            recalculate();
            return null;
        }


        protected void onPostExecute(Void unused) {
            long totalBytes = (totalBytesInternal + totalBytesSD);
            long totalAvailableBytes = (availableBytesInternal + availableBytesSD);

            if (getView() != null)
            {

                LinearLayout lvHolder = (LinearLayout) getView().findViewById(R.id.lvHolder);
                if (lvHolder != null)
                    showView(lvHolder);

                TextView tvLoading = (TextView) getView().findViewById(R.id.tvRunningProcessesPlaceholder);
                if (tvLoading != null)
                    hideView(tvLoading);

                StatsBlock statsBlockInternal = (StatsBlock) getView().findViewById(R.id.statsBlockInternal);
                StatsBlock statsBlockSd = (StatsBlock) getView().findViewById(R.id.statsBlockSd);

                statsBlockInternal.setPieValues(totalBytesInternal, availableBytesInternal);
                statsBlockSd.setPieValues(totalBytesSD, availableBytesSD);

                statsBlockInternal.setHeader(DResources.getString(getActivity(), R.string.internal_memory));
                statsBlockSd.setHeader(DResources.getString(getActivity(), R.string.sd_card));

                // Hide the "Details" buttons on these StatsBlocks
                hideView(statsBlockInternal.findViewById(R.id.bDetails));
                hideView(statsBlockSd.findViewById(R.id.bDetails));

                statsBlockInternal.setFreeString(this.bytesToMb(availableBytesInternal));
                statsBlockInternal.setTakenString(this.bytesToMb(totalBytesInternal-availableBytesInternal));

                statsBlockSd.setFreeString(this.bytesToMb(availableBytesSD));
                statsBlockSd.setTakenString(this.bytesToMb(totalBytesSD-availableBytesSD));
            }
        }

        @SuppressWarnings("deprecation")
        private void recalculate() {

            // Calculate usage in SD card
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            int sdkInt = Build.VERSION.SDK_INT;

            if (sdkInt < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                int blockSize = statFs.getBlockSize();
                availableBytesSD = ((long) statFs.getAvailableBlocks()) * blockSize;
                totalBytesSD = ((long) statFs.getBlockCount()) * blockSize;
            } else {
                availableBytesSD = statFs.getAvailableBytes();
                totalBytesSD = statFs.getTotalBytes();
            }

            // Calculate usage in internal memory
            statFs.restat(Environment.getDataDirectory().getPath());
            if (sdkInt < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                int blockSize = statFs.getBlockSize();
                availableBytesInternal = ((long) statFs.getAvailableBlocks()) * blockSize;
                totalBytesInternal = ((long) statFs.getBlockCount()) * blockSize;
            } else {
                availableBytesInternal = statFs.getAvailableBytes();
                totalBytesInternal = statFs.getTotalBytes();
            }

            Log.d(getClass().toString(), "Disk usage calculation result (bytes):"
                    + "Internal (" + availableBytesInternal + "," + totalBytesInternal
                    + ") External (" + availableBytesSD + "," + totalBytesSD + ")");
        }

        private String bytesToMb(long bytes) {
            return this.humanReadableByteCount(bytes, false);
        }

        public String humanReadableByteCount(long bytes, boolean si) {
            int unit = si ? 1000 : 1024;
            if (bytes < unit) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
            return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        }
    }


}
