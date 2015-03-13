package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
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

    public ExpressionControllerFactoryImpl(
            RelativeLayout rootView, ExpressionViewRenderer viewRenderer,
            DragObservableGenerator dragObservableGenerator) {
        this.rootView = rootView;
        this.viewRenderer = viewRenderer;
        this.dragObservableGenerator = dragObservableGenerator;
    }

    public static ExpressionControllerFactoryFactory createFactory(
            ExpressionViewRenderer viewRenderer,
            DragObservableGenerator dragObservableGenerator) {
        return (rootView) -> new ExpressionControllerFactoryImpl(
                rootView, viewRenderer, dragObservableGenerator);
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
        View view = controller.getView();

        Observable<? extends Observable<PointerMotionEvent>> dragObservable =
                dragObservableGenerator.getDragObservable(view);
        dragObservable.subscribe(eventObservable ->
                controller.handleDragAction(rootView, eventObservable));
    }
}
