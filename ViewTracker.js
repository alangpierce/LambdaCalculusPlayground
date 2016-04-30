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

let viewMap: Immutable.Map<ExprPath, ScreenRect> = new Immutable.Map();

type NativeNode = {
    measure: (callback: MeasureOnSuccessCallback) => void,
};

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
    setTimeout(() => {
        viewRef.measure((x, y, width, height, pageX, pageY) => {
            viewMap = viewMap.set(path, t.newScreenRect(
                t.newScreenPoint(pageX, pageY),
                t.newScreenPoint(pageX + width, pageY + height)
            ));
        })
    });
};

export const getPositionOnScreen = (path: ExprPath): ?ScreenRect => {
    return viewMap.get(path);
};