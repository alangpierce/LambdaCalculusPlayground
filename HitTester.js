/**
 * @flow
 */
import * as Immutable from 'immutable'

import {getPositionOnScreen} from './ViewTracker';
import {ptInRect, ptMinusPt} from './Geometry';

import * as t from './types';
import type {
    DragData,
    DropResult,
    PointDifference,
    ScreenPoint,
    State,
} from './types';

type TouchResult = {
    exprId: number,
    offset: PointDifference,
}

/**
 * Given a click on the screen, figure out what expression it is under, if any.
 */
export const resolveTouch = (state: State, point: ScreenPoint): ?TouchResult => {
    let result = null;
    state.screenExpressions.keySeq().forEach((exprId) => {
        const viewKey = t.newExpressionKey(
            t.newExprPath(exprId, new Immutable.List()));
        const screenRect = getPositionOnScreen(viewKey);
        if (!screenRect) {
            return;
        }
        // TODO: Deal with tiebreaking.
        if (ptInRect(point, screenRect)) {
            result = {
                exprId,
                offset: ptMinusPt(point, screenRect.topLeft),
            };
        }
    });
    return result;
};

export const resolveDrop = (
        state: State, dragData: DragData, touchPos: ScreenPoint):
        DropResult => {
    return t.newAddToTopLevel(dragData.screenExpr);
};