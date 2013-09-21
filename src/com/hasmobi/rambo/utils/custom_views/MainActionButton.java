package com.hasmobi.rambo.utils.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * A subclass of the Button that will allow us to create square buttons
 */
public class MainActionButton extends Button {

	private boolean typefaceApplied = false;

	public MainActionButton(Context context) {
		super(context);
		setupFont();
	}

	public MainActionButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupFont();
	}

	public MainActionButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupFont();
	}

	private void setupFont() {
		if (typefaceApplied || this.isInEditMode())
			return;

		final Typeface face = Typeface.createFromAsset(
				getContext().getAssets(), "notosansregular.ttf");
		this.setTypeface(face);
		this.typefaceApplied = true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// UNUSED
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int size;
		if (widthMode == MeasureSpec.EXACTLY && widthSize == 0) {
			size = widthSize;
		} else if (heightMode == MeasureSpec.EXACTLY && heightSize == 0) {
			size = heightSize;
		} else {
			size = widthSize > heightSize ? widthSize : heightSize;
		}

		int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size,
				MeasureSpec.EXACTLY);
	}

}
