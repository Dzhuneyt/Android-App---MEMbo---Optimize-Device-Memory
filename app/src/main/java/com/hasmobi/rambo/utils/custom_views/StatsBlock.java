package com.hasmobi.rambo.utils.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hasmobi.rambo.R;
import com.hasmobi.rambo.lib.DDebug;
import com.hasmobi.rambo.utils.FontHelper;

public class StatsBlock extends LinearLayout {

    private PieView pie;
    Runnable afterDetailsButtonClick;

    public StatsBlock(Context context) {
        super(context);
        init();
    }

    public StatsBlock(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatsBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Do your View initialization here. Run only once per view.
     */
    public void init() {
        inflate(getContext(), R.layout.stats_block, this);

        RelativeLayout pieHolder = (RelativeLayout) findViewById(R.id.pie);
        pieHolder.removeAllViews();

        pie = new PieView(getContext());
        pie.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Set initial values to the Pie
        // They should generally be overwritten immediately
        pie.setRam(100, 50);

        pieHolder.addView(pie);

        // Apply a typeface globally to the view
        this.setTypeface(Typeface.createFromAsset(getContext().getAssets(), FontHelper.FONT_COMFORTAA));

        Button bDetails = (Button) findViewById(R.id.bDetails);
        if (bDetails != null) {
            bDetails.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (afterDetailsButtonClick != null) {
                        final Handler h = new Handler();
                        h.post(afterDetailsButtonClick);
                    }
                }
            });
        }
    }

    /**
     * Sets the header text (label) for this stats block
     *
     * @param s
     */
    public void setHeader(String s) {
        final TextView header = (TextView) this.findViewById(R.id.tvHeader);
        if (header != null)
            header.setText(s);
    }

    public void setDetailsButtonLabel(String label) {
        Button bDetails = (Button) findViewById(R.id.bDetails);
        if (bDetails != null) {
            bDetails.setText(label);
        }
    }

    /**
     * Set new values to the Pie (redraws it internally)
     *
     * @param total
     * @param available
     */
    public void setPieValues(long total, long available) {
        if (pie != null) {
            pie.setRam(total, available);
        } else {
            DDebug.log(this.getClass().toString(), "Pie not available yet. Unable to set pie values");
        }
    }

    public void setDetailsButtonClickAction(Runnable r) {
        this.afterDetailsButtonClick = r;
    }

    public void setTakenString(String taken) {
        TextView tvTaken = (TextView) findViewById(R.id.tvTaken);
        if (tvTaken != null)
            tvTaken.setText(taken);
    }

    public void setFreeString(String free) {
        TextView tvFree = (TextView) findViewById(R.id.tvFree);
        if (tvFree != null)
            tvFree.setText(free);
    }

    /**
     * Sets the View to be still in "Loading..." screen, e.g.
     * when a Async task is doing something in the background.
     * It should call this same method with FALSE as parameter
     * when ready to reset the View to the actual ready state
     *
     * @param isStillLoading
     */
    public void setLoading(boolean isStillLoading) {
        final View holderContent = findViewById(R.id.holderContent);
        final View holderLoading = findViewById(R.id.holderLoading);

        if (holderContent == null || holderLoading == null)
            return;

        if (isStillLoading) {
            holderContent.setVisibility(View.GONE);
            holderLoading.setVisibility(View.VISIBLE);
        } else {
            holderContent.setVisibility(View.VISIBLE);
            holderLoading.setVisibility(View.GONE);
        }
    }

    /**
     * Allows you to override the custom view's typeface globally
     *
     * @param typeface
     */
    public void setTypeface(Typeface typeface) {
        if (typeface != null)
            FontHelper.overrideFonts(getRootView(), typeface);
    }

    /**
     * Redraws the view (e.g. in case of size or positioning changes)
     */
    public void refresh() {
        invalidate();
        requestLayout();
    }
}
