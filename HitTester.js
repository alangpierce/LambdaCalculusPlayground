/**
 * @flow
 */
import * as Immutable from 'immutable'

import {getPositionOnScreen} from './ViewTracker';
import {ptInRect} from './Geometry';

import * as t from './types';
import type {
    ScreenPoint,
    State,
} from './types';

/**
 * Given a click on the screen, figure out what expression it is under, if any.
 */
export const resolveTouch = (state: State, point: ScreenPoint): ?number => {
    let result = null;
    state.screenExpressions.keySeq().forEach((exprId) => {
        const screenRect =
            getPositionOnScreen(t.newExprPath(exprId, new Immutable.List()));
        if (!screenRect) {
            return;
        }
        // TODO: Deal with tiebreaking.
        if (ptInRect(point, screenRect)) {
            result = exprId;
        }
    });
    return result;
};