package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.view.View;

import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.FuncCallView;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class FuncCallExpressionController implements ExpressionController {
    private final FuncCallView view;

    /*
     * State kept by this class. Since this class corresponds to an actual Android view, we need to
     * care about what it's logically a part of as it moves around.
     */
    private UserFuncCall userFuncCall;
    private OnChangeCallback onChangeCallback;

    public FuncCallExpressionController(FuncCallView view, UserFuncCall userFuncCall) {
        this.view = view;
        this.userFuncCall = userFuncCall;
    }

    @Override
    public ExpressionView getView() {
        return view;
    }

    @Override
    public void setOnChangeCallback(OnChangeCallback onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    @Override
    public List<DragSource> getDragSources() {
        return ImmutableList.of();
    }

    @Override
    public List<DropTarget> getDropTargets() {
        return ImmutableList.of();
    }

    public void handleFuncChange(UserExpression newFunc) {
        userFuncCall = new UserFuncCall(newFunc, userFuncCall.arg);
        onChangeCallback.onChange(userFuncCall);
    }

    public void handleArgChange(UserExpression newArg) {
        userFuncCall = new UserFuncCall(userFuncCall.func, newArg);
        onChangeCallback.onChange(userFuncCall);
    }
}
