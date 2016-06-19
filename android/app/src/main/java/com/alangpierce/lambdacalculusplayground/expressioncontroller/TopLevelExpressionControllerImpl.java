package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.util.TypedValue;
import android.view.View.MeasureSpec;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController.ExpressionControllerProvider;
import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.geometry.PointDifference;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;

import javax.annotation.Nullable;

import rx.Subscription;

public class TopLevelExpressionControllerImpl implements TopLevelExpressionController {
    private final TopLevelExpressionView view;
    private final PointConverter pointConverter;

    private ScreenExpression screenExpression;
    private ExpressionController expressionController;
    private OnTopLevelChangeCallback onChangeCallback;

    private @Nullable Subscription dragActionSubscription;

    public TopLevelExpressionControllerImpl(TopLevelExpressionView view,
            PointConverter pointConverter, ScreenExpression screenExpression,
            ExpressionController expressionController) {
        this.view = view;
        this.pointConverter = pointConverter;
        this.screenExpression = screenExpression;
        this.expressionController = expressionController;
    }

    @Override
    public ScreenExpression getScreenExpression() {
        return screenExpression;
    }

    @Override
    public TopLevelExpressionView getView() {
        return view;
    }

    @Override
    public void setOnChangeCallback(OnTopLevelChangeCallback onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    @Override
    public void handlePositionChange(ScreenPoint screenPos) {
        CanvasPoint canvasPos = pointConverter.toCanvasPoint(screenPos);
        screenExpression = ScreenExpression.create(screenExpression.expr(), canvasPos);
        onChangeCallback.onChange(this);
    }

    @Override
    public void onPan() {
        // All DrawableAreaPoint values might be invalid, so recompute them and move if necessary.
        DrawableAreaPoint drawableAreaPoint =
                pointConverter.toDrawableAreaPoint(screenExpression.canvasPos());
        view.setCanvasPos(drawableAreaPoint);
    }

    public void handleExprChange(ExpressionControllerProvider newExpressionControllerProvider) {
        view.detach();
        ExpressionController newExpressionController =
                newExpressionControllerProvider.produceController();
        UserExpression newExpression = newExpressionController.getExpression();
        screenExpression = ScreenExpression.create(newExpression, screenExpression.canvasPos());
        boolean isExecutable = false;
        DrawableAreaPoint drawableAreaPoint =
                pointConverter.toDrawableAreaPoint(screenExpression.canvasPos());
        view.attachNewExpression(
                newExpressionController.getView(), drawableAreaPoint, isExecutable);
        newExpressionController.setOnChangeCallback(this::handleExprChange);
        this.expressionController = newExpressionController;
        onChangeCallback.onChange(this);
    }

    @Override
    public void invalidateDefinitions() {
        view.setIsExecutable(false);
    }

    @Override
    public ExpressionController decommission() {
        onChangeCallback.onChange(null);
        if (dragActionSubscription != null) {
            dragActionSubscription.unsubscribe();
        }
        view.decommission();
        return expressionController;
    }
}
