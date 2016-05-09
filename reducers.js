/**
 * @flow
 */

import * as Immutable from 'immutable';

import {evaluateUserExpr, canStepUserExpr} from './UserExpressionEvaluator'
import type {
    Action,
    DragData,
    CanvasExpression,
    State
} from './types'
import * as t from './types'
import {
    addExpression,
    decomposeExpression,
    modifyExpression,
    insertAsArg,
    insertAsBody
} from './ExpressionState'
import {ptMinusPt, ptPlusDiff, rectPlusDiff} from './Geometry'
import {resolveDrop, resolveTouch} from './HitTester'
import {screenPtToCanvasPt} from './PointConversion'

const initialState: State = t.newState(
    new Immutable.Map(),
    0,
    new Immutable.Map(),
    new Immutable.Set(),
    new Immutable.Set());

// TODO: Consider adding a top-level try/catch.
const playgroundApp = (state: State = initialState, action: Action): State => {
    // Despite our action union, there are some internal redux actions that
    // start with @@, which we want to just ignore.
    if (action.type.startsWith('@@')) {
        return state;
    }

    const exprWithId = (exprId: number): CanvasExpression => {
        const result = state.canvasExpressions.get(exprId);
        if (!result) {
            throw new Error('Expected expression with ID ' + exprId);
        }
        return result;
    };

    return t.matchAction(action, {
        reset: () => initialState,
        addExpression: ({canvasExpr}) => addExpression(state, canvasExpr),
        moveExpression: ({exprId, pos}) => {
            return state.updateCanvasExpressions((exprs) =>
                exprs.update(exprId, (canvasExpr) =>
                    canvasExpr.withPos(pos)));
        },
        decomposeExpressionAction: ({path: {exprId, pathSteps}, targetPos}) => {
            const existingCanvasExpr = exprWithId(exprId);
            const {original, extracted} = decomposeExpression(
                existingCanvasExpr.expr, pathSteps);
            state = addExpression(
                state, t.newCanvasExpression(extracted, targetPos));
            state = modifyExpression(state, exprId,
                () => existingCanvasExpr.withExpr(original));
            return state;
        },
        insertAsArg: ({argExprId, path: {exprId, pathSteps}}) => {
            const argCanvasExpr = exprWithId(argExprId);
            const targetCanvasExpr = exprWithId(exprId);
            const resultExpr = insertAsArg(
                targetCanvasExpr.expr, argCanvasExpr.expr, pathSteps);
            const newCanvasExpr = targetCanvasExpr.withExpr(resultExpr);
            return state.updateCanvasExpressions((exprs) =>
                exprs.remove(argExprId).set(exprId, newCanvasExpr));
        },
        insertAsBody: ({bodyExprId, path: {exprId, pathSteps}}) => {
            const bodyCanvasExpr = exprWithId(bodyExprId);
            const targetCanvasExpr = exprWithId(exprId);
            const resultExpr = insertAsBody(
                targetCanvasExpr.expr, bodyCanvasExpr.expr, pathSteps);
            const newCanvasExpr = targetCanvasExpr.withExpr(resultExpr);
            return state.updateCanvasExpressions((exprs) =>
                exprs.remove(bodyExprId).set(exprId, newCanvasExpr));
        },
        evaluateExpression: ({exprId}) => {
            const existingExpr = exprWithId(exprId);
            if (!canStepUserExpr(existingExpr.expr)) {
                return state;
            }
            const evaluatedExpr = evaluateUserExpr(existingExpr.expr);
            if (!evaluatedExpr) {
                return state;
            }
            // TODO: Horizontally center the result.
            const targetPos = existingExpr.pos.updateCanvasY((y) => y + 100);
            return addExpression(
                state, t.newCanvasExpression(evaluatedExpr, targetPos));
        },
        fingerDown: ({fingerId, screenPos}) => {
            const dragResult = resolveTouch(state, screenPos);
            state = t.matchDragResult(dragResult, {
                pickUpExpression: ({exprId, offset, screenRect}) => {
                    const expr = exprWithId(exprId).expr;
                    return state
                        .updateCanvasExpressions(exprs => exprs.remove(exprId))
                        .updateActiveDrags((drags) =>
                            drags.set(fingerId,
                                t.newDragData(expr, offset, screenRect)));
                },
                decomposeExpression: ({exprPath, offset, screenRect}) => {
                    const exprId = exprPath.exprId;
                    const existingCanvasExpr = exprWithId(exprPath.exprId);
                    const {original, extracted} = decomposeExpression(
                        existingCanvasExpr.expr, exprPath.pathSteps);
                    return state
                        .updateCanvasExpressions(canvasExprs =>
                            canvasExprs.update(exprId, canvasExpr =>
                                canvasExpr.withExpr(original)))
                        .updateActiveDrags(drags =>
                            drags.set(fingerId,
                                t.newDragData(extracted, offset, screenRect)));
                },
                createExpression: ({expr, offset, screenRect}) => {
                    return state.updateActiveDrags(drags =>
                        drags.set(fingerId,
                            t.newDragData(expr, offset, screenRect)));
                },
                startPan: () => {
                    // TODO
                    return state;
                },
            });
            return computeHighlights(state);
        },
        fingerMove: ({fingerId, screenPos}) => {
            const dragData: ?DragData = state.activeDrags.get(fingerId);
            if (!dragData) {
                return state;
            }
            const {grabOffset, screenRect} = dragData;
            const oldGrabPoint = ptPlusDiff(screenRect.topLeft, grabOffset);
            const shiftAmount = ptMinusPt(screenPos, oldGrabPoint);
            const newScreenRect = rectPlusDiff(screenRect, shiftAmount);
            state = state.updateActiveDrags((drags) =>
                drags.update(fingerId, (dragData) =>
                    dragData.withScreenRect(newScreenRect)));
            return computeHighlights(state);
        },
        fingerUp: ({fingerId, screenPos}) => {
            const dragData: ?DragData = state.activeDrags.get(fingerId);
            if (!dragData) {
                return state;
            }
            const dropResult = resolveDrop(state, dragData, screenPos);
            state = state.updateActiveDrags((drags) => drags.remove(fingerId));
            state =  t.matchDropResult(dropResult, {
                addToTopLevelResult: ({expr, screenPos}) => {
                    const canvasPos = screenPtToCanvasPt(screenPos);
                    return addExpression(state,
                        t.newCanvasExpression(expr, canvasPos));
                },
                insertAsBodyResult: ({lambdaPath: {exprId, pathSteps}, expr}) => {
                    const targetCanvasExpr = exprWithId(exprId);
                    const resultExpr = insertAsBody(
                        targetCanvasExpr.expr, expr, pathSteps);
                    const newCanvasExpr = targetCanvasExpr.withExpr(resultExpr);
                    return state.updateCanvasExpressions((exprs) =>
                        exprs.set(exprId, newCanvasExpr));
                },
                insertAsArgResult: ({path: {exprId, pathSteps}, expr}) => {
                    const targetCanvasExpr = exprWithId(exprId);
                    const resultExpr = insertAsArg(
                        targetCanvasExpr.expr, expr, pathSteps);
                    const newCanvasExpr = targetCanvasExpr.withExpr(resultExpr);
                    return state.updateCanvasExpressions((exprs) =>
                        exprs.set(exprId, newCanvasExpr));
                },
                removeResult: () => {
                    // Do nothing; we've already removed the expression.
                    return state;
                },
            });
            return computeHighlights(state);
        },
    });
};

const computeHighlights = (state: State): State => {
    const exprPaths = [];
    const emptyBodyPaths = [];
    for (let [_, dragData] of state.activeDrags) {
        t.matchDropResult(resolveDrop(state, dragData), {
            addToTopLevelResult: () => {},
            insertAsBodyResult: ({lambdaPath}) => {
                emptyBodyPaths.push(lambdaPath)
            },
            insertAsArgResult: ({path}) => {exprPaths.push(path)},
            removeResult: () => {},
        });
    }
    return state
        .withHighlightedExprs(new Immutable.Set(exprPaths))
        .withHighlightedEmptyBodies(new Immutable.Set(emptyBodyPaths));
};

export default playgroundApp;