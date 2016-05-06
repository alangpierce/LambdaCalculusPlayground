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
import {resolveDrop, resolveTouch} from './HitTester'
import {
    addExpression,
    decomposeExpression,
    modifyExpression,
    insertAsArg,
    insertAsBody
} from './ExpressionState'

const initialState: State = t.newState(
    new Immutable.Map(), 0, new Immutable.Map());

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
        evaluateExpression: ({exprId, targetPos}) => {
            const existingCanvasExpr = exprWithId(exprId);
            if (!canStepUserExpr(existingCanvasExpr.expr)) {
                return state;
            }
            const evaluatedExpr = evaluateUserExpr(existingCanvasExpr.expr);
            if (!evaluatedExpr) {
                return state;
            }
            return addExpression(
                state, t.newCanvasExpression(evaluatedExpr, targetPos));
        },
        fingerDown: ({fingerId, screenPos}) => {
            const dragResult = resolveTouch(state, screenPos);
            return t.matchDragResult(dragResult, {
                pickUpExpression: ({exprId, offset}) => {
                    const canvasExpr = state.canvasExpressions.get(exprId);
                    return state
                        .updateCanvasExpressions(exprs => exprs.remove(exprId))
                        .updateActiveDrags((drags) =>
                            drags.set(fingerId, t.newDragData(offset, canvasExpr)));
                },
                decomposeExpression: ({exprPath, offset, newPos}) => {
                    const exprId = exprPath.exprId;
                    const existingCanvasExpr = exprWithId(exprId);
                    const {original, extracted} = decomposeExpression(
                        existingCanvasExpr.expr, exprPath.pathSteps);
                    const newCanvasPos =
                        t.newCanvasPoint(newPos.screenX, newPos.screenY);
                    const newCanvasExpr =
                        t.newCanvasExpression(extracted, newCanvasPos);
                    return state
                        .updateCanvasExpressions(canvasExprs =>
                            canvasExprs.update(exprId, canvasExpr =>
                                canvasExpr.withExpr(original)))
                        .updateActiveDrags(drags =>
                            drags.set(fingerId,
                                t.newDragData(offset, newCanvasExpr)));
                },
                createExpression: ({expr, offset, newPos}) => {
                    const newCanvasPos =
                        t.newCanvasPoint(newPos.screenX, newPos.screenY);
                    const newCanvasExpr =
                        t.newCanvasExpression(expr, newCanvasPos);
                    return state.updateActiveDrags(drags =>
                        drags.set(fingerId,
                            t.newDragData(offset, newCanvasExpr)));
                },
                startPan: () => {
                    // TODO
                    return state;
                },
            });
        },
        fingerMove: ({fingerId, screenPos: {screenX, screenY}}) => {
            const dragData: ?DragData = state.activeDrags.get(fingerId);
            if (!dragData) {
                return state;
            }
            const {offset: {dx, dy}} = dragData;
            const newPos = t.newCanvasPoint(screenX - dx, screenY - dy);
            return state.updateActiveDrags((drags) =>
                drags.update(fingerId, (dragData) =>
                    dragData.updateCanvasExpr((canvasExpr) =>
                        canvasExpr.withPos(newPos))));
        },
        fingerUp: ({fingerId, screenPos}) => {
            const dragData: ?DragData = state.activeDrags.get(fingerId);
            if (!dragData) {
                return state;
            }
            const dropResult = resolveDrop(state, dragData, screenPos);
            state = state.updateActiveDrags((drags) => drags.remove(fingerId));
            return t.matchDropResult(dropResult, {
                addToTopLevelResult: ({canvasExpr}) => {
                    return addExpression(state, canvasExpr);
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
                }
            });
        },
    });
};

export default playgroundApp;