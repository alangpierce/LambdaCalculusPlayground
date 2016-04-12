package com.alangpierce.lambdacalculusplayground.dragdrop;

import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alangpierce.lambdacalculusplayground.R;

public class DragActionManagerImpl implements DragActionManager {
    private final Toolbar toolbar;
    private final LinearLayout dragActionBar;
    private final TextView removeTextView;

    public DragActionManagerImpl(Toolbar toolbar, LinearLayout dragActionBar,
            TextView removeTextView) {
        this.toolbar = toolbar;
        this.dragActionBar = dragActionBar;
        this.removeTextView = removeTextView;
    }

    @Override
    public void initDropTargets(DragManager dragManager) {
        dragManager.registerDropTarget(new DeleteDropTarget());
    }

    @Override
    public void handleDragDown(DragData dragData) {
        dragActionBar.getParent().bringChildToFront(dragActionBar);
        // Change the phrasing based on what's being dragged.
        dragData.visit(
                expression -> {
                    removeTextView.setText(R.string.remove);
                    return null;
                },
                definition -> {
                    removeTextView.setText(R.string.hide);
                    return null;
                }
        );
        toolbar.animate().setDuration(200).alpha(0);
    }

    @Override
    public void handleDragUp() {
        toolbar.animate().setDuration(200).alpha(1);
        dragActionBar.getParent().bringChildToFront(dragActionBar);
    }

    private class DeleteDropTarget implements DropTarget<DragData> {
        @Override
        public int hitTest(DragData dragData) {
            if (dragData.intersectsWith(dragActionBar)) {
                return Integer.MAX_VALUE;
            } else {
                return DropTarget.NOT_HIT;
            }
        }
        @Override
        public void handleEnter(DragData dragData) {
            dragActionBar.setBackgroundColor(getColor(R.color.drag_action_delete));
        }
        @Override
        public void handleExit() {
            dragActionBar.setBackgroundColor(getColor(R.color.drag_action_background));
        }
        @Override
        public void handleDrop(DragData dragData) {
            dragData.destroy();
        }
        @Override
        public Class<DragData> getDataClass() {
            return DragData.class;
        }
    }

    private int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(dragActionBar.getContext(), resId);
    }
}
