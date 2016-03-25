package com.alangpierce.lambdacalculusplayground.view;

import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;

import android.widget.LinearLayout;

public interface ExpressionView {
    // TODO: This violates law of demeter. Try to restructure things to not need this.
    LinearLayout getNativeView();
    ScreenPoint getScreenPos();
}
