package com.alangpierce.lambdacalculusplayground.dragdrop;

/**
 * An element that can receive drops. The type parameter T should be the subclass of DragData (which
 * could just be DragData itself) for the type of drops the drop target is willing to accept. The
 * drag manager will ensure that all other types are ignored for this drop target.
 */
public interface DropTarget<T extends DragData> {
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
    int hitTest(T dragData);

    int NOT_HIT = -1;

    /**
     * Change the display of the drop target to indicate that it is accepting drops.
     */
    void handleEnter(T dragData);

    /**
     * Change the display of the drop target back to normal.
     */
    void handleExit();

    /**
     * Update the display and data to indicate that the expression was dropped.
     */
    void handleDrop(T dragData);

    Class<T> getDataClass();
}
