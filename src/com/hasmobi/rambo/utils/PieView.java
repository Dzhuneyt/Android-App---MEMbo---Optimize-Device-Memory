package com.hasmobi.rambo.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PieView extends View {
	RectF mOvals = null;

	long freeRam = 0, totalRam = 0;

	Paint brush, transparentPaint;

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

		brush.setAntiAlias(true);
		brush.setStyle(Paint.Style.FILL);
		brush.setColor(getResources().getColor(R.color.pieRed));
		brush.setStrokeWidth(0.00f);

		brush.setTextSize(30);
		brush.setTextAlign(Paint.Align.CENTER);

		// Draw the transparent background first
		transparentPaint.setAlpha(0);
		canvas.drawColor(transparentPaint.getColor());

		// The free degrees
		long freePercent = (360 * freeRam) / totalRam;
		canvas.drawArc(mOvals, 0, 360, true, brush);
		brush.setColor(getResources().getColor(R.color.pieGreen));
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
