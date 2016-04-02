package com.alangpierce.lambdacalculusplayground.component;

import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.R;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.ExpressionViewRenderer;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

import rx.Observable;

public class SlotView {
    private final DragObservableGenerator dragObservableGenerator;
    private final ExpressionViewRenderer renderer;

    private final ComponentParent parent;

    // If null, then we should present a placeholder view instead.
    private @Nullable ExpressionView view;
    // Non-null as long as we are attached, but may be a placeholder.
    private @Nullable View nativeView;

    public SlotView(
            DragObservableGenerator dragObservableGenerator,
            ExpressionViewRenderer renderer,
            ComponentParent parent,
            @Nullable ExpressionView view, @Nullable View nativeView) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.renderer = renderer;
        this.parent = parent;
        this.view = view;
        this.nativeView = nativeView;
    }

    public @Nullable Observable<? extends Observable<PointerMotionEvent>> getObservable() {
        if (view == null) {
            return null;
        }
        return dragObservableGenerator.getDragObservable(view.getNativeView());
    }

    public void detach() {
        Preconditions.checkNotNull(nativeView);
        parent.detach(nativeView);
        view = null;
        nativeView = null;
    }

    public void attach(@Nullable ExpressionView newView) {
        Preconditions.checkState(nativeView == null);
        view = newView;
        if (newView == null) {
            nativeView = renderer.makeMissingBodyView();
        } else {
            nativeView = newView.getNativeView();
        }
        parent.attach(nativeView);
    }

    public ScreenPoint getPos() {
        return Views.getScreenPos(nativeView);
    }

    /**
     * Do a hit test to determine if the dragged view is over the slot. If this slot is part of the
     * dragged view, we want to return false.
     */
    public boolean intersectsWith(TopLevelExpressionView dragView) {
        LinearLayout dragNativeView = dragView.getNativeView();
        return !Views.isAncestor(nativeView, dragNativeView) &&
                Views.viewsIntersect(nativeView, dragNativeView);
    }

    // TODO: Get rid of these NPE warnings. Currently it's safe because the functions will only be
    // called when nativeView is non-null.
    public void handleDragEnter() {
        nativeView.setBackgroundColor(getColor(R.color.expression_highlight));
    }

    public void handleDragExit() {
        nativeView.setBackgroundColor(getColor(R.color.empty_body));
    }

    private int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(nativeView.getContext(), resId);
    }

    public int getViewDepth() {
        return Views.viewDepth(nativeView);
    }
}
