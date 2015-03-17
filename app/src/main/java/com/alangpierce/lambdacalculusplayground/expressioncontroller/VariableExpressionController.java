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
    private final FuncCallControllerFactory funcCallFactory;
    private final VariableView view;
    private final UserVariable userVariable;

    private OnChangeCallback onChangeCallback;

    public VariableExpressionController(FuncCallControllerFactory funcCallFactory,
            VariableView view, UserVariable userVariable) {
        this.funcCallFactory = funcCallFactory;
        this.view = view;
        this.userVariable = userVariable;
    }

    public interface FuncCallControllerFactory {
        FuncCallExpressionController createFuncCall(
                ExpressionController funcController, ExpressionController argController);
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

    @Override
    public List<DragSource> getDragSources() {
        return ImmutableList.of();
    }

    @Override
    public List<DropTarget> getDropTargets() {
        return ImmutableList.of(new FuncCallDropTarget());
    }

    private class FuncCallDropTarget implements DropTarget {
        @Override
        public boolean hitTest(TopLevelExpressionView dragView) {
            return view.rightEdgeIntersectsWith(dragView);
        }
        @Override
        public void handleEnter(TopLevelExpressionController expressionController) {
            view.handleDragEnter();
        }
        @Override
        public void handleExit() {
            view.handleDragExit();
        }
        @Override
        public void handleDrop(TopLevelExpressionController expressionController) {
            // Setting up the top-level expression modifies our callback so save it first.
            OnChangeCallback changeCallback = onChangeCallback;
            view.detach();
            ExpressionController funcController = VariableExpressionController.this;
            ExpressionController argController = expressionController.decommission();
            FuncCallExpressionController funcCallController =
                    funcCallFactory.createFuncCall(funcController, argController);
            changeCallback.onChange(funcCallController);
        }
    }
}
