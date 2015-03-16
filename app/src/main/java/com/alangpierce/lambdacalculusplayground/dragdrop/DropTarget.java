package com.alangpierce.lambdacalculusplayground.dragdrop;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.Rect;

public interface DropTarget {
    /**
     * Return true if the dragged view is "over" the target.
     *
     * TODO: Lots of ways to improve this kind of hit testing:
     * - Add a concept of "priority" so we can deterministically choose which target is hit.
     * - Change API so we can find the "closest" drop target out of all hits.
     * - Use a snazzy geometric data structure.
     */
    boolean hitTest(Rect dragRect);

    /**
     * Change the display of the drop target to indicate that it is accepting drops.
     */
    void handleEnter(ExpressionController expressionController);

    /**
     * Change the display of the drop target back to normal.
     */
    void handleExit();

    /**
     * Update the display and data to indicate that the expression was dropped.
     */
    void handleDrop(TopLevelExpressionController expressionController);
}
