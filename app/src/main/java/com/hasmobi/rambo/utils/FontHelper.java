package com.hasmobi.rambo.utils;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FontHelper {

    final public static String FONT_COMFORTAA = "comfortaa-regular.ttf";

    public static void overrideFonts(final View rootView, Typeface typeface) {
        try {
            if (rootView instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) rootView;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(child, typeface);
                }
            } else if (rootView instanceof TextView) {
                ((TextView)rootView).setTypeface(typeface);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
