package com.alangpierce.lambdacalculusplayground.component;

import android.view.View;

import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;

import rx.Observable;

public class ProducerView {
    private final View nativeView;

    public ProducerView(View nativeView) {
        this.nativeView = nativeView;
    }

    public ScreenPoint getPos() {
        return Views.getScreenPos(nativeView);
    }

    public boolean intersectsWith(TopLevelExpressionView dragView) {
        return Views.viewsIntersect(nativeView, dragView.getNativeView());
    }
}
