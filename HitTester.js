/**
 * @flow
 */
import {emptyPath, step} from './ExprPaths';
import {getPositionOnScreen} from './ViewTracker';
import {ptInRect, ptMinusPt} from './Geometry';

import * as t from './types';
import type {
    DragData,
    DropResult,
    ExprPath,
    PointDifference,
    ScreenPoint,
    State,
    UserExpression,
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
    for (let [exprId] of state.screenExpressions) {
        const viewKey = t.newExpressionKey(emptyPath(exprId));
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
    }
    return result;
};

/**
 * Given a dragged item, determine the action that would happen if the item was
 * dropped. This is useful both to perform the drop and to do highlighting.
 */
export const resolveDrop = (
        state: State, dragData: DragData, touchPos: ScreenPoint):
        DropResult => {
    for (let [path, expr] of yieldAllExpressions(state)) {
        if (expr.type === 'userLambda' && !expr.body) {
            const rect = getPositionOnScreen(t.newEmptyBodyKey(path));
            if (rect && ptInRect(touchPos, rect)) {
                return t.newInsertAsBodyResult(path, dragData.screenExpr.expr);
            }
        }
    }
    return t.newAddToTopLevelResult(dragData.screenExpr);
};

const yieldAllExpressions = function* (state: State):
        Generator<[ExprPath, UserExpression], void, void> {
    for (let [exprId, screenExpr] of state.screenExpressions) {
        yield* yieldExpressions(screenExpr.expr, emptyPath(exprId))
    }
};

const yieldExpressions = function* (expr: UserExpression, path: ExprPath):
        Generator<[ExprPath, UserExpression], void, void> {
    yield [path, expr];
    yield* t.matchUserExpression(expr, {
        userLambda: function* ({body}) {
            if (body) {
                yield* yieldExpressions(body, step(path, 'body'));
            }
        },
        userFuncCall: function* ({func, arg}) {
            yield* yieldExpressions(func, step(path, 'func'));
            yield* yieldExpressions(arg, step(path, 'arg'));
        },
        userVariable: function* () {},
        userReference: function* () {},
    })
};