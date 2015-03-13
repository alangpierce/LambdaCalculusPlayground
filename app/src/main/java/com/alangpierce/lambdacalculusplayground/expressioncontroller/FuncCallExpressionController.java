package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;

public class FuncCallExpressionController implements ExpressionController {
    private final LinearLayout view;

    /*
     * State kept by this class. Since this class corresponds to an actual Android view, we need to
     * care about what it's logically a part of as it moves around.
     */
    private OnChangeCallback onChangeCallback;
    private OnDetachCallback onDetachCallback;
    private UserFuncCall userFuncCall;
    private ExpressionController funcController;
    private ExpressionController argController;

    public FuncCallExpressionController(LinearLayout view) {
        this.view = view;
    }

    @Override
    public LinearLayout getView() {
        return view;
    }

    @Override
    public void setCallbacks(OnChangeCallback onChangeCallback, OnDetachCallback onDetachCallback) {
        this.onChangeCallback = onChangeCallback;
        this.onDetachCallback = onDetachCallback;
    }

    public void handleFuncDetach() {
        view.removeView(funcController.getView());
        funcController = null;
        handleFuncChange(null);
    }

    public void handleArgDetach() {
        view.removeView(argController.getView());
        argController = null;
        handleArgChange(null);
    }

    public void handleFuncChange(UserExpression newFunc) {
        userFuncCall = new UserFuncCall(newFunc, userFuncCall.arg);
        onChangeCallback.onChange(userFuncCall);
    }

    public void handleArgChange(UserExpression newArg) {
        userFuncCall = new UserFuncCall(userFuncCall.func, newArg);
        onChangeCallback.onChange(userFuncCall);
    }

    private void receiveFunc(ExpressionController newFunc) {
        view.addView(newFunc.getView(), 0);
    }

    private void receiveArg(ExpressionController newArg) {
        view.addView(newArg.getView(), 1);
    }
}
