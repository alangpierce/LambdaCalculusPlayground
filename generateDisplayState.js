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
import {
    DraggedExpression
} from './types'
import {IList, ISet} from './types-collections'
import {canStepUserExpr, expandAllDefinitions} from './UserExpressionEvaluator'

const executeHandler = (exprId) => {
    return () => {
        store.dispatch(t.EvaluateExpression.make(exprId));
    };
};

const generateDisplayState = (state: State): DisplayState =>  {
    const screenExpressions: Array<ScreenExpression> = [];
    const screenDefinitions: Array<ScreenDefinition> = [];
    const {
        highlightedExprs, highlightedEmptyBodies, highlightedDefinitionBodies
    } = state;

    const definitions = expandAllDefinitions(state.definitions);

    for (let [exprId, canvasExpr] of state.canvasExpressions) {
        const rootPath = emptyIdPath(exprId);
        const displayExpr = buildDisplayExpression(
            canvasExpr.expr, rootPath, highlightedExprs,
            highlightedEmptyBodies);
        const isDragging = false;
        const isExecutable = canStepUserExpr(definitions, canvasExpr.expr);
        screenExpressions.push(t.ScreenExpression.make(
            displayExpr,
            canvasPtToScreenPt(canvasExpr.pos),
            'expr' + exprId,
            isDragging,
            isExecutable ? executeHandler(exprId) : null,
        ));
    }

    for (let [fingerId, dragData] of state.activeDrags) {
        const payload = dragData.payload;
        payload.match({
            draggedExpression: ({userExpr}) => {
                const displayExpr = buildDisplayExpression(
                    userExpr, null, highlightedExprs, highlightedEmptyBodies);
                const isDragging = true;
                const executeHandler = null;
                screenExpressions.push(t.ScreenExpression.make(
                    displayExpr,
                    dragData.screenRect.topLeft,
                    'dragExpr' + fingerId,
                    isDragging,
                    executeHandler,
                ));
            },
            draggedDefinition: ({defName}) => {
                const userExpr = state.definitions.get(defName);
                const isDragging = true;
                let displayExpr = null;
                if (userExpr != null) {
                    displayExpr = buildDisplayExpression(
                        userExpr, null, highlightedExprs, highlightedEmptyBodies);
                }
                screenDefinitions.push(t.ScreenDefinition.make(
                    defName,
                    displayExpr,
                    dragData.screenRect.topLeft,
                    null,
                    null,
                    null,
                    false,
                    'dragDef' + fingerId,
                    isDragging,
                ))
            }
        });
    }

    for (let [defName, canvasPoint] of state.canvasDefinitions) {
        const userExpr = state.definitions.get(defName);
        const isDragging = false;
        let displayExpr = null;
        if (userExpr != null) {
            const rootPath = emptyDefinitionPath(defName);
            displayExpr = buildDisplayExpression(
                userExpr, rootPath, highlightedExprs, highlightedEmptyBodies);
        }
        const shouldHighlightEmptyBody = highlightedDefinitionBodies.has(defName);
        screenDefinitions.push(t.ScreenDefinition.make(
            defName,
            displayExpr,
            canvasPtToScreenPt(canvasPoint),
            t.DefinitionKey.make(defName),
            t.DefinitionRefKey.make(defName),
            userExpr ? null : t.DefinitionEmptyBodyKey.make(defName),
            shouldHighlightEmptyBody,
            'def' + defName,
            isDragging,
        ))
    }

    const measureRequests: Array<MeasureRequest> = [];
    for (let [exprId, pendingResult] of state.pendingResults) {
        const displayExpr = buildDisplayExpression(
            pendingResult.expr, null, highlightedExprs, highlightedEmptyBodies);
        measureRequests.push(t.MeasureRequest.make(
            displayExpr,
            (width, height) => {
                store.dispatch(t.PlacePendingResult.make(exprId, width, height));
            }
        ))
    }

    return t.DisplayState.make(
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
        const exprKey = path && t.ExpressionKey.make(path);
        const shouldHighlight = path != null && highlightedExprs.has(path);
        return expr.match({
            userLambda: ({varName, body}) => {
                const varKey = path && t.LambdaVarKey.make(path);
                const emptyBodyKey = path && t.EmptyBodyKey.make(path);
                if (body) {
                    const displayBody = rec(body, path && step(path, 'body'));
                    return t.DisplayLambda.make(
                        exprKey, shouldHighlight, varKey, null, false, varName,
                        displayBody);
                } else {
                    const highlightBody = path != null &&
                        highlightedEmptyBodies.has(path);
                    return t.DisplayLambda.make(
                        exprKey, shouldHighlight, varKey, emptyBodyKey,
                        highlightBody, varName, null);
                }
            },
            userFuncCall: ({func, arg}) => {
                const displayFunc = rec(func, path && step(path, 'func'));
                const displayArg = rec(arg, path && step(path, 'arg'));
                return t.DisplayFuncCall.make(
                    exprKey, shouldHighlight, displayFunc, displayArg);
            },
            userVariable: ({varName}) => {
                return t.DisplayVariable.make(exprKey, shouldHighlight, varName);
            },
            userReference: ({defName}) => {
                return t.DisplayReference.make(exprKey, shouldHighlight, defName);
            },
        });
    };
    return rec(userExpr, rootPath);
};

export default generateDisplayState;