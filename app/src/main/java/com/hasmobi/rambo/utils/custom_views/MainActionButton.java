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
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// Get whichever is smaller - width or height.
		// Helps to make a square instead of a rectangle.
		// int size = Math.max(getMeasuredWidth(), getMeasuredHeight());

//		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}

}
