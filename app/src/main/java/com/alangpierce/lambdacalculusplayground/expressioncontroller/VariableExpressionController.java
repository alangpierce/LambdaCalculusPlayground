package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;
import com.alangpierce.lambdacalculusplayground.view.VariableView;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class VariableExpressionController implements ExpressionController {
    private final VariableView view;
    private final UserVariable userVariable;

    private OnChangeCallback onChangeCallback;

    public VariableExpressionController(VariableView view, UserVariable userVariable) {
        this.view = view;
        this.userVariable = userVariable;
    }

    @Override
    public UserExpression getExpression() {
        return userVariable;
    }

    @Override
    public ExpressionView getView() {
        return view;
    }

    @Override
    public void setOnChangeCallback(OnChangeCallback onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    public OnChangeCallback getOnChangeCallback() {
        return onChangeCallback;
    }

    @Override
    public List<DragSource> getDragSources() {
        return ImmutableList.of();
    }

    @Override
    public List<DropTarget> getDropTargets(FuncCallDropTarget.FuncCallControllerFactory funcCallFactory) {
        return ImmutableList.of(new FuncCallDropTarget(this, view, funcCallFactory));
    }
}
