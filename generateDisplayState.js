/**
 * @flow
 */

import * as Immutable from 'immutable'

import {emptyPath, step} from './ExprPaths'
import {canvasPtToScreenPt} from './PointConversion'
import store from './store'
import * as t from './types'
import type {
    DisplayExpression,
    DisplayState,
    ExprPath,
    ScreenExpression,
    State,
    UserExpression,
} from './types'
import {canStepUserExpr} from './UserExpressionEvaluator'

const executeHandler = (exprId) => {
    return () => {
        store.dispatch(t.newEvaluateExpression(exprId));
    };
};

const generateDisplayState = (state: State): DisplayState =>  {
    const screenExpressions: Array<ScreenExpression> = [];
    const {highlightedExprs, highlightedEmptyBodies} = state;
    
    for (let [exprId, canvasExpr] of state.canvasExpressions) {
        const displayExpr = buildDisplayExpression(
            canvasExpr.expr, exprId, highlightedExprs, highlightedEmptyBodies);
        const isDragging = false;
        const isExecutable = canStepUserExpr(canvasExpr.expr);
        screenExpressions.push(t.newScreenExpression(
            displayExpr,
            canvasPtToScreenPt(canvasExpr.pos),
            'expr' + exprId,
            isDragging,
            isExecutable ? executeHandler(exprId) : null,
        ));
    }

    for (let [fingerId, dragData] of state.activeDrags) {
        const displayExpr = buildDisplayExpression(
            dragData.userExpr, null, highlightedExprs, highlightedEmptyBodies);
        const isDragging = true;
        const executeHandler = null;
        screenExpressions.push(t.newScreenExpression(
            displayExpr,
            dragData.screenRect.topLeft,
            'drag' + fingerId,
            isDragging,
            executeHandler,
        ));
    }
    return t.newDisplayState(new Immutable.List(screenExpressions));
};

/**
 * Build a DisplayExpression with paths for the given expression ID. If exprId
 * is null, no paths are attached.
 */
const buildDisplayExpression = (
        userExpr: UserExpression, exprId: ?number,
        highlightedExprs: Immutable.Set<ExprPath>,
        highlightedEmptyBodies: Immutable.Set<ExprPath>): DisplayExpression => {
    const rec = (expr: UserExpression, path: ?ExprPath): DisplayExpression => {
        const exprKey = path && t.newExpressionKey(path);
        const shouldHighlight = path != null && highlightedExprs.has(path);
        return t.matchUserExpression(expr, {
            userLambda: ({varName, body}) => {
                const varKey = path && t.newLambdaVarKey(path);
                const emptyBodyKey = path && t.newEmptyBodyKey(path);
                if (body) {
                    const displayBody = rec(body, path && step(path, 'body'));
                    return t.newDisplayLambda(
                        exprKey, shouldHighlight, varKey, null, false, varName,
                        displayBody);
                } else {
                    const highlightBody = path != null &&
                        highlightedEmptyBodies.has(path);
                    return t.newDisplayLambda(
                        exprKey, shouldHighlight, varKey, emptyBodyKey,
                        highlightBody, varName, null);
                }
            },
            userFuncCall: ({func, arg}) => {
                const displayFunc = rec(func, path && step(path, 'func'));
                const displayArg = rec(arg, path && step(path, 'arg'));
                return t.newDisplayFuncCall(
                    exprKey, shouldHighlight, displayFunc, displayArg);
            },
            userVariable: ({varName}) => {
                return t.newDisplayVariable(exprKey, shouldHighlight, varName);
            },
            userReference: ({defName}) => {
                return t.newDisplayReference(exprKey, shouldHighlight, defName);
            },
        });
    };
    return rec(userExpr, exprId == null ? null : emptyPath(exprId));
};

export default generateDisplayState;