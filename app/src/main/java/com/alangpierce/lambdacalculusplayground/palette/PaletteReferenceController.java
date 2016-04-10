package com.alangpierce.lambdacalculusplayground.palette;

import com.alangpierce.lambdacalculusplayground.CanvasManager;
import com.alangpierce.lambdacalculusplayground.component.ProducerController;
import com.alangpierce.lambdacalculusplayground.component.ProducerControllerParent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserReference;
import com.alangpierce.lambdacalculusplayground.view.ReferenceView;

public class PaletteReferenceController {
    private final ProducerController producerController;
    private final ReferenceView referenceView;

    public PaletteReferenceController(
            ProducerController producerController, ReferenceView referenceView) {
        this.producerController = producerController;
        this.referenceView = referenceView;
    }

    public static ProducerControllerParent createProducerParent(
            CanvasManager canvasManager, String defName) {
        return new ProducerControllerParent() {
            @Override
            public TopLevelExpressionController produceExpression(ScreenPoint point) {
                return canvasManager.createNewExpression(
                        UserReference.create(defName), point, true /* placeAbovePalette */);
            }
            @Override
            public boolean shouldDeleteExpression(UserExpression expression) {
                return expression instanceof UserReference;
            }
        };
    }

    public void registerCallbacks(DragManager dragManager) {
        dragManager.registerDragSource(producerController.getDragSource());
        dragManager.registerDropTarget(producerController.getDropTarget());
    }

    public ReferenceView getView() {
        return referenceView;
    }
}
