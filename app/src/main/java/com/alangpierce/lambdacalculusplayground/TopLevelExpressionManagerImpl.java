package com.alangpierce.lambdacalculusplayground;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;

import java.util.Map.Entry;

public class TopLevelExpressionManagerImpl implements TopLevelExpressionManager {
    private final TopLevelExpressionState expressionState;
    private final RelativeLayout rootView;
    private final ExpressionControllerFactory controllerFactory;

    public TopLevelExpressionManagerImpl(
            TopLevelExpressionState expressionState, RelativeLayout rootView,
            ExpressionControllerFactory controllerFactory) {
        this.expressionState = expressionState;
        this.rootView = rootView;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void renderInitialExpressions() {
        for (Entry<Integer, ScreenExpression> entry : expressionState.expressionsById()) {
            int exprId = entry.getKey();
            ScreenExpression screenExpression = entry.getValue();
            renderTopLevelExpression(exprId, screenExpression);
        }
    }

    /**
     * Given a new expression, create a view for it and hook up all necessary callbacks.
     */
    private void renderTopLevelExpression(int exprId, ScreenExpression screenExpression) {
        TopLevelExpressionController controller =
                controllerFactory.createTopLevelController(screenExpression);
        controller.setCallbacks(
                // onChange
                (newScreenExpression) ->
                        expressionState.modifyExpression(exprId, newScreenExpression),
                // onDetach
                rootView::removeView);
        RelativeLayout.LayoutParams expressionParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        expressionParams.leftMargin = screenExpression.x;
        expressionParams.topMargin = screenExpression.y;
        rootView.addView(controller.getView().getNativeView(), expressionParams);
    }
}
