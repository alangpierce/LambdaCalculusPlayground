package com.alangpierce.lambdacalculusplayground.palette;

import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;

import rx.Observable;

public class PaletteLambdaController {
    private final TopLevelExpressionManager topLevelExpressionManager;
    private final LambdaView view;
    private final String varName;

    public PaletteLambdaController(TopLevelExpressionManager topLevelExpressionManager,
                                   LambdaView view, String varName) {
        this.topLevelExpressionManager = topLevelExpressionManager;
        this.view = view;
        this.varName = varName;
    }

    public void registerCallbacks(DragManager dragManager) {
        dragManager.registerDragSource(new PaletteDragSource());
    }

    public LambdaView getView() {
        return view;
    }

    private class PaletteDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return view.getWholeExpressionObservable();
        }
        @Override
        public TopLevelExpressionController handleStartDrag() {
            return topLevelExpressionManager.createNewExpression(
                    UserLambda.create(varName, null), view.getScreenPos());
        }
    }
}
