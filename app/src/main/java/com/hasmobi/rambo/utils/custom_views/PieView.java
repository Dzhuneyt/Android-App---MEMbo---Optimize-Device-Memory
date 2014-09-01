package com.hasmobi.rambo.utils.custom_views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.hasmobi.rambo.R;

public class PieView extends View {
	RectF mOvals = null;

	long freeRam = 0, totalRam = 0;

	Paint brush, transparentPaint;

	boolean drawShadow = false;

	// Some styling
	int bgColor = R.color.pieBg;
	int overlaycolor = R.color.pieOverlay;
	int shadowOffset = 2;

	public PieView(Context context) {
		super(context);
		init();
	}

	public PieView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		brush = new Paint();
		transparentPaint = new Paint();
	}

	public void setRam(long total, long free) {
		freeRam = free;
		totalRam = total;
		this.invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (freeRam == 0 || totalRam == 0)
			return;

		brush.setTextSize(30);
		brush.setTextAlign(Paint.Align.CENTER);

		// Draw the transparent background first
		transparentPaint.setAlpha(0);
		canvas.drawColor(transparentPaint.getColor());

		// Draw the shadow
		mOvals.offset(shadowOffset, shadowOffset);
		if (drawShadow) {
			brush.setColor(getResources().getColor(R.color.solidBlack));
			brush.setAlpha(130);
			canvas.drawArc(mOvals, 0, 360, true, brush);
		}
		// Restore brush to its original settings
		brush.setAlpha(255);
		mOvals.offset(shadowOffset - 2 * shadowOffset, shadowOffset - 2
				* shadowOffset);

		brush.setAntiAlias(true);
		brush.setStyle(Paint.Style.FILL);
		brush.setColor(getResources().getColor(overlaycolor));
		brush.setStrokeWidth(0.00f);

		// Fill the pie + draw the overlaying slice
		long freePercent = (360 * freeRam) / totalRam;
		canvas.drawArc(mOvals, 0, 360, true, brush);
		brush.setColor(getResources().getColor(bgColor));
		canvas.drawArc(mOvals, -90, freePercent, true, brush);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// Get whichever is smaller - width or height.
		// Helps to make a square instead of a rectangle.
		int size = Math.min(getMeasuredWidth(), getMeasuredHeight());

		final int padding = 24;

		// The bounding box is not set, initialize it
		if (mOvals == null && !(mOvals instanceof RectF)) {
			// Initial creation of bounding square box
			mOvals = new RectF(padding, padding, (size - padding),
					(size - padding));
		} else {
			mOvals.set(padding, padding, (size - padding), (size - padding));
		}

		setMeasuredDimension(size, size);
	}

}
