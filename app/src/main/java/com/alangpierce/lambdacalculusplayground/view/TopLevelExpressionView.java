package com.alangpierce.lambdacalculusplayground.view;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.compat.Compat;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.geometry.PointDifference;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.base.Preconditions;

import rx.Observable;

public class TopLevelExpressionView {
    private final DragObservableGenerator dragObservableGenerator;
    private final PointConverter pointConverter;

    // The root view that we use is a bit complicated. In most situations, we use canvasRoot for
    // everything. However, when making an initial expression dragged from the palette, we want it
    // to be above the palette for the duration of that drag operation. That means that we use
    // abovePaletteRoot for a while, then when we see a drag operation finish, we re-parent the
    // expression onto the canvasRoot and set isAbovePalette to false, after which the expression
    // becomes a normal expression using canvasRoot;
    private final RelativeLayout canvasRoot;
    private final RelativeLayout abovePaletteRoot;
    private boolean isAbovePalette;

    private ExpressionView exprView;
    // This is the original LayoutParams for the view that's attached to the root, or null if the
    // view is not attached to the root. When we detach, we set the layout params back. This is
    // necessary because layout params are both for the margin when in a LinearLayout and for the
    // position when in a RelativeLayout, and the original layout params came from an XML file, so
    // it's not trivial to just restore them.
    private LinearLayout.LayoutParams savedLayoutParams;

    // The execute button, which may or may not be attached to the root view.
    private View executeButton;
    private boolean isExecutable;

