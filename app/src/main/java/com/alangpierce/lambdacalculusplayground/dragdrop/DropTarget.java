package com.alangpierce.lambdacalculusplayground.dragdrop;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;

public interface DropTarget {
    /**
     * Determine if the dragged view is "over" the target.
     *
     * TODO: Lots of ways to improve this kind of hit testing:
     * - Change API so we can find the "closest" drop target out of all hits.
     * - Use a snazzy geometric data structure.
     *
     * Returns NOT_HIT if the view was not hit. Otherwise, returns an integer which is the priority
     * of the drop target. The priority is used to break ties when there are multiple drop targets.
     */
    int hitTest(TopLevelExpressionController dragController);

    int NOT_HIT = -1;

    /**
     * Change the display of the drop target to indicate that it is accepting drops.
     */
    void handleEnter(TopLevelExpressionController expressionController);

    /**
     * Change the display of the drop target back to normal.
     */
    void handleExit();

    /**
     * Update the display and data to indicate that the expression was dropped.
     */
    void handleDrop(TopLevelExpressionController expressionController);
}
