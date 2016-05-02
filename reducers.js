/**
 * @flow
 */

import * as Immutable from 'immutable';

import {evaluateUserExpr, canStepUserExpr} from './UserExpressionEvaluator'
import type {
    Action,
    DragData,
    ScreenExpression,
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

    const exprWithId = (exprId: number): ScreenExpression => {
        const result = state.screenExpressions.get(exprId);
        if (!result) {
            throw new Error('Expected expression with ID ' + exprId);
        }
        return result;
    };

    return t.matchAction(action, {
        reset: () => initialState,
        addExpression: ({screenExpr}) => addExpression(state, screenExpr),
        moveExpression: ({exprId, pos}) => {
            return state.updateScreenExpressions((exprs) =>
                exprs.update(exprId, (screenExpr) =>
                    screenExpr.withPos(pos)));
        },
        decomposeExpression: ({path: {exprId, pathSteps}, targetPos}) => {
            const existingScreenExpr = exprWithId(exprId);
            const {original, extracted} = decomposeExpression(
                existingScreenExpr.expr, pathSteps);
            state = addExpression(
                state, t.newScreenExpression(extracted, targetPos));
            state = modifyExpression(state, exprId,
                () => existingScreenExpr.withExpr(original));
            return state;
        },
        insertAsArg: ({argExprId, path: {exprId, pathSteps}}) => {
            const argScreenExpr = exprWithId(argExprId);
            const targetScreenExpr = exprWithId(exprId);
            const resultExpr = insertAsArg(
                targetScreenExpr.expr, argScreenExpr.expr, pathSteps);
            const newScreenExpr = targetScreenExpr.withExpr(resultExpr);
            return state.updateScreenExpressions((exprs) =>
                exprs.remove(argExprId).set(exprId, newScreenExpr));
        },
        insertAsBody: ({bodyExprId, path: {exprId, pathSteps}}) => {
            const bodyScreenExpr = exprWithId(bodyExprId);
            const targetScreenExpr = exprWithId(exprId);
            const resultExpr = insertAsBody(
                targetScreenExpr.expr, bodyScreenExpr.expr, pathSteps);
            const newScreenExpr = targetScreenExpr.withExpr(resultExpr);
            return state.updateScreenExpressions((exprs) =>
                exprs.remove(bodyExprId).set(exprId, newScreenExpr));
        },
        evaluateExpression: ({exprId, targetPos}) => {
            const existingScreenExpr = exprWithId(exprId);
            if (!canStepUserExpr(existingScreenExpr.expr)) {
                return state;
            }
            const evaluatedExpr = evaluateUserExpr(existingScreenExpr.expr);
            if (!evaluatedExpr) {
                return state;
            }
            return addExpression(
                state, t.newScreenExpression(evaluatedExpr, targetPos));
        },
        fingerDown: ({fingerId, screenPos}) => {
            const touchResult = resolveTouch(state, screenPos);
            if (touchResult === null || touchResult === undefined) {
                console.log("Touch didn't match anything.");
                return state;
            }
            const {exprId, offset} = touchResult;
            const screenExpr = state.screenExpressions.get(exprId);
            return state
                .updateScreenExpressions(exprs => exprs.remove(exprId))
                .updateActiveDrags((drags) =>
                    drags.set(fingerId, t.newDragData(offset, screenExpr)));
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
                    dragData.updateScreenExpr((screenExpr) =>
                        screenExpr.withPos(newPos))));
        },
        fingerUp: ({fingerId, screenPos}) => {
            const dragData: ?DragData = state.activeDrags.get(fingerId);
            if (!dragData) {
                return state;
            }
            const dropResult = resolveDrop(state, dragData, screenPos);
            state = state.updateActiveDrags((drags) => drags.remove(fingerId));
            return t.matchDropResult(dropResult, {
                addToTopLevelResult: ({screenExpr}) => {
                    return addExpression(state, screenExpr);
                },
                insertAsBodyResult: ({lambdaPath: {exprId, pathSteps}, expr}) => {
                    const targetScreenExpr = exprWithId(exprId);
                    const resultExpr = insertAsBody(
                        targetScreenExpr.expr, expr, pathSteps);
                    const newScreenExpr = targetScreenExpr.withExpr(resultExpr);
                    return state.updateScreenExpressions((exprs) =>
                        exprs.set(exprId, newScreenExpr));
                },
                insertAsArgResult: ({path: {exprId, pathSteps}, expr}) => {
                    const targetScreenExpr = exprWithId(exprId);
                    const resultExpr = insertAsArg(
                        targetScreenExpr.expr, expr, pathSteps);
                    const newScreenExpr = targetScreenExpr.withExpr(resultExpr);
                    return state.updateScreenExpressions((exprs) =>
                        exprs.set(exprId, newScreenExpr));
                }
            });
        },
    });
};

export default playgroundApp;