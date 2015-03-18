package com.alangpierce.lambdacalculusplayground.palette;

import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;

public class PaletteController {
    private final PaletteView view;

    public PaletteController(PaletteView view) {
        this.view = view;
    }

    public void registerCallbacks(DragManager dragManager) {
        dragManager.registerDropTarget(new PaletteDeleteDropTarget());
    }

    private class PaletteDeleteDropTarget implements DropTarget {
        @Override
        public int hitTest(TopLevelExpressionController dragController) {
            if (Views.viewsIntersect(view.getNativeView(),
                    dragController.getView().getNativeView())) {
                // Don't let drop target shadowing let people drag things to the delete area.
                return Integer.MAX_VALUE;
            } else {
                return DropTarget.NOT_HIT;
            }
        }

        @Override
        public void handleEnter(TopLevelExpressionController expressionController) {
            if (!expressionController.isTrivialExpression()) {
                view.handleDeleteDragEnter();
            }
        }

        @Override
        public void handleExit() {
            view.handleDeleteDragExit();
        }

        @Override
        public void handleDrop(TopLevelExpressionController expressionController) {
            view.handleDeleteDragExit();
            expressionController.decommission();
        }
    }
}
