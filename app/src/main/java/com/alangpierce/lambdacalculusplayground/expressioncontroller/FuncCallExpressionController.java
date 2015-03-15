package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.FuncCallView;
import com.google.common.collect.ImmutableList;

import java.util.List;

import rx.Observable;
import rx.Subscription;

public class FuncCallExpressionController implements ExpressionController {
    private final ExpressionControllerFactory controllerFactory;
    private final FuncCallView view;
    private final ExpressionController argController;

    /*
     * State kept by this class. Since this class corresponds to an actual Android view, we need to
     * care about what it's logically a part of as it moves around.
     */
    private UserFuncCall userFuncCall;
    private OnChangeCallback onChangeCallback;

    public FuncCallExpressionController(
            ExpressionControllerFactory controllerFactory,
            FuncCallView view,
            ExpressionController argController,
            UserFuncCall userFuncCall) {
        this.controllerFactory = controllerFactory;
        this.view = view;
        this.argController = argController;
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
        return ImmutableList.of(new ArgDragSource());
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

    private class ArgDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return view.getArgObservable();
        }
        @Override
        public TopLevelExpressionController handleStartDrag(Subscription subscription) {
            subscription.unsubscribe();
            ExpressionView argView = view.detachArg();
            return controllerFactory.wrapInTopLevelController(
                    argController,
                    ScreenExpression.create(userFuncCall.arg, argView.getScreenPos()));
        }
    }
}
