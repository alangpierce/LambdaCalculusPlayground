package com.alangpierce.lambdacalculusplayground.view;

import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;

public interface ExpressionView {
    // TODO: This violates law of demeter. Try to restructure things to not need this.
    LinearLayout getNativeView();
    ScreenPoint getScreenPos();
}
