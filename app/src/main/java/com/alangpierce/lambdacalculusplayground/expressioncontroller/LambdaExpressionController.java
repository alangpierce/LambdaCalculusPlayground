package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.component.SlotController;
import com.alangpierce.lambdacalculusplayground.component.SlotControllerParent;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.FuncCallDropTarget.FuncCallControllerFactory;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nullable;

import rx.Observable;

public class LambdaExpressionController implements ExpressionController {
    private final TopLevelExpressionManager topLevelExpressionManager;
    private final LambdaView view;
    private final SlotController bodySlotController;

    private UserLambda userLambda;
    private OnChangeCallback onChangeCallback;

    public LambdaExpressionController(
            TopLevelExpressionManager topLevelExpressionManager, LambdaView view,
            SlotController bodySlotController, UserLambda userLambda) {
        this.topLevelExpressionManager = topLevelExpressionManager;
        this.view = view;
        this.bodySlotController = bodySlotController;
        this.userLambda = userLambda;
    }

    public static LambdaExpressionController create(
            TopLevelExpressionManager topLevelExpressionManager, LambdaView view,
            UserLambda userLambda, @Nullable ExpressionController bodyController) {
        SlotController bodySlotController = new SlotController(
                topLevelExpressionManager, view.getBodySlot(), bodyController);
        LambdaExpressionController result = new LambdaExpressionController(
                topLevelExpressionManager, view, bodySlotController, userLambda);
        bodySlotController.setParent(result.createSlotParent());
        return result;
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
        return ImmutableList.of(bodySlotController.getDragSource(), new ParameterDragSource());
    }

    @Override
    public List<DropTarget<?>> getDropTargets(FuncCallControllerFactory funcCallFactory) {
        return ImmutableList.of(
                bodySlotController.getDropTarget(),
                new FuncCallDropTarget(this, view, funcCallFactory),
                new ParameterDropTarget());
    }

    private class ParameterDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            // The parameter shouldn't ever change, so no need to use a subject.
            return view.getParameterObservable();
        }
        @Override
        public TopLevelExpressionController handleStartDrag() {
            return topLevelExpressionManager.createNewExpression(
                    UserVariable.create(userLambda.varName()), view.getParameterPos(),
                    false /* placeAbovePalette */);
        }
    }

    /**
     * Dropping a variable back should make it disappear instead of awkwardly stick around on the
     * canvas.
     */
    private class ParameterDropTarget implements DropTarget<TopLevelExpressionController> {
        @Override
        public int hitTest(TopLevelExpressionController dragController) {
            if (dragController.getScreenExpression().expr() instanceof UserVariable &&
                    view.parameterIntersectsWith(dragController.getView())) {
                // Always have the lowest possible priority, since the only thing we're avoiding
                // here is putting
                return 0;
            } else {
                return DropTarget.NOT_HIT;
            }
        }
        @Override
        public void handleEnter(TopLevelExpressionController expressionController) {
            // Do nothing
        }
        @Override
        public void handleExit() {
            // Do nothing
        }
        @Override
        public void handleDrop(TopLevelExpressionController expressionController) {
            // Just throw the variable away.
            expressionController.decommission();
        }

        @Override
        public Class<TopLevelExpressionController> getDataClass() {
            return TopLevelExpressionController.class;
        }
    }
}
