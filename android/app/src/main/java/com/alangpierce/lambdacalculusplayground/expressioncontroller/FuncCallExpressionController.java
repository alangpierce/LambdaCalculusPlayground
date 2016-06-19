package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.CanvasManager;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.FuncCallView;

public class FuncCallExpressionController implements ExpressionController {
    private final FuncCallView view;

    /*
     * State kept by this class. Since this class corresponds to an actual Android view, we need to
     * care about what it's logically a part of as it moves around.
     */
    private ExpressionController funcController;
    private ExpressionController argController;
    private UserFuncCall userFuncCall;
    private OnChangeCallback onChangeCallback;

    public FuncCallExpressionController(
            CanvasManager canvasManager, FuncCallView view,
            ExpressionController funcController, ExpressionController argController,
            UserFuncCall userFuncCall) {
        this.view = view;
        this.funcController = funcController;
        this.argController = argController;
        this.userFuncCall = userFuncCall;
        setFuncViewEnabled(false);
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
    public OnChangeCallback getOnChangeCallback() {
        return onChangeCallback;
    }

    public void handleFuncChange(ExpressionControllerProvider funcControllerProvider) {
        setFuncViewEnabled(true);
        // Start accepting touch events for the arg view.
        funcController.getView().getNativeView().setEnabled(false);

        view.detachFunc();
        ExpressionController newFuncController = funcControllerProvider.produceController();
        userFuncCall = UserFuncCall.create(newFuncController.getExpression(), userFuncCall.arg());
        view.attachFunc(newFuncController.getView());
        newFuncController.setOnChangeCallback(this::handleFuncChange);
        funcController = newFuncController;
        onChangeCallback.onChange(() -> this);
        setFuncViewEnabled(false);
    }

    public void handleArgChange(ExpressionControllerProvider newArgControllerProvider) {
        view.detachArg();
        ExpressionController newArgController = newArgControllerProvider.produceController();
        userFuncCall = UserFuncCall.create(userFuncCall.func(), newArgController.getExpression());
        view.attachArg(newArgController.getView());
        newArgController.setOnChangeCallback(this::handleArgChange);
        argController = newArgController;
        onChangeCallback.onChange(() -> this);
    }

    /**
     * Enable or disable touch events for the function view. The behavior of function calls is that
     * the left side should not itself be draggable, and should fall through to the function call
     * drag handler. Unfortunately, the drag observable code always consumes motion events on any
     * views that have ever been registered, so it's hard to make it fall through in that case.
     * Instead, we tell Android to skip the touch handler completely by setting its enabled state to
     * false.
     */
    private void setFuncViewEnabled(boolean enabled) {
        funcController.getView().getNativeView().setEnabled(enabled);
    }
}
