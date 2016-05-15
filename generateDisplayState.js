/**
 * @flow
 */

import {emptyIdPath, emptyDefinitionPath, step} from './ExprPaths'
import {canvasPtToScreenPt} from './PointConversion'
import store from './store'
import * as t from './types'
import type {
    DisplayExpression,
    DisplayState,
    ExprPath,
    MeasureRequest,
    ScreenDefinition,
    ScreenExpression,
    State,
    UserExpression,
} from './types'
import {IList, ISet} from './types-collections'
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
        const rootPath = emptyIdPath(exprId);
        const displayExpr = buildDisplayExpression(
            canvasExpr.expr, rootPath, highlightedExprs,
            highlightedEmptyBodies);
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

    const screenDefinitions: Array<ScreenDefinition> = [];
    for (let [defName, canvasPoint] of state.canvasDefinitions) {
        const userExpr = state.definitions.get(defName);
        const isDragging = false;
        let displayExpr = null;
        if (userExpr != null) {
            const rootPath = emptyDefinitionPath(defName);
            displayExpr = buildDisplayExpression(
                userExpr, rootPath, highlightedExprs, highlightedEmptyBodies);
        }
        screenDefinitions.push(t.newScreenDefinition(
            defName,
            displayExpr,
            canvasPtToScreenPt(canvasPoint),
            'def' + defName,
            isDragging,
        ))
    }

    const measureRequests: Array<MeasureRequest> = [];
    for (let [exprId, pendingResult] of state.pendingResults) {
        const displayExpr = buildDisplayExpression(
            pendingResult.expr, null, highlightedExprs, highlightedEmptyBodies);
        measureRequests.push(t.newMeasureRequest(
            displayExpr,
            (width, height) => {
                store.dispatch(t.newPlacePendingResult(exprId, width, height));
            }
        ))
    }

    return t.newDisplayState(
        IList.make(screenExpressions),
        IList.make(screenDefinitions),
        IList.make(measureRequests));
};

/**
 * Build a DisplayExpression with paths for the given expression ID. If exprId
 * is null, no paths are attached.
 */
const buildDisplayExpression = (
        userExpr: UserExpression, rootPath: ?ExprPath,
        highlightedExprs: ISet<ExprPath>,
        highlightedEmptyBodies: ISet<ExprPath>): DisplayExpression => {
    const rec = (expr: UserExpression, path: ?ExprPath): DisplayExpression => {
        const exprKey = path && t.newExpressionKey(path);
        const shouldHighlight = path != null && highlightedExprs.has(path);
        return expr.match({
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
    return rec(userExpr, rootPath);
};

export default generateDisplayState;