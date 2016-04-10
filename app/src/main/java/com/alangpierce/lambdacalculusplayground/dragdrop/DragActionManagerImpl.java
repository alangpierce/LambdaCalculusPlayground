package com.alangpierce.lambdacalculusplayground.dragdrop;

import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.R;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.Views;

public class DragActionManagerImpl implements DragActionManager {
    private final Toolbar toolbar;
    private final LinearLayout dragActionBar;

    public DragActionManagerImpl(Toolbar toolbar, LinearLayout dragActionBar) {
        this.toolbar = toolbar;
        this.dragActionBar = dragActionBar;
    }

    @Override
    public void initDropTargets(DragManager dragManager) {
        dragManager.registerDropTarget(new DeleteDropTarget());
    }

    @Override
    public void handleDragDown() {
        toolbar.animate().setDuration(200).alpha(0);
    }

    @Override
    public void handleDragUp() {
        toolbar.animate().setDuration(200).alpha(1);
        dragActionBar.getParent().bringChildToFront(dragActionBar);
    }

    private class DeleteDropTarget implements DropTarget<TopLevelExpressionController> {
        @Override
        public int hitTest(TopLevelExpressionController dragData) {
            if (Views.viewsIntersect(dragActionBar, dragData.getView().getNativeView())) {
                return Integer.MAX_VALUE;
            } else {
                return DropTarget.NOT_HIT;
            }
        }
        @Override
        public void handleEnter(TopLevelExpressionController dragData) {
            dragActionBar.setBackgroundColor(getColor(R.color.drag_action_delete));
        }
        @Override
        public void handleExit() {
            dragActionBar.setBackgroundColor(getColor(R.color.drag_action_background));
        }
        @Override
        public void handleDrop(TopLevelExpressionController dragData) {
            dragData.decommission();
        }
        @Override
        public Class<TopLevelExpressionController> getDataClass() {
            return TopLevelExpressionController.class;
        }
    }

    private int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(dragActionBar.getContext(), resId);
    }
}
