/**
 * This file is responsible for keeping track of the screen position of all
 * views that interact with dragging and dropping. It does not implement any
 * dragging or dropping.
 *
 * @flow
 */
import * as Immutable from 'immutable';

import type {ExprPath, ScreenRect} from './types';
import * as t from './types';

type NativeNode = {
    measure: (callback: MeasureOnSuccessCallback) => void,
};

let nodeMap: Immutable.Map<ExprPath, NativeNode> = new Immutable.Map();
let viewMap: Immutable.Map<ExprPath, ScreenRect> = new Immutable.Map();

type MeasureOnSuccessCallback = (
    x: number,
    y: number,
    width: number,
    height: number,
    pageX: number,
    pageY: number
) => void;

// TODO: Unregister views.
// TODO: Handle when views move.
export const registerView = (path: ExprPath, viewRef: NativeNode) => {
    nodeMap = nodeMap.set(path, viewRef);
};

export const unregisterView = (path: ExprPath, viewRef: NativeNode) => {
    const existingRef = nodeMap.get(path);
    if (existingRef === viewRef) {
        nodeMap = nodeMap.delete(path);
        viewMap = viewMap.delete(path);
    }
};

// TODO: Don't run this on init; explicitly start it.
// TODO: Yuck. Change this to invalidate the positions properly (or some
// other nice solution) instead of recomputing it all the time.
setInterval(() => {
    Array.from(nodeMap).map(([exprPath, viewRef]) => {
        viewRef.measure((x, y, width, height, pageX, pageY) => {
            // If the node gets removed in between, do nothing.
            if (!nodeMap.get(exprPath)) {
                return;
            }
            viewMap = viewMap.set(exprPath, t.newScreenRect(
                t.newScreenPoint(pageX, pageY),
                t.newScreenPoint(pageX + width, pageY + height)
            ));
        })
    });
}, 500);

export const getPositionOnScreen = (path: ExprPath): ?ScreenRect => {
    return viewMap.get(path);
};