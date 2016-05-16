/**
 * This file is responsible for keeping track of the screen position of all
 * views that interact with dragging and dropping. It does not implement any
 * dragging or dropping.
 *
 * @flow
 */
import type {ViewKey, ScreenRect} from './types';
import * as t from './types';
import {IMap} from './types-collections'

type NativeNode = {
    measure: (callback: MeasureOnSuccessCallback) => void,
};

let nodeMap: IMap<ViewKey, NativeNode> = IMap.make();
let viewMap: IMap<ViewKey, ScreenRect> = IMap.make();

type MeasureOnSuccessCallback = (
    x: number,
    y: number,
    width: number,
    height: number,
    pageX: number,
    pageY: number
) => void;

// TODO: Handle when views move.
export const registerView = (key: ViewKey, viewRef: NativeNode) => {
    nodeMap = nodeMap.set(key, viewRef);
};

export const unregisterView = (key: ViewKey, viewRef: NativeNode) => {
    const existingRef = nodeMap.get(key);
    if (existingRef === viewRef) {
        nodeMap = nodeMap.delete(key);
        viewMap = viewMap.delete(key);
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
            viewMap = viewMap.set(exprPath, t.ScreenRect.make(
                t.ScreenPoint.make(pageX, pageY),
                t.ScreenPoint.make(pageX + width, pageY + height)
            ));
        })
    });
}, 500);

export const getPositionOnScreen = (key: ViewKey): ?ScreenRect => {
    return viewMap.get(key);
};