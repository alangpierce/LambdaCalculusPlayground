package com.alangpierce.lambdacalculusplayground.palette;

import com.alangpierce.lambdacalculusplayground.CanvasManager;
import com.alangpierce.lambdacalculusplayground.component.ProducerController;
import com.alangpierce.lambdacalculusplayground.component.ProducerControllerParent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;

public class PaletteLambdaController {
    private final ProducerController producerController;
    private final LambdaView view;

    public PaletteLambdaController(
            ProducerController producerController,
            LambdaView view) {
        this.producerController = producerController;
        this.view = view;
    }

    public static ProducerControllerParent createProducerParent(
            CanvasManager canvasManager, String varName) {
        return new ProducerControllerParent() {
            @Override
            public TopLevelExpressionController produceExpression(ScreenPoint point) {
                return canvasManager.createNewExpression(
                        UserLambda.create(varName, null), point, true /* placeAbovePalette */);
            }
            @Override
            public boolean shouldDeleteExpression(UserExpression expression) {
                return expression instanceof UserLambda && ((UserLambda) expression).body() == null;
            }
        };
    }

    public void registerCallbacks(DragManager dragManager) {
        dragManager.registerDragSource(producerController.getDragSource());
        dragManager.registerDropTarget(producerController.getDropTarget());
    }

    public LambdaView getView() {
        return view;
    }
}
