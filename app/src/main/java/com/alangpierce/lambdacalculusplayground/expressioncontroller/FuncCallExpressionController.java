package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.FuncCallView;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nullable;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class FuncCallExpressionController implements ExpressionController {
    private final TopLevelExpressionManager topLevelExpressionManager;
    private final FuncCallView view;

    /*
     * State kept by this class. Since this class corresponds to an actual Android view, we need to
     * care about what it's logically a part of as it moves around.
     */
    private ExpressionController funcController;
    private ExpressionController argController;
    private UserFuncCall userFuncCall;
    private OnChangeCallback onChangeCallback;

    private final Subject<Observable<PointerMotionEvent>,Observable<PointerMotionEvent>>
            argDragActionSubject = PublishSubject.create();
    private @Nullable Subscription argDragActionSubscription;

    public FuncCallExpressionController(
            TopLevelExpressionManager topLevelExpressionManager, FuncCallView view,
            ExpressionController funcController, ExpressionController argController,
            UserFuncCall userFuncCall) {
        this.topLevelExpressionManager = topLevelExpressionManager;
        this.view = view;
        this.funcController = funcController;
        this.argController = argController;
        this.userFuncCall = userFuncCall;
    }

    @Override
    public UserExpression getExpression() {
        return userFuncCall;
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
        updateDragActionSubscription();
        return ImmutableList.of(new ArgDragSource());
    }

    @Override
    public List<DropTarget> getDropTargets() {
        return ImmutableList.of();
    }

    public void handleFuncChange(ExpressionController newFuncController) {
        userFuncCall = new UserFuncCall(newFuncController.getExpression(), userFuncCall.arg);
        view.handleFuncChange(newFuncController.getView());
        newFuncController.setOnChangeCallback(this::handleFuncChange);
        funcController = newFuncController;
        onChangeCallback.onChange(this);
    }

    public void handleArgChange(ExpressionController newArgController) {
        userFuncCall = new UserFuncCall(userFuncCall.func, newArgController.getExpression());
        view.handleArgChange(newArgController.getView());
        updateDragActionSubscription();
        newArgController.setOnChangeCallback(this::handleArgChange);
        argController = newArgController;
        onChangeCallback.onChange(this);
    }

    private void updateDragActionSubscription() {
        if (argDragActionSubscription != null) {
            argDragActionSubscription.unsubscribe();
            argDragActionSubscription = null;
        }
        argDragActionSubscription = view.getArgObservable().subscribe(argDragActionSubject);
    }

    private void decommission() {
        if (argDragActionSubscription != null) {
            argDragActionSubscription.unsubscribe();
        }
        view.decommission();
    }

    private class ArgDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return argDragActionSubject;
        }
        @Override
        public TopLevelExpressionController handleStartDrag() {
            Point screenPos = view.getScreenPos();
            decommission();
            onChangeCallback.onChange(funcController);
            return topLevelExpressionManager.sendExpressionToTopLevel(argController, screenPos);
        }
    }
}
