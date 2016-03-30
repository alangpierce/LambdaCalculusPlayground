package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserReference;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.ReferenceView;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class ReferenceExpressionController implements ExpressionController {
    private final ReferenceView view;
    private final UserReference userReference;

    private OnChangeCallback onChangeCallback;

    public ReferenceExpressionController(ReferenceView view, UserReference userReference) {
        this.view = view;
        this.userReference = userReference;
    }

    @Override
    public UserExpression getExpression() {
        return userReference;
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
