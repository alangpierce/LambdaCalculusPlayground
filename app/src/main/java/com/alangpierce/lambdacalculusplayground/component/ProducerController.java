package com.alangpierce.lambdacalculusplayground.component;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;

import rx.Observable;

public class ProducerController {
    private final ProducerView view;
    private final ProducerControllerParent parent;

    public ProducerController(ProducerView view,
            ProducerControllerParent parent) {
        this.view = view;
        this.parent = parent;
    }

    public DragSource getDragSource() {
        return new ProducerDragSource();
    }

    public DropTarget<?> getDropTarget() {
        return new ProducerDropTarget();
    }

    private class ProducerDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            // The view shouldn't ever change, so no need to use a subject.
            return view.getObservable();
        }
        @Override
        public TopLevelExpressionController handleStartDrag() {
            return parent.produceExpression(view.getPos());
        }
    }

    /**
     * Dropping a variable back should make it disappear instead of awkwardly stick around on the
     * canvas.
     */
    private class ProducerDropTarget implements DropTarget<TopLevelExpressionController> {
        @Override
        public int hitTest(TopLevelExpressionController dragController) {
            if (dragController.getScreenExpression().expr() instanceof UserVariable &&
                    view.intersectsWith(dragController.getView())) {
                // Always have the lowest possible priority.
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
