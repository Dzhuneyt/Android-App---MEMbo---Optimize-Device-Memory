package com.hasmobi.rambo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PieView extends View {
	RectF mOvals = null;

	int w = 0, h = 0;

	long freeRam = 0, totalRam = 0;

	Paint mBgPaints, tPaint;

	public PieView(Context context) {
		super(context);
		init();
	}

	public PieView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void init() {
		mBgPaints = new Paint();
		tPaint = new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (freeRam == 0 || totalRam == 0)
			return;

		mBgPaints.setAntiAlias(true);
		mBgPaints.setStyle(Paint.Style.FILL);
		mBgPaints.setColor(getResources().getColor(R.color.pieRed));
		mBgPaints.setStrokeWidth(0.00f);

		mBgPaints.setTextSize(30);
		mBgPaints.setTextAlign(Paint.Align.CENTER);

		// Draw the transparent background first
		tPaint.setAlpha(0);
		canvas.drawColor(tPaint.getColor());

		// The free degrees
		long freePercent = (360 * freeRam) / totalRam;
		canvas.drawArc(mOvals, 0, 360, true, mBgPaints);
		mBgPaints.setColor(getResources().getColor(R.color.pieGreen));
		canvas.drawArc(mOvals, -90, freePercent, true, mBgPaints);

		// // Draw the free/taken percent text over the circle
		//
		// mBgPaints.setColor(Color.parseColor(getContext().getResources()
		// .getString(R.color.pieSlice)));
		//
		// // Find the center of the arc to draw text on
		// double angle = ((freePercent + 90) / 180.0 * Math.PI);
		// double radius = (canvas.getWidth()) / 2; // Radius of the circle
		// double x = (radius + radius * Math.cos(-angle)) - radius / 2;
		// double y = (radius + radius * Math.sin(-angle));
		//
		// int percent = (int) ((freePercent * 100) / 360);
		// canvas.drawText(percent + "%", (float) x, (float) y, mBgPaints);
		//
		// // Draw the Available Ram percent
		// double polarRad = Math.atan2(x, y);
		// x = radius / 2 * Math.cos(polarRad) + radius / 4;
		// y = radius * Math.sin(polarRad) + radius / 4;
		// canvas.drawText(100 - percent + "%", (float) x, (float) y,
		// mBgPaints);

	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// Get whichever is smaller - width or height.
		// Helps to make a square instead of a rectangle.
		int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
		w = size;
		h = size;

		// The bounding box is not set, initialize it
		if (mOvals == null && !(mOvals instanceof RectF)) {
			mOvals = new RectF(24, 24, size - 24, size - 24);
		} else {
			// It's set, just change its dimensions
			mOvals.set(24, 24, size - 24, size - 24);
		}

		setMeasuredDimension(size, size);
	}

	public void setRam(long total, long free) {
		freeRam = free;
		totalRam = total;
	}

}