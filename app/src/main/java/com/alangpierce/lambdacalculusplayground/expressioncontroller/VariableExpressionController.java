package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.VariableView;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class VariableExpressionController implements ExpressionController {
    private final VariableView view;

    private OnChangeCallback onChangeCallback;
    private OnDetachCallback onDetachCallback;

    public VariableExpressionController(VariableView view) {
        this.view = view;
    }

    @Override
    public ExpressionView getView() {
        return view;
    }

    @Override
    public void setCallbacks(OnChangeCallback onChangeCallback, OnDetachCallback onDetachCallback) {
        this.onChangeCallback = onChangeCallback;
        this.onDetachCallback = onDetachCallback;
    }

    @Override
    public List<DragSource> getDragSources() {
        return ImmutableList.of();
    }

    @Override
    public List<DropTarget> getDropTargets() {
        return ImmutableList.of();
    }
}
