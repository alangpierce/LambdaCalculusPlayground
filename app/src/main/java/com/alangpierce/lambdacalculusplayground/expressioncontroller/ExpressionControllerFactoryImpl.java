package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTargetRegistry;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression.UserExpressionVisitor;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;

import javax.annotation.Nullable;

import rx.Observable;

public class ExpressionControllerFactoryImpl implements ExpressionControllerFactory {
    private final RelativeLayout rootView;
    private final ExpressionViewRenderer viewRenderer;
    private final DragObservableGenerator dragObservableGenerator;
    private final DropTargetRegistry dropTargetRegistry;

    public ExpressionControllerFactoryImpl(RelativeLayout rootView,
            ExpressionViewRenderer viewRenderer,
            DragObservableGenerator dragObservableGenerator,
            DropTargetRegistry dropTargetRegistry) {
        this.rootView = rootView;
        this.viewRenderer = viewRenderer;
        this.dragObservableGenerator = dragObservableGenerator;
        this.dropTargetRegistry = dropTargetRegistry;
    }

    public static ExpressionControllerFactoryFactory createFactory(
            ExpressionViewRenderer viewRenderer, DragObservableGenerator dragObservableGenerator,
            DropTargetRegistry dropTargetRegistry) {
        return (rootView) -> new ExpressionControllerFactoryImpl(
                rootView, viewRenderer, dragObservableGenerator, dropTargetRegistry);
    }

    @Override
    public TopLevelExpressionController createTopLevelController(
            ScreenExpression screenExpression) {
        ExpressionController exprController = createController(screenExpression.expr);
        TopLevelExpressionControllerImpl result =
                new TopLevelExpressionControllerImpl(exprController.getView(), screenExpression);
        exprController.setCallbacks(result::handleExprChange, result::handleExprDetach);
        return result;
    }

    @Override
    public ExpressionController createController(
            UserExpression userExpression) {
        return userExpression.visit(new UserExpressionVisitor<ExpressionController>() {
            @Override
            public ExpressionController visit(UserLambda lambda) {
                @Nullable ExpressionController bodyController = null;
                if (lambda.body != null) {
                    bodyController = createController(lambda.body);
                }
                LinearLayout view = viewRenderer.makeLambdaView(
                        lambda.varName, bodyController != null ? bodyController.getView() : null);
                LambdaExpressionController result = new LambdaExpressionController(view, lambda);
                setUpDragging(result);
                if (bodyController != null) {
                    bodyController.setCallbacks(result::handleBodyChange, result::handleBodyDetach);
                }
                return result;
            }
            @Override
            public ExpressionController visit(UserFuncCall funcCall) {
                ExpressionController funcController = createController(funcCall.func);
                ExpressionController argController = createController(funcCall.arg);

                LinearLayout view = viewRenderer.makeFuncCallView(
                        funcController.getView(), argController.getView());

                FuncCallExpressionController result =
                        new FuncCallExpressionController(view, funcCall);
                funcController.setCallbacks(result::handleFuncChange, result::handleFuncDetach);
                argController.setCallbacks(result::handleArgChange, result::handleArgDetach);
                return result;
            }
            @Override
            public ExpressionController visit(UserVariable variable) {
                LinearLayout view = viewRenderer.makeVariableView(variable.varName);
                return new VariableExpressionController(view);
            }
        });
    }

    private void setUpDragging(LambdaExpressionController controller) {
        DragSource dragSource = controller.getDragSource();
        Observable<? extends Observable<PointerMotionEvent>> dragObservable =
                dragObservableGenerator.getDragObservable(dragSource.getDragSourceView());
        dragObservable.subscribe(eventObservable ->
                dragSource.handleStartDrag(rootView, eventObservable)
                // TODO: Actually do something here.
                .subscribe(event -> {
                }));
    }
}
