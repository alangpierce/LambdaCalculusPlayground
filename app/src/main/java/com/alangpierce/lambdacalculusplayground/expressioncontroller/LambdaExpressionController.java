package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.CanvasManager;
import com.alangpierce.lambdacalculusplayground.component.ProducerController;
import com.alangpierce.lambdacalculusplayground.component.ProducerControllerParent;
import com.alangpierce.lambdacalculusplayground.component.SlotController;
import com.alangpierce.lambdacalculusplayground.component.SlotControllerParent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.FuncCallDropTarget.FuncCallControllerFactory;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nullable;

public class LambdaExpressionController implements ExpressionController {
    private final LambdaView view;
    private final ProducerController parameterProducerController;
    private final SlotController bodySlotController;

    private UserLambda userLambda;
    private OnChangeCallback onChangeCallback;

    public LambdaExpressionController(
            LambdaView view, ProducerController parameterProducerController,
            SlotController bodySlotController, UserLambda userLambda) {
        this.view = view;
        this.parameterProducerController = parameterProducerController;
        this.bodySlotController = bodySlotController;
        this.userLambda = userLambda;
    }

    public static ProducerControllerParent createProducerParent(
            CanvasManager canvasManager, String varName) {
        return new ProducerControllerParent() {
            @Override
            public TopLevelExpressionController produceExpression(ScreenPoint point) {
                return canvasManager.createNewExpression(
                        UserVariable.create(varName), point, false /* placeAbovePalette */);
            }
            @Override
            public boolean shouldDeleteExpression(UserExpression expression) {
                return expression instanceof UserVariable;
            }
        };
    }

    public SlotControllerParent createSlotParent() {
        return new SlotControllerParent() {
            @Override
            public void updateSlotExpression(@Nullable UserExpression userExpression) {
                userLambda = UserLambda.create(userLambda.varName(), userExpression);
            }
            @Override
            public void handleChange() {
                onChangeCallback.onChange(() -> LambdaExpressionController.this);
            }
        };
    }

    @Override
    public void invalidateDefinitions() {
        bodySlotController.invalidateDefinitions();
    }

    @Override
    public void setOnChangeCallback(OnChangeCallback onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    @Override
    public OnChangeCallback getOnChangeCallback() {
        return onChangeCallback;
    }

    @Override
    public UserExpression getExpression() {
        return userLambda;
    }

    @Override
    public ExpressionView getView() {
        return view;
    }

    @Override
    public List<DragSource> getDragSources() {
        return ImmutableList.of(
                parameterProducerController.getDragSource(),
                bodySlotController.getDragSource());
    }

    @Override
    public List<DropTarget<?>> getDropTargets(FuncCallControllerFactory funcCallFactory) {
        return ImmutableList.of(
                parameterProducerController.getDropTarget(),
                bodySlotController.getDropTarget(),
                new FuncCallDropTarget(this, view, funcCallFactory));
    }
}