    public TopLevelExpressionView(DragObservableGenerator dragObservableGenerator,
            PointConverter pointConverter, RelativeLayout canvasRoot,
            RelativeLayout abovePaletteRoot, boolean isAbovePalette, ExpressionView exprView,
            View executeButton,
            boolean isExecutable) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.pointConverter = pointConverter;
        this.canvasRoot = canvasRoot;
        this.abovePaletteRoot = abovePaletteRoot;
        this.isAbovePalette = isAbovePalette;
        this.exprView = exprView;
        this.executeButton = executeButton;
        this.isExecutable = isExecutable;
    }

    public static TopLevelExpressionView render(
            ExpressionViewRenderer renderer, DragObservableGenerator dragObservableGenerator,
            PointConverter pointConverter, RelativeLayout canvasRoot,
            RelativeLayout abovePaletteRoot, boolean placeAbovePalette, ExpressionView exprView,
            boolean isExecutable) {
        View executeButton = renderer.makeExecuteButton();
        return new TopLevelExpressionView(dragObservableGenerator, pointConverter, canvasRoot,
                abovePaletteRoot, placeAbovePalette, exprView, executeButton, isExecutable);
    }

    public ScreenPoint getScreenPos() {
        return Views.getScreenPos(exprView.getNativeView());
    }

    public void attachToRoot(CanvasPoint canvasPos) {
        attach(pointConverter.toDrawableAreaPoint(canvasPos));
    }

    private void attach(DrawableAreaPoint point) {
        LinearLayout nativeView = exprView.getNativeView();
        Preconditions.checkState(!isAttached(), "Tried to attach view, but was already attached.");
        savedLayoutParams = (LinearLayout.LayoutParams) nativeView.getLayoutParams();
        rootView().addView(nativeView, Views.layoutParamsForRelativePos(point));

        invalidateExecuteButton(point);
    }

    public void detach() {
        Preconditions.checkState(isAttached(), "Tried to detach view, but was already detached.");
        LinearLayout nativeView = exprView.getNativeView();
        rootView().removeView(nativeView);
        nativeView.setLayoutParams(savedLayoutParams);
        savedLayoutParams = null;
    }

    private boolean isAttached() {
        boolean result = exprView.getNativeView().getParent() == rootView();
        Preconditions.checkState(
                result == (savedLayoutParams != null),
                "The top-level expression should be attached iff there are saved layout params.");
        return result;

    }

    public void setScreenPos(ScreenPoint screenPos) {
        exprView.getNativeView().setLayoutParams(
                Views.layoutParamsForRelativePos(pointConverter.toDrawableAreaPoint(screenPos)));

        DrawableAreaPoint expressionPos = pointConverter.toDrawableAreaPoint(screenPos);
        recomputeExecuteButtonPosition(expressionPos);
    }

    public void setCanvasPos(DrawableAreaPoint canvasPos) {
        exprView.getNativeView().setLayoutParams(Views.layoutParamsForRelativePos(canvasPos));
        invalidateExecuteButton(canvasPos);
    }

    public LinearLayout getNativeView() {
        return exprView.getNativeView();
    }

    public Observable<? extends Observable<PointerMotionEvent>> getExpressionObservable() {
        return dragObservableGenerator.getDragObservable(exprView.getNativeView());
    }

    public void startDrag() {
        // On older devices that don't support elevation, we need to move the dragged expression
        // to have the highest z-order.
        rootView().bringChildToFront(exprView.getNativeView());
        ViewPropertyAnimator animator = exprView.getNativeView().animate()
                .setDuration(100).scaleX(1.05f).scaleY(1.05f);
        Compat.translationZBy(animator, 10);
        executeButton.animate().setDuration(150).alpha(0);
        executeButton.setClickable(false);
    }

    public void endDrag() {
        if (isAbovePalette) {
            moveToCanvasRoot();
        }
        ViewPropertyAnimator animator = exprView.getNativeView().animate()
                .setDuration(100).scaleX(1.0f).scaleY(1.0f);
        Compat.translationZBy(animator, -10);
        executeButton.animate().setDuration(150).alpha(1);
        executeButton.setClickable(true);
    }

    /**
     * Migrate this expression from being attached to the abovePaletteRoot to being attached to the
     * canvasRoot. This is done for new expressions as soon as they are dropped onto the main canvas
     * after being dragged from the palette.
     */
    private void moveToCanvasRoot() {
        Preconditions.checkState(isAbovePalette);
        LinearLayout nativeView = exprView.getNativeView();
        Preconditions.checkState(nativeView.getParent() == abovePaletteRoot);
        // We rely on the fact that the views are in the same position, which means that the layout
        // params cary over when re-parenting. We also rely on the fact that the execute button
        // never shows up for expressions at this point, so we don't need to worry about it.
        abovePaletteRoot.removeView(nativeView);
        canvasRoot.addView(nativeView);
        // Setting this boolean means that all future calls to rootView() will return canvasRoot.
        isAbovePalette = false;
    }

    public void attachNewExpression(
            ExpressionView newExpression, DrawableAreaPoint drawableAreaPoint,
            boolean newIsExecutable) {
        Preconditions.checkState(!isAttached());
        exprView = newExpression;
        isExecutable = newIsExecutable;
        attach(drawableAreaPoint);
    }

    public void decommission() {
        detach();
        rootView().removeView(executeButton);
    }

    public interface OnExecuteListener {
        void execute();
    }

    public void setOnExecuteListener(OnExecuteListener listener) {
        executeButton.setOnClickListener((view) -> listener.execute());
    }

    private void invalidateExecuteButton(DrawableAreaPoint expressionPos) {
        rootView().removeView(executeButton);
        if (isExecutable) {
            recomputeExecuteButtonPosition(expressionPos);
            rootView().addView(executeButton);
        }
    }

    private void recomputeExecuteButtonPosition(DrawableAreaPoint expressionPos) {
        LinearLayout exprNativeView = exprView.getNativeView();
        exprNativeView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        executeButton.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // Center the execute button at the bottom-right corner, then shift down and to the right by
        // 8dp.
        int shiftAmount = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f,
                executeButton.getResources().getDisplayMetrics());
        DrawableAreaPoint executePos = expressionPos.plus(PointDifference.create(
                exprNativeView.getMeasuredWidth() -
                        (executeButton.getMeasuredWidth() / 2) +
                        shiftAmount,
                exprNativeView.getMeasuredHeight() -
                        (executeButton.getMeasuredHeight() / 2) +
                        shiftAmount)
        );
        Views.updateLayoutParamsToRelativePos(executeButton, executePos);
    }

    private RelativeLayout rootView() {
        if (isAbovePalette) {
            return abovePaletteRoot;
        } else {
            return canvasRoot;
        }
    }
}
